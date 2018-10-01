// Copyright 2018 Google Inc.
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

public class PublicKeyCredentialRpEntity extends PublicKeyCredentialEntity {
  protected String id;

  /**
   * @param id
   * @param name
   * @param icon
   */
  PublicKeyCredentialRpEntity(String id, String name, String icon) {
    super(name, icon);
    this.id = id;
  }

  PublicKeyCredentialRpEntity() {
    super(null, null);
    this.id = null;
  }

  @Override
  public JsonObject getJsonObject() {
    Gson gson = new Gson();
    return (JsonObject) gson.toJsonTree(this);
  }
}
