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

public enum AttestationStatementEnum {
  FIDOU2F("fido-u2f"),
  ANDROIDSAFETYNET("android-safetynet"),
  PACKED("packed"),
  NONE("none");

  private final String name;

  /**
   * @param name
   */
  private AttestationStatementEnum(String name) {
    this.name = name;
  }

  /**
   * @param s
   * @return Attestation Statement Format Identifiers corresponding to the input string
   */
  public static AttestationStatementEnum decode(String s) {
    for (AttestationStatementEnum t : AttestationStatementEnum.values()) {
      if (t.name.equals(s)) {
        return t;
      }
    }
    throw new IllegalArgumentException(s + " not a valid Attestation Statement Format Identifiers");
  }

  @Override
  public String toString() {
    return name;
  }
}
