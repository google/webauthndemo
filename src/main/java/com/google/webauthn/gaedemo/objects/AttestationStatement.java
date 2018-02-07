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

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;
import com.google.webauthn.gaedemo.exceptions.ResponseException;

public abstract class AttestationStatement {

  /**
   * @param fmt
   * @param attStmt
   * @return Attestation statement of provided format
   */
  public static AttestationStatement decode(String fmt, DataItem attStmt) {
    if (fmt.equals("fido-u2f")) {
      FidoU2fAttestationStatement stmt = FidoU2fAttestationStatement.decode(attStmt);
      return stmt;
    } else if (fmt.equals("android-safetynet")) {
      AndroidSafetyNetAttestationStatement stmt;
      try {
        stmt = AndroidSafetyNetAttestationStatement.decode(attStmt);
      } catch (ResponseException e) {
        return null;
      }
      return stmt;
    } else if (fmt.equals("packed")) {
      return PackedAttestationStatement.decode(attStmt);
    } else if (fmt.equals("none")) {
      return new NoneAttestationStatement();
    }

    return null;
  }

  /**
   * @return Encoded AttestationStatement
   * @throws CborException
   */
  abstract DataItem encode() throws CborException;

  public abstract String getName();


}
