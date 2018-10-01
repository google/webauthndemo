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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationExtensions {

  public List<CableSessionData> cableAuthentication;

  public static AuthenticationExtensions parse(String parameter) {
    Gson gson = new Gson();
    return gson.fromJson(parameter, AuthenticationExtensions.class);
  }

  public void addCableSessionData(CableSessionData cableSessionData) {
    if (cableAuthentication == null) {
      cableAuthentication = new ArrayList<>();
    }
    cableAuthentication.add(cableSessionData);
  }

  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    if (cableAuthentication != null) {
      JsonArray cableSessionDatas = new JsonArray();
      for (CableSessionData sessionData : cableAuthentication) {
        cableSessionDatas.add(sessionData.getJsonObject());
      }
      result.add("cableAuthentication", cableSessionDatas);
    }
    return result;
  }

}
