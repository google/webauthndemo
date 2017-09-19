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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class AuthenticatorSelectionCriteria {
  public AuthenticatorAttachment attachment;
  public boolean rk;
  public boolean uv;

  public AuthenticatorSelectionCriteria() {
    attachment = null;
    rk = false;
    uv = false;
  }

  public AuthenticatorSelectionCriteria(String attachment, boolean requireResidentKey, boolean uv) {
    this.attachment = AuthenticatorAttachment.decode(attachment);
    this.rk = requireResidentKey;
    this.uv = uv;
  }

  public static AuthenticatorSelectionCriteria parse(String jsonString) {
    Gson gson = new Gson();
    return gson.fromJson(jsonString, AuthenticatorSelectionCriteria.class);
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
