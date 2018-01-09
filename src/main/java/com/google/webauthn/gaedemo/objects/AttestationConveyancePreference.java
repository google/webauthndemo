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

public enum AttestationConveyancePreference {
  NONE("none"), INDIRECT("indirect"), DIRECT("direct");

  private final String val;

  AttestationConveyancePreference(String s) {
    this.val = s;
  }

  /**
   * @param s
   * @return AuthenticatorAttachment corresponding to the input string
   */
  public static AttestationConveyancePreference decode(String s) {
    for (AttestationConveyancePreference a : AttestationConveyancePreference.values()) {
      if (a.val.equals(s)) {
        return a;
      }
    }
    throw new IllegalArgumentException(s + " not a valid AttestationConveyancePreference");
  }

  @Override
  public String toString() {
    return this.val;
  }
}
