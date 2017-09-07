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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import co.nstant.in.cbor.CborException;
import com.google.common.primitives.Bytes;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import org.junit.Test;

public class AuthenticatorDataTest {
  private final SecureRandom random = new SecureRandom();

  /**
   * Test method for {@link com.google.webauthn.gaedemo.objects.AuthenticatorData#decode(byte[])}.
   */
  @Test
  public void testDecodeWithoutAttestation() {
    byte[] randomRpIdHash = new byte[32];
    random.nextBytes(randomRpIdHash);
    byte[] flags = {0};
    int countInt = random.nextInt(Integer.MAX_VALUE);
    byte[] count = ByteBuffer.allocate(4).putInt(countInt).array();
    byte[] data = Bytes.concat(randomRpIdHash, flags, count);

    try {
      AuthenticatorData result = AuthenticatorData.decode(data);

      assertArrayEquals(randomRpIdHash, result.getRpIdHash());
      assertEquals(countInt, result.getSignCount());
    } catch (ResponseException e) {
      fail("Exception occurred");
    }
  }

  /**
   * Test method for {@link com.google.webauthn.gaedemo.objects.AuthenticatorData#decode(byte[])}.
   */
  @Test
  public void testDecodeWithAttestation() {
    byte[] randomRpIdHash = new byte[32];
    random.nextBytes(randomRpIdHash);
    byte[] flags = {1 << 6};
    AttestationData attData = new AttestationData();
    random.nextBytes(attData.aaguid);
    int countInt = random.nextInt(Integer.MAX_VALUE);
    byte[] count = ByteBuffer.allocate(4).putInt(countInt).array();
    byte[] data = null;
    try {
      data = Bytes.concat(randomRpIdHash, flags, count, attData.encode());
    } catch (CborException e1) {
      fail("Failed during Cbor encoding");
    }

    try {
      AuthenticatorData result = AuthenticatorData.decode(data);
      assertArrayEquals(randomRpIdHash, result.getRpIdHash());
      assertEquals(countInt, result.getSignCount());
    } catch (ResponseException e) {
      fail("Exception occurred");
    }
  }

}
