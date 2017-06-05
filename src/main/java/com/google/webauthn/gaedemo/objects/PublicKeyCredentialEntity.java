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

import com.google.gson.JsonObject;

public class PublicKeyCredentialEntity {
  private String id;
  private String name;
  private String icon;

  /**
   * @param id
   * @param name
   * @param icon
   */
  PublicKeyCredentialEntity(String id, String name, String icon) {
    this.id = id;
    this.name = name;
    this.icon = icon;
  }

  /**
   * @return Encoded JsonObject representation of PublicKeyCredentialEntity
   */
  JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.addProperty("id", id);
    result.addProperty("name", name);
    result.addProperty("icon", icon);

    return result;
  }
}
