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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.DatatypeConverter;

import com.google.appengine.repackaged.com.google.common.primitives.Bytes;
import com.googlecode.objectify.annotation.Subclass;
import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.NegativeInteger;
import co.nstant.in.cbor.model.UnsignedInteger;

@Subclass
public class EccKey extends CredentialPublicKey {
  byte[] x, y;
  int crv;

  EccKey() {
    x = null;
    y = null;
    alg = Algorithm.UNDEFINED;
  }

  public EccKey(Algorithm alg, byte[] x, byte[] y) {
    this.alg = alg;
    this.x = x;
    this.y = y;
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
  public int hashCode() {
    return Arrays.hashCode(Bytes.concat(x, y));
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
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    List<DataItem> dataItems =
        new CborBuilder().addMap().put(new UnsignedInteger(KTY_LABEL), new UnsignedInteger(kty))
            .put(new UnsignedInteger(ALG_LABEL), new NegativeInteger(alg.encodeToInt()))
            .put(new NegativeInteger(CRV_LABEL), new UnsignedInteger(crv))
            .put(new NegativeInteger(X_LABEL), new ByteString(x))
            .put(new NegativeInteger(Y_LABEL), new ByteString(y)).end().build();
    new CborEncoder(output).encode(dataItems);
    return output.toByteArray();
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
    StringBuilder b = new StringBuilder();
    b.append("alg:");
    b.append(alg.toReadableString());
    b.append(" x:");
    b.append(DatatypeConverter.printHexBinary(x));
    b.append(" y:");
    b.append(DatatypeConverter.printHexBinary(y));
    return b.toString();
  }

}
