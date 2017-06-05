/*
 * Copyright 2017 Google Inc. All Rights Reserved.
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

package com.google.webauthn.gaedemo.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import co.nstant.in.cbor.CborException;
import org.junit.Test;

public class CredentialPublicKeyTest {

  /**
   * Test method for {@link com.google.webauthn.gaedemo.objects.CredentialPublicKey#decode(byte[])}.
   */
  @Test
  public void testDecode() {
    EccKey eccKey = new EccKey();
    eccKey.alg = Algorithm.decode("ES256");
    eccKey.x = new byte[] {0, 1, 2, 3};
    eccKey.y = new byte[] {0, 2, 4, 6};
    RsaKey rsaKey = new RsaKey();
    rsaKey.alg = Algorithm.decode("PS512");
    rsaKey.e = new byte[] {0, 1, 2, 3};
    rsaKey.n = new byte[] {0, 2, 4, 6};
    try {
      CredentialPublicKey ecc = CredentialPublicKey.decode(eccKey.encode());
      CredentialPublicKey rsa = CredentialPublicKey.decode(rsaKey.encode());
      assertTrue(ecc instanceof EccKey);
      assertEquals(ecc, eccKey);
      assertTrue(rsa instanceof RsaKey);
      assertEquals(rsa, rsaKey);
    } catch (CborException e) {
      fail(e.toString());
    }
  }

}
