package com.google.webauthn.gaedemo.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import co.nstant.in.cbor.CborException;
import java.security.SecureRandom;
import java.util.ArrayList;
import org.junit.Test;

public class FidoU2fAttestationStatementTest {
  final SecureRandom rand = new SecureRandom();

  @Test
  public void testEncode() {
    FidoU2fAttestationStatement attStmt = new FidoU2fAttestationStatement();
    attStmt.attestnCert = new byte[32];
    rand.nextBytes(attStmt.attestnCert);
    attStmt.sig = new byte[64];
    rand.nextBytes(attStmt.sig);
    attStmt.caCert = new ArrayList<byte[]>();
    for (int i = 0; i < 5; i++) {
      byte[] cert = new byte[32];
      rand.nextBytes(cert);
      attStmt.caCert.add(cert);
    }
    try {
      FidoU2fAttestationStatement decoded = FidoU2fAttestationStatement.decode(attStmt.encode());
      assertEquals(decoded, attStmt);
    } catch (CborException e) {
      fail(e.toString());
    }
  }

}
