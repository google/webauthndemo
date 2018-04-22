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
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.googlecode.objectify.annotation.Subclass;
import java.util.Arrays;

/**
 * Object representation of the Android SafetyNet attestation statement
 */
@Subclass
public class AndroidSafetyNetAttestationStatement extends AttestationStatement {

  String ver;
  byte[] response;

  public AndroidSafetyNetAttestationStatement() {
    this.ver = null;
    this.response = null;
  }

  /**
   * Decodes a cbor representation of an AndroidSafetyNetAttestationStatement into the object
   * representation
   *
   * @param attStmt Cbor DataItem representation of the attestation statement to decode
   * @return Decoded AndroidSafetyNetAttestationStatement
   * @throws ResponseException Input was not a valid AndroidSafetyNetAttestationStatement DataItem
   */
  public static AndroidSafetyNetAttestationStatement decode(DataItem attStmt)
      throws ResponseException {
    AndroidSafetyNetAttestationStatement result = new AndroidSafetyNetAttestationStatement();
    Map given = (Map) attStmt;
    for (DataItem data : given.getKeys()) {
      if (data instanceof UnicodeString) {
        if (((UnicodeString) data).getString().equals("ver")) {
          UnicodeString version = (UnicodeString) given.get(data);
          result.ver = version.getString();
        } else if (((UnicodeString) data).getString().equals("response")) {
          result.response = ((ByteString) (given.get(data))).getBytes();
        }
      }
    }
    if (result.response == null || result.ver == null)
      throw new ResponseException("Invalid JWT Cbor");
    return result;
  }

  @Override
  DataItem encode() throws CborException {
    Map map = new Map();
    map.put(new UnicodeString("ver"), new UnicodeString(ver));
    map.put(new UnicodeString("response"), new ByteString(response));
    return map;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AndroidSafetyNetAttestationStatement) {
      AndroidSafetyNetAttestationStatement other = (AndroidSafetyNetAttestationStatement) obj;
      if (ver == other.ver || ((ver != null && other.ver != null) && ver.equals(other.ver))) {
        if (Arrays.equals(response, other.response)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Return the Google Play Services version used to create the SafetyNet attestation
   *
   * @return the version
   */
  public String getVer() {
    return ver;
  }

  /**
   * @return the response bytes
   */
  public byte[] getResponse() {
    return response;
  }

  @Override
  public String getName() {
    return "Android SafetyNet";
  }

  @Override
  public AttestationStatementEnum getAttestationType() {
    return AttestationStatementEnum.ANDROIDSAFETYNET;
  }

  @Override
  public byte[] getCert() {
    return response;
  }
}
