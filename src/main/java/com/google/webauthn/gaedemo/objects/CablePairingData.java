/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.google.webauthn.gaedemo.objects;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;

import org.bouncycastle.util.Arrays;

import com.google.common.primitives.Bytes;
import com.google.webauthn.gaedemo.crypto.Crypto;

public class CablePairingData {
  public int version;
  public byte[] irk;
  public byte[] lk;
  private static int HKDF_SHA_LENGTH = 64;
  private static int K_LENGTH = 32;

  public CablePairingData(int version, byte[] irk, byte[] lk) {
    this.version = version;
    this.irk = irk;
    this.lk = lk;
  }

  public CablePairingData() {}

  public static CablePairingData generatePairingData(CableRegistrationData cableData,
      KeyPair sessionKeyPair) {
    byte[] sharedSecret = Crypto.getS(sessionKeyPair.getPrivate(), cableData.publicKey);

    byte[] info = "FIDO caBLE v1 pairing data".getBytes(StandardCharsets.US_ASCII);
    byte[] version = ByteBuffer.allocate(4).putInt(cableData.versions.get(0)).array();

    byte[] result = Crypto.hkdfSha256(sharedSecret, Crypto.sha256Digest(Bytes.concat(version,
        Crypto.compressECPublicKey((ECPublicKey) sessionKeyPair.getPublic()), cableData.publicKey)),
        info, HKDF_SHA_LENGTH);

    return new CablePairingData(cableData.versions.get(0), Arrays.copyOf(result, K_LENGTH),
        Arrays.copyOfRange(result, K_LENGTH, 2 * K_LENGTH));
  }
}
