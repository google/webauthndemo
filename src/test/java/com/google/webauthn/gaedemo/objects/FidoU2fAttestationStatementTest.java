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
import static org.junit.Assert.fail;

import co.nstant.in.cbor.CborException;
import java.security.SecureRandom;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("unused")
public class FidoU2fAttestationStatementTest {
  final SecureRandom rand = new SecureRandom();

  @Test
  @Ignore
  public void testEncode() throws CborException {
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
    FidoU2fAttestationStatement decoded = FidoU2fAttestationStatement.decode(attStmt.encode());
    assertEquals(decoded, attStmt);
  }

}
