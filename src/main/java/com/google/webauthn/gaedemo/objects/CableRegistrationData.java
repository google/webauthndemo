package com.google.webauthn.gaedemo.objects;

import java.util.ArrayList;
import java.util.List;

import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import co.nstant.in.cbor.model.UnsignedInteger;

public class CableRegistrationData implements AttestationExtension {
  public static final String KEY = "cableRegistration";
  List<Integer> versions;
  Integer maxVersion;
  byte[] publicKey;

  public CableRegistrationData() {
  }

  public static CableRegistrationData parseFromCbor(DataItem cborCableData) {
    CableRegistrationData cableData = new CableRegistrationData();

    Map cborMap = (Map) cborCableData;

    for (DataItem data : cborMap.getKeys()) {
      if (data instanceof UnicodeString) {
        switch (((UnicodeString) data).getString()) {
          case "version":
            cableData.versions = new ArrayList<>();
            cableData.versions.add(((UnsignedInteger) cborMap.get(data)).getValue().intValue());
            break;
          case "maxVersion":
            cableData.maxVersion = ((UnsignedInteger) cborMap.get(data)).getValue().intValue();
            break;
          case "authenticatorPublicKey":
            cableData.publicKey = ((ByteString) cborMap.get(data)).getBytes();
            break;
        }
      }
    }
    return cableData;
  }

  @Override
  public Type getType() {
    return Type.CABLE;
  }
}
