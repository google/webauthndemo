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
