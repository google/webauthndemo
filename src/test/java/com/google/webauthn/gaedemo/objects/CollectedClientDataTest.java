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
    one.hashAlg = "SHA-256";
    assertNotEquals(one, two);
    two.hashAlg = "SHA-256";
    assertEquals(one, two);
    one.origin = "https://google.com";
    assertNotEquals(one, two);
    two.origin = "https://google.com";
    assertEquals(one, two);
    one.tokenBinding = "test";
    assertNotEquals(one, two);
    two.tokenBinding = "test";
    assertEquals(one, two);
  }

  /**
   * Test method for {@link com.google.webauthn.gaedemo.objects.CollectedClientData#encode()}.
   */
  @Test
  public void testEncode() {
    CollectedClientData clientData = new CollectedClientData();
    clientData.challenge = "testChallenge";
    clientData.hashAlg = "SHA-1";
    clientData.origin = "testOrigin";
    CollectedClientData decoded = CollectedClientData.decode(clientData.encode());
    assertEquals(clientData, decoded);
  }
}
