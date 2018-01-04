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

import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AuthenticatorSelectionCriteria {
  public AuthenticatorAttachment attachment;
  public boolean rk;
  public boolean uv;

  public AuthenticatorSelectionCriteria() {
    attachment = null;
    rk = false;
    uv = false;
  }

  public AuthenticatorSelectionCriteria(AuthenticatorAttachment attachment,
      boolean requireResidentKey, boolean uv) {
    this.attachment = attachment;
    this.rk = requireResidentKey;
    this.uv = uv;
  }

  public static AuthenticatorSelectionCriteria parse(String jsonString) {
    JsonElement jsonElement = new JsonParser().parse(jsonString);
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
    boolean rk = false;
    boolean uv = false;
    AuthenticatorAttachment attachment = null;
    for (Map.Entry<String, JsonElement> entry : entries) {
      if (entry.getKey().equals("rk")) {
        rk = entry.getValue().getAsBoolean();
      } else if (entry.getKey().equals("uv")) {
        uv = entry.getValue().getAsBoolean();
      } else if (entry.getKey().equals("attachment")) {
        attachment = AuthenticatorAttachment.decode(entry.getValue().getAsString());
      }
    }

    return new AuthenticatorSelectionCriteria(attachment, rk, uv);
  }

  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    if (attachment != null) {
      result.addProperty("attachment", attachment.toString());
    }
    result.addProperty("requireResidentKey", rk);
    result.addProperty("uv", uv);
    return result;
  }

}
