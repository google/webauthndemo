/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.google.webauthn.gaedemo.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import co.nstant.in.cbor.CborException;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import java.security.SecureRandom;
import java.util.Arrays;
import org.junit.Test;

public class AuthenticatorAssertionResponseTest {
  final SecureRandom random = new SecureRandom();

  /**
   * Test method for
   * {@link com.google.webauthn.gaedemo.objects.AuthenticatorAssertionResponse#AuthenticatorAssertionResponse(java.lang.String)}.
   */
  // @Test
  public void testAuthenticatorAssertionResponse() {
    Gson gson = new Gson();
    CollectedClientData clientData = new CollectedClientData();
    clientData.challenge = "challengeString";
    clientData.hashAlg = "SHA-256";
    clientData.origin = "https://localhost";
    String clientJson = gson.toJson(clientData);
    String clientBase64 = BaseEncoding.base64Url().encode(clientJson.getBytes());

    AuthenticatorData authData = null;
    {
      byte flags = 1 << 6;
      byte[] rpIdHash = new byte[32];

      authData = new AuthenticatorData(rpIdHash, flags, 0);
    }

    String authenticatorBase64 = null;
    try {
      authenticatorBase64 = BaseEncoding.base64().encode(authData.encode());
    } catch (CborException e1) {
      fail(e1.toString());
    }

    byte[] signature = new byte[32];
    random.nextBytes(signature);
    String signatureBase64 = BaseEncoding.base64().encode(signature);

    JsonObject json = new JsonObject();
    json.addProperty("clientDataJSON", clientJson);
    json.addProperty("authenticatorData", authenticatorBase64);
    json.addProperty("signature", signatureBase64);

    JsonElement element = gson.fromJson(json.toString(), JsonElement.class);
    System.out.println(json.toString());
    try {
      AuthenticatorAssertionResponse decoded = new AuthenticatorAssertionResponse(element);
      assertTrue(Arrays.equals(decoded.signature, signature));
      assertEquals(decoded.getClientData(), clientJson);
      assertEquals(decoded.getAuthenticatorData(), authData);
    } catch (ResponseException e) {
      fail("Decode failed" + e);
    }
  }
}
