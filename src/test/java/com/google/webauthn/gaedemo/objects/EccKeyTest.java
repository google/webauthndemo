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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import co.nstant.in.cbor.CborException;
import org.junit.Test;

public class EccKeyTest {

  @Test
  public void testEquals() {
    EccKey one = new EccKey();
    EccKey two = new EccKey();
    assertEquals(one, two);
    one.x = new byte[] {0, 1, 2};
    assertNotEquals(one, two);
    two.x = new byte[] {0, 1, 2};
    assertEquals(one, two);
    one.y = one.x;
    assertNotEquals(one, two);
    two.y = one.x;
    assertEquals(one, two);
    one.alg = Algorithm.ES256;
    assertNotEquals(one, two);
    two.alg = Algorithm.ES256;
    assertEquals(one, two);
    one.alg = null;
    assertNotEquals(one, two);
    two.alg = null;
    assertEquals(one, two);

    CredentialPublicKey three = new RsaKey();
    CredentialPublicKey four = new EccKey();
    assertNotEquals(four, three);
  }

  //@Test
  public void testEncode() {
    EccKey testKey = new EccKey();
    testKey.alg = Algorithm.ES256;
    testKey.x = "testX".getBytes();
    testKey.y = "testY".getBytes();
    try {
      CredentialPublicKey decodedCpk = CredentialPublicKey.decode(testKey.encode());
      assertTrue(decodedCpk instanceof EccKey);
      assertEquals(decodedCpk, testKey);
      testKey.alg = Algorithm.PS256;
      decodedCpk = CredentialPublicKey.decode(testKey.encode());
      assertTrue(!(decodedCpk instanceof EccKey));
    } catch (CborException e) {
      fail("CborException: " + e.getMessage());
    }
  }

}
