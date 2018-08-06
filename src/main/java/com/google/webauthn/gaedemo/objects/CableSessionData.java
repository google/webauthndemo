/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.webauthn.gaedemo.objects;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonObject;
import java.util.Arrays;

public class CableSessionData {

  private static final BaseEncoding HEX = BaseEncoding.base16();

  public int version;
  public byte[] clientEid;
  public byte[] authenticatorEid;
  public byte[] sessionPreKey;

  public CableSessionData() {
  }

  public CableSessionData(int version, byte[] clientEid, byte[] authenticatorEid,
      byte[] sessionPreKey) {
    this.version = version;
    this.clientEid = clientEid;
    this.authenticatorEid = authenticatorEid;
    this.sessionPreKey = sessionPreKey;
  }

  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.addProperty("version", version);
    result.addProperty("clientEid", HEX.encode(clientEid));
    result.addProperty("authenticatorEid", HEX.encode(authenticatorEid));
    result.addProperty("sessionPreKey", HEX.encode(sessionPreKey));
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CableSessionData that = (CableSessionData) o;
    return version == that.version &&
        Arrays.equals(clientEid, that.clientEid) &&
        Arrays.equals(authenticatorEid, that.authenticatorEid) &&
        Arrays.equals(sessionPreKey, that.sessionPreKey);
  }
}
