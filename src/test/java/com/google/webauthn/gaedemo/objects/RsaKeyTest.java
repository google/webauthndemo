package com.google.webauthn.gaedemo.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import co.nstant.in.cbor.CborException;
import org.junit.Test;

public class RsaKeyTest {

  @Test
  public void testEquals() {
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
  public void testEncode() {
    RsaKey rsaKey = new RsaKey();
    rsaKey.alg = Algorithm.PS256;
    rsaKey.e = "e".getBytes();
    rsaKey.n = "n".getBytes();
    
    try {
      CredentialPublicKey decodedCpk = CredentialPublicKey.decode(rsaKey.encode());
      assertTrue(decodedCpk instanceof RsaKey);
      assertEquals(decodedCpk, rsaKey);
      rsaKey.alg = Algorithm.ES256;
      decodedCpk = CredentialPublicKey.decode(rsaKey.encode());
      assertTrue(!(decodedCpk instanceof RsaKey));
    } catch (CborException e) {
      fail("CborException: " + e.getMessage());
    }
  }
}
