/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.webauthn.gaedemo.crypto;

import com.google.webauthn.gaedemo.objects.CablePairingData;
import com.google.webauthn.gaedemo.objects.CableSessionData;
import org.bouncycastle.util.Arrays;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Random;

public class Cable {

  private static final byte[] HMAC_TAG_CLIENT_EID =
      "client".getBytes(StandardCharsets.UTF_8);
  private static final byte[] HMAC_TAG_AUTHENTICATOR_EID =
      "authenticator".getBytes(StandardCharsets.UTF_8);
  private static final byte[] HKDF_INFO_SESSION_PRE_KEY =
      "FIDO caBLE v1 sessionPreKey".getBytes(StandardCharsets.UTF_8);

  private final Random random;

  public Cable() {
    this(new SecureRandom());
  }

  Cable(Random random) {
    this.random = random;
  }

  public CableSessionData generateSessionData(CablePairingData pairingData) {
    byte[] nonce = new byte[8];
    random.nextBytes(nonce);

    byte[] clientEidHash = Crypto.hmacSha256(pairingData.irk,
        Arrays.concatenate(nonce, HMAC_TAG_CLIENT_EID), 8);
    byte[] clientEid = Arrays.concatenate(nonce, clientEidHash);

    byte[] authenticatorEid = Crypto.hmacSha256(pairingData.irk,
        Arrays.concatenate(clientEid, HMAC_TAG_AUTHENTICATOR_EID), 16);

    byte[] sessionPreKey = Crypto.hkdfSha256(pairingData.lk, nonce,
        HKDF_INFO_SESSION_PRE_KEY, 32);

    return new CableSessionData(pairingData.version, clientEid, authenticatorEid, sessionPreKey);
  }
}
