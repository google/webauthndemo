package com.google.webauthn.gaedemo.objects;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;

public abstract class AttestationStatement {

  /**
   * @param fmt
   * @param attStmt
   * @return Attestation statement of provided format
   */
  public static AttestationStatement decode(String fmt, DataItem attStmt) {
    if (fmt.equals("fido-u2f")) {
      FidoU2fAttestationStatement stmt = FidoU2fAttestationStatement.decode(attStmt);
      return stmt;
    }

    return null;
  }

  /**
   * @return Encoded AttestationStatement
   * @throws CborException
   */
  abstract DataItem encode() throws CborException;


}
