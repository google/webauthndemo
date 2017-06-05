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

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.UnicodeString;
import com.googlecode.objectify.annotation.Subclass;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

@Subclass
public class RsaKey extends CredentialPublicKey {
  byte[] n, e;

  RsaKey() {
    n = null;
    e = null;
    alg = Algorithm.UNDEFINED;
  }

  @Override
  public boolean equals(Object obj) {
    try {
      if (obj instanceof RsaKey) {
        RsaKey other = (RsaKey) obj;
        if (Arrays.equals(n, other.n) && Arrays.equals(e, other.e) && alg == other.alg) {
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
        new CborBuilder().addMap().put(new UnicodeString("alg"), new UnicodeString(alg.toString()))
            .put(new UnicodeString("n"), new ByteString(n))
            .put(new UnicodeString("e"), new ByteString(e)).end().build();
    new CborEncoder(output).encode(dataItems);
    return output.toByteArray();
  }
}
