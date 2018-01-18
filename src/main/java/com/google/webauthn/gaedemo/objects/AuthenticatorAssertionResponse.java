// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.webauthn.gaedemo.objects;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.googlecode.objectify.annotation.Subclass;

import java.util.Map;

@Subclass
public class AuthenticatorAssertionResponse extends AuthenticatorResponse {
  private static class AssertionResponseJson {
    Map<String, Byte> clientDataJSON;
    String authenticatorData;
    String signature;
  }

  AuthenticatorData authData;
  byte[] signature;

  public AuthenticatorAssertionResponse() {}

  public AuthenticatorAssertionResponse(String clientDataJSON, String authenticatorData,
      String signatureString) throws ResponseException {
    clientData = CollectedClientData.decode(clientDataJSON);
    clientDataString = clientDataJSON;
    authData = AuthenticatorData.decode(BaseEncoding.base64().decode(authenticatorData));
    signature = BaseEncoding.base64().decode(signatureString);
  }

  /**
   * @param data
   * @throws ResponseException
   */
  public AuthenticatorAssertionResponse(JsonElement data) throws ResponseException {
    Gson gson = new Gson();
    try {
      AssertionResponseJson parsedObject = gson.fromJson(data, AssertionResponseJson.class);

      StringBuffer decodedData = new StringBuffer();
      // This should probably need some kind of sort to be stable, but for now
      // values() seems to walk through this crazy map alright
      for (byte b : parsedObject.clientDataJSON.values()) {
        decodedData.appendCodePoint(b);
      }
      System.out.println("Decoded data: " + decodedData.toString());

      // Temporary until fix clientData ordering issue.
      clientDataString = decodedData.toString();
      clientData = gson.fromJson(decodedData.toString(), CollectedClientData.class);

      authData =
          AuthenticatorData.decode(BaseEncoding.base64().decode(parsedObject.authenticatorData));
      signature = BaseEncoding.base64().decode(parsedObject.signature);
    } catch (JsonSyntaxException e) {
      throw new ResponseException("Response format incorrect");
    }
  }

  /**
   * @return the authData
   */
  public AuthenticatorData getAuthenticatorData() {
    return authData;
  }

  /**
   * @return the signature
   */
  public byte[] getSignature() {
    return signature;
  }
}
