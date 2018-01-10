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

public class AuthenticatorSelectionCriteria {
  public AuthenticatorAttachment authenticatorAttachment;
  public boolean requireResidentKey;
  public UserVerificationRequirement userVerification;

  public AuthenticatorSelectionCriteria() {
    authenticatorAttachment = null;
    requireResidentKey = false;
    userVerification = UserVerificationRequirement.PREFERRED;
  }

  public AuthenticatorSelectionCriteria(AuthenticatorAttachment authenticatorAttachment,
      boolean requireResidentKey, UserVerificationRequirement userVerification) {
    this.authenticatorAttachment = authenticatorAttachment;
    this.requireResidentKey = requireResidentKey;
    this.userVerification = userVerification;
  }

  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    if (authenticatorAttachment != null) {
      result.addProperty("authenticatorAttachment", authenticatorAttachment.toString());
    }
    result.addProperty("requireResidentKey", requireResidentKey);
    if (userVerification != null) {
      result.addProperty("userVerification", userVerification.toString());
    }
    return result;
  }

}
