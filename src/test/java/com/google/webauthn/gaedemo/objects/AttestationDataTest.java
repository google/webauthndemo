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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import co.nstant.in.cbor.CborException;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import java.util.Random;
import org.junit.Test;

public class AttestationDataTest {

  /**
   * Test method for {@link com.google.webauthn.gaedemo.objects.AttestationData#encode()}.
   */
  @Test
  public void testEncode() {
    AttestationData attData = new AttestationData();
    Random rand = new Random();
    rand.nextBytes(attData.aaguid);
    attData.credentialId = "testCredentialId".getBytes();
    RsaKey publicKey = new RsaKey();
    publicKey.alg = Algorithm.RS256;
    publicKey.e = "e".getBytes();
    publicKey.n = "n".getBytes();
    attData.publicKey = publicKey;
    AttestationData decoded;
    try {
      decoded = AttestationData.decode(attData.encode());
      assertEquals(decoded, attData);
    } catch (ResponseException e) {
      fail(e.toString());
    } catch (CborException e) {
      e.printStackTrace();
      fail(e.toString());
    }
  }

  /**
   * Test method for
   * {@link com.google.webauthn.gaedemo.objects.AttestationData#equals(java.lang.Object)}.
   */
  @Test
  public void testEquals() {
    AttestationData one = new AttestationData();
    AttestationData two = new AttestationData();
    assertEquals(one, two);
    one.aaguid = "aaguid".getBytes();
    assertNotEquals(one, two);
    two.aaguid = "aaguid".getBytes();
    assertEquals(one, two);
    one.credentialId = "credentialId".getBytes();
    assertNotEquals(one, two);
    two.credentialId = "credentialId".getBytes();
    assertEquals(one, two);
    one.publicKey = new RsaKey();
    assertNotEquals(one, two);
    two.publicKey = new RsaKey();
    assertEquals(one, two);
  }

}
