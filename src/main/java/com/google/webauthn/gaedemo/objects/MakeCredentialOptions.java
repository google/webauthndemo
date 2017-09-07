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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.storage.Attachment;
import java.security.SecureRandom;
import java.util.ArrayList;
import com.google.common.io.BaseEncoding;

public class MakeCredentialOptions {
  private static final int CHALLENGE_LENGTH = 32;
  private final SecureRandom random = new SecureRandom();

  /**
   * 
   */
  public MakeCredentialOptions() {
    parameters = new ArrayList<PublicKeyCredentialParameters>();
    excludeList = new ArrayList<PublicKeyCredentialDescriptor>();
  }

  /**
   * @param userId
   * @param rpId
   * @param rpName
   */
  public MakeCredentialOptions(String userName, String userId, String rpId, String rpName) {
    parameters = new ArrayList<PublicKeyCredentialParameters>();
    excludeList = new ArrayList<PublicKeyCredentialDescriptor>();
    rp = new PublicKeyCredentialEntity(rpId, rpName, null);
    user = new PublicKeyCredentialUserEntity(userName, userId);

    challenge = new byte[CHALLENGE_LENGTH];
    random.nextBytes(challenge);
    parameters.add(
        new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, Algorithm.ES256));
  }

  PublicKeyCredentialEntity rp;
  PublicKeyCredentialUserEntity user;

  public byte[] challenge;
  ArrayList<PublicKeyCredentialParameters> parameters;
  long timeout;
  ArrayList<PublicKeyCredentialDescriptor> excludeList;
  Attachment attachment;

  /**
   * @return Encoded JsonObect representation of MakeCredentialOptions
   */
  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.add("rp", rp.getJsonObject());
    result.add("user", user.getJsonObject());
    result.addProperty("challenge", BaseEncoding.base64().encode(challenge));
    if (parameters.size() > 0) {
      JsonArray params = new JsonArray();
      for (PublicKeyCredentialParameters param : parameters) {
        params.add(param.getJsonObject());
      }
      result.add("parameters", params);
    }
    if (timeout > 0) {
      result.addProperty("timeout", timeout);
    }
    if (excludeList.size() > 0) {
      JsonArray params = new JsonArray();
      for (PublicKeyCredentialDescriptor descriptor : excludeList) {
        params.add(descriptor.getJsonObject());
      }
      result.add("excludeList", params);
    }
    if (attachment != null) {
      result.addProperty("attachment", attachment.toString());
    }
    return result;
  }
}
