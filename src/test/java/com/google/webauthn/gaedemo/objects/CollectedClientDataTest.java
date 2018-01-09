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

import org.junit.Test;

public class CollectedClientDataTest {
  /**
   * Test method for {@link com.google.webauthn.gaedemo.objects.CollectedClientData#equals(Object)}.
   */
  @Test
  public void testEquals() {
    CollectedClientData one = new CollectedClientData();
    CollectedClientData two = new CollectedClientData();
    assertEquals(one, two);
    one.challenge = "challenge";
    assertNotEquals(one, two);
    two.challenge = "challenge";
    assertEquals(one, two);
    one.hashAlgorithm = "SHA-256";
    assertNotEquals(one, two);
    two.hashAlgorithm = "SHA-256";
    assertEquals(one, two);
    one.origin = "https://google.com";
    assertNotEquals(one, two);
    two.origin = "https://google.com";
    assertEquals(one, two);
    one.tokenBindingId = "test";
    assertNotEquals(one, two);
    two.tokenBindingId = "test";
    assertEquals(one, two);
  }

  /**
   * Test method for {@link com.google.webauthn.gaedemo.objects.CollectedClientData#encode()}.
   */
  @Test
  public void testEncode() {
    CollectedClientData clientData = new CollectedClientData();
    clientData.challenge = "testChallenge";
    clientData.hashAlgorithm = "SHA-1";
    clientData.origin = "testOrigin";
    CollectedClientData decoded = CollectedClientData.decode(clientData.encode());
    assertEquals(clientData, decoded);
  }
}
