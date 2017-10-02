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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.googlecode.objectify.annotation.Subclass;

import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;

@Subclass
public class EccKey extends CredentialPublicKey {
  byte[] x, y;

  EccKey() {
    x = null;
    y = null;
    alg = Algorithm.UNDEFINED;
  }

  /**
   * @param x
   * @param y
   */
  public EccKey(byte[] x, byte[] y) {
    super();
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object obj) {
    try {
      if (obj instanceof EccKey) {
        EccKey other = (EccKey) obj;
        if (Arrays.equals(x, other.x) && Arrays.equals(y, other.y) && alg == other.alg) {
          return true;
        }
      }
    } catch (NullPointerException e) {
    }
    return false;
  }

  @Override
  public byte[] encode() throws CborException {
    return cborEncodedKey;
  }

  /**
   * @return the x
   */
  public byte[] getX() {
    return x;
  }

  /**
   * @return the y
   */
  public byte[] getY() {
    return y;
  }

  @Override
  public String toString() {
    StringBuffer b = new StringBuffer();
    b.append("alg:");
    b.append(alg.toReadableString());
    b.append(" x:");
    b.append(DatatypeConverter.printHexBinary(x));
    b.append(" y:");
    b.append(DatatypeConverter.printHexBinary(y));
    return b.toString();
  }

}
