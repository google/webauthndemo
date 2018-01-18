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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;

public class PublicKeyCredentialDescriptor {
  /**
   * @param type
   * @param id
   */
  public PublicKeyCredentialDescriptor(PublicKeyCredentialType type, byte[] id) {
    this.type = type;
    this.id = id;
    this.transports = new ArrayList<AuthenticatorTransport>();
  }

  /**
   * @param type
   * @param id
   * @param transports
   */
  public PublicKeyCredentialDescriptor(PublicKeyCredentialType type, byte[] id,
      ArrayList<AuthenticatorTransport> transports) {
    this.type = type;
    this.id = id;
    this.transports = transports;
  }

  private PublicKeyCredentialType type;
  private byte[] id;
  private ArrayList<AuthenticatorTransport> transports;

  /**
   * @return Encoded JsonObject representation of PublicKeyCredentialDescriptor
   */
  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.addProperty("type", type.toString());

    result.addProperty("id", BaseEncoding.base64().encode(id));
    JsonArray transports = new JsonArray();
    if (this.transports != null) {
      for (AuthenticatorTransport t : this.transports) {
        JsonPrimitive element = new JsonPrimitive(t.toString());
        transports.add(element);
      }
      if (transports.size() > 0) {
        result.add("transports", transports);
      }
    }

    return result;
  }
}
