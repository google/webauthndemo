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

@Subclass
public class AuthenticatorAssertionResponse extends AuthenticatorResponse {
  private static class AssertionResponseJson {
    String clientDataJSON;
    String authenticatorData;
    String signature;
    String userHandle;
  }

  byte[] authDataBytes;
  AuthenticatorData authData;
  byte[] signature;
  byte[] userHandle;

  /**
   * @param data
   * @throws ResponseException
   */
  public AuthenticatorAssertionResponse(JsonElement data) throws ResponseException {
    Gson gson = new Gson();
    try {
      AssertionResponseJson parsedObject = gson.fromJson(data, AssertionResponseJson.class);
      clientDataBytes = BaseEncoding.base64().decode(parsedObject.clientDataJSON);
      clientData = gson.fromJson(new String(clientDataBytes), CollectedClientData.class);

      authDataBytes = BaseEncoding.base64().decode(parsedObject.authenticatorData);
      authData =
          AuthenticatorData.decode(authDataBytes);
      signature = BaseEncoding.base64().decode(parsedObject.signature);
      userHandle = BaseEncoding.base64().decode(parsedObject.userHandle);
    } catch (JsonSyntaxException e) {
      throw new ResponseException("Response format incorrect");
    }
  }

  /**
   * @return byte array of authData
   */
  public byte[] getAuthDataBytes() {
    return authDataBytes;
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

  /**
   * @return the userHandle
   */
  public byte[] getUserHandle() {
    return userHandle;
  }
}
