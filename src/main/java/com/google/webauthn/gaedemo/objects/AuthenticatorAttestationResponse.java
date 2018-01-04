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
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.googlecode.objectify.annotation.Subclass;

import co.nstant.in.cbor.CborException;

@Subclass
public class AuthenticatorAttestationResponse extends AuthenticatorResponse {
  private static class AttestationResponseJson {
    String clientDataJSON;
    String attestationObject;
  }

  public AttestationObject decodedObject;

  /**
   *
   */
  public AuthenticatorAttestationResponse() {}

  public AuthenticatorAttestationResponse(String clientDataJSON, String attestationObject)
      throws ResponseException {
    clientData = CollectedClientData.decode(clientDataJSON);
    clientDataString = clientDataJSON;
    try {
      decodedObject = AttestationObject.decode(BaseEncoding.base64().decode(attestationObject));
    } catch (CborException e) {
      throw new ResponseException("Cannot decode attestation object");
    }
  }

  /**
   * @param data
   * @throws ResponseException
   */
  public AuthenticatorAttestationResponse(JsonElement data) throws ResponseException {
    Gson gson = new Gson();
    AttestationResponseJson parsedObject = gson.fromJson(data, AttestationResponseJson.class);

    clientDataBytes = BaseEncoding.base64().decode(parsedObject.clientDataJSON);
    byte[] attestationObject = BaseEncoding.base64().decode(parsedObject.attestationObject);

    try {
      decodedObject = AttestationObject.decode(attestationObject);
    } catch (CborException e) {
      throw new ResponseException("Cannot decode attestation object");
    }

    clientData = gson.fromJson(new String(clientDataBytes), CollectedClientData.class);
  }

  /**
   * @return json encoded representation of the AuthenticatorAttestationResponse
   */
  public String encode() {
    JsonObject json = new JsonObject();
    json.addProperty("clientDataJSON", BaseEncoding.base64().encode(clientDataBytes));
    try {
      json.addProperty("attestationObject", BaseEncoding.base64().encode(decodedObject.encode()));
    } catch (CborException e) {
      return null;
    }
    return json.getAsString();
  }

  /**
   * @return the decodedObject
   */
  public AttestationObject getAttestationObject() {
    return decodedObject;
  }
}
