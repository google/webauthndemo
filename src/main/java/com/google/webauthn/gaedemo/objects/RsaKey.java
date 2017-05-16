package com.google.webauthn.gaedemo.objects;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.UnicodeString;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

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
