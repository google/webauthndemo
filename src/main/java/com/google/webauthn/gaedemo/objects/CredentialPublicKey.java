package com.google.webauthn.gaedemo.objects;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import java.util.List;

public abstract class CredentialPublicKey {
  Algorithm alg;

  /**
   * @return Cbor encoded byte array
   * @throws CborException
   */
  public abstract byte[] encode() throws CborException;

  /**
   * @param cbor
   * @return CredentialPublicKey object decoded from cbor byte array
   * @throws CborException
   */
  public static CredentialPublicKey decode(byte[] cbor) throws CborException {
    CredentialPublicKey cpk = null;

    List<DataItem> dataItems = CborDecoder.decode(cbor);
    if (dataItems.size() < 1 || !(dataItems.get(0) instanceof Map)) {
      return null;
    }

    Map map = (Map) dataItems.get(0);
    for (DataItem key : map.getKeys()) {
      UnicodeString keyString = (UnicodeString) key;
      if (keyString.getString().equals("alg")) {
        String algorithm = ((UnicodeString) map.get(keyString)).getString();
        Algorithm alg = null;
        try {
          alg = Algorithm.decode(algorithm);
        } catch (IllegalArgumentException e) {
          break;
        }
        if (Algorithm.isEccAlgorithm(alg)) {
          EccKey ecc = new EccKey();
          ecc.alg = alg;
          for (DataItem d : map.getKeys()) {
            if (((UnicodeString) d).getString().equals("x")) {
              ecc.x = ((ByteString) map.get(d)).getBytes();
            } else if (((UnicodeString) d).getString().equals("y")) {
              ecc.y = ((ByteString) map.get(d)).getBytes();
            }
          }
          cpk = ecc;
          break;
        } else if (Algorithm.isRsaAlgorithm(alg)) {
          RsaKey rsa = new RsaKey();
          rsa.alg = alg;
          for (DataItem d : map.getKeys()) {
            if (((UnicodeString) d).getString().equals("n")) {
              rsa.n = ((ByteString) map.get(d)).getBytes();
            } else if (((UnicodeString) d).getString().equals("e")) {
              rsa.e = ((ByteString) map.get(d)).getBytes();
            }
          }
          cpk = rsa;
          break;
        }
      }
    }
    return cpk;
  }
}
