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
import static org.junit.jupiter.api.Assertions.fail;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

class AndroidSafetyNetAttestationStatementTest {

  /**
   * Test method for
   * {@link com.google.webauthn.gaedemo.objects.AndroidSafetyNetAttestationStatement#encode()} and
   * {@link com.google.webauthn.gaedemo.objects.AndroidSafetyNetAttestationStatement#decode(co.nstant.in.cbor.model.DataItem)}.
   */
  @Test
  void testEncode() {
    SecureRandom random = new SecureRandom();
    AndroidSafetyNetAttestationStatement attStmt = new AndroidSafetyNetAttestationStatement();
    attStmt.ver = "10";
    attStmt.response = new byte[20];
    random.nextBytes(attStmt.response);

    try {
      DataItem encoded = attStmt.encode();
      AndroidSafetyNetAttestationStatement decoded =
          AndroidSafetyNetAttestationStatement.decode(encoded);
      assertEquals(decoded, attStmt);
    } catch (CborException | ResponseException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.google.webauthn.gaedemo.objects.AndroidSafetyNetAttestationStatement#equals(java.lang.Object)}.
   */
  @Test
  void testEqualsObject() {
    AndroidSafetyNetAttestationStatement a = new AndroidSafetyNetAttestationStatement();
    AndroidSafetyNetAttestationStatement b = new AndroidSafetyNetAttestationStatement();
    assertEquals(a, b);
    a.ver = "testString";
    assertNotEquals(a, b);
    b.ver = "testString";
    assertEquals(a, b);
    a.response = new byte[] {0, 1, 2, 3};
    assertNotEquals(a, b);
    b.response = new byte[] {0, 1, 2, 3};
    assertEquals(a, b);
  }

}
