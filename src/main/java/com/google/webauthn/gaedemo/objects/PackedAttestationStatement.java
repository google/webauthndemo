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

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import com.googlecode.objectify.annotation.Subclass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Subclass
public class PackedAttestationStatement extends AttestationStatement {
  public byte[] sig;
  public byte[] attestnCert;
  public List<byte[]> caCert;
  public Algorithm alg;
  public byte[] ecdaaKeyId;

  /**
   * @param sig
   * @param attestnCert
   * @param caCert
   */
  public PackedAttestationStatement(byte[] sig, byte[] attestnCert, List<byte[]> caCert,
      String alg) {
    super();
    this.sig = sig;
    this.attestnCert = attestnCert;
    this.caCert = caCert;
    this.alg = Algorithm.decode(alg);
    this.ecdaaKeyId = null;
  }

  /**
   * @param sig
   * @param attestnCert
   * @param caCert
   */
  public PackedAttestationStatement(byte[] sig, byte[] ecdaaKeyId, String alg) {
    super();
    this.sig = sig;
    this.ecdaaKeyId = ecdaaKeyId;
    this.alg = Algorithm.decode(alg);
    this.caCert = null;
    this.attestnCert = null;
  }

  public PackedAttestationStatement() {
    this.sig = null;
    this.attestnCert = null;
    this.caCert = null;
    this.alg = null;
    this.ecdaaKeyId = null;
  }

  /**
   * @param attStmt
   * @return Decoded FidoU2fAttestationStatement
   */
  public static PackedAttestationStatement decode(DataItem attStmt) {
    PackedAttestationStatement result = new PackedAttestationStatement();
    Map given = null;

    if (attStmt instanceof ByteString) {
      byte[] temp = ((ByteString) attStmt).getBytes();
      List<DataItem> dataItems = null;
      try {
        dataItems = CborDecoder.decode(temp);
      } catch (Exception e) {
      }
      given = (Map) dataItems.get(0);
    } else {
      given = (Map) attStmt;
    }

    for (DataItem data : given.getKeys()) {
      if (data instanceof UnicodeString) {
        if (((UnicodeString) data).getString().equals("x5c")) {
          Array array = (Array) given.get(data);
          List<DataItem> list = array.getDataItems();
          if (list.size() > 0) {
            result.attestnCert = ((ByteString) list.get(0)).getBytes();
          }
          result.caCert = new ArrayList<byte[]>();
          for (int i = 1; i < list.size(); i++) {
            result.caCert.add(((ByteString) list.get(i)).getBytes());
          }
        } else if (((UnicodeString) data).getString().equals("sig")) {
          result.sig = ((ByteString) (given.get(data))).getBytes();
        } else if (((UnicodeString) data).getString().equals("alg")) {
          result.alg = Algorithm.decode(((UnicodeString) (given.get(data))).getString());
        } else if (((UnicodeString) data).getString().equals("ecdaaKeyId")) {
          result.ecdaaKeyId = ((ByteString) (given.get(data))).getBytes();
        }
      }
    }
    return result;
  }

  @Override
  DataItem encode() throws CborException {
    Map result = new Map();
    if (attestnCert != null) {
      Array x5c = new Array();
      x5c.add(new ByteString(attestnCert));
      for (byte[] cert : this.caCert) {
        x5c.add(new ByteString(cert));
      }
      result.put(new UnicodeString("x5c"), x5c);
    }
    if (ecdaaKeyId != null) {
      result.put(new UnicodeString("ecdaaKeyId"), new ByteString(ecdaaKeyId));
    }
    result.put(new UnicodeString("sig"), new ByteString(sig));
    result.put(new UnicodeString("alg"), new UnicodeString(alg.toString()));

    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PackedAttestationStatement) {
      PackedAttestationStatement other = (PackedAttestationStatement) obj;
      if (attestnCert == other.attestnCert || Arrays.equals(attestnCert, other.attestnCert)) {
        if (Arrays.equals(sig, other.sig)) {
          if (caCert == other.caCert || caCert.size() == other.caCert.size()) {
            if (caCert != null) {
              for (int i = 0; i < caCert.size(); i++) {
                if (!Arrays.equals(caCert.get(i), other.caCert.get(i))) {
                  return false;
                }
              }
            }
            if (other.alg != alg) {
              return false;
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public String getName() {
    return "Packed Attestation";
  }
}
