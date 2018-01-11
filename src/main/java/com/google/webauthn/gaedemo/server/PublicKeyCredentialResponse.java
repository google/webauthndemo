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

package com.google.webauthn.gaedemo.server;

import com.google.gson.Gson;

public class PublicKeyCredentialResponse {
  protected boolean success;
  protected String message;
  protected String handle;

  public PublicKeyCredentialResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public PublicKeyCredentialResponse(boolean success, String message, String handle) {
    this.success = success;
    this.message = message;
    this.handle = handle;
  }

  public String toJson() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}
