/*
 * Copyright 2017 Google Inc. All Rights Reserved.
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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import co.nstant.in.cbor.CborException;
import org.junit.jupiter.api.Test;

class RsaKeyTest {

  @Test
  void testEquals() {
    RsaKey one = new RsaKey();
    RsaKey two = new RsaKey();
    assertEquals(one, two);
    one.alg = Algorithm.UNDEFINED;
    two.alg = null;
    assertNotEquals(one, two);
    two.alg = Algorithm.UNDEFINED;
    one.e = new byte[] {0, 1, 2};
    assertNotEquals(one, two);
    two.e = new byte[] {0, 1, 2};
    assertEquals(one, two);
    one.n = new byte[] {2, 1, 0};
    assertNotEquals(one, two);
    two.n = new byte[] {2, 1, 0};
    assertEquals(one, two);

    CredentialPublicKey three = new RsaKey();
    CredentialPublicKey four = new EccKey();
    assertNotEquals(four, three);
  }

  @Test
  void testEncode() {
    RsaKey rsaKey = new RsaKey();
    rsaKey.alg = Algorithm.PS256;
    rsaKey.e = "e".getBytes();
    rsaKey.n = "n".getBytes();

    try {
      CredentialPublicKey decodedCpk = CredentialPublicKey.decode(rsaKey.encode());
      assertTrue(decodedCpk instanceof RsaKey);
      assertEquals(decodedCpk, rsaKey);
    } catch (CborException e) {
      fail("CborException: " + e.getMessage());
    }
  }
}
