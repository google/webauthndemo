package com.google.webauthn.gaedemo.objects;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;
import com.google.webauthn.gaedemo.exceptions.ResponseException;

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
    } else if (fmt.equals("android-safetynet")) {
      AndroidSafetyNetAttestationStatement stmt;
      try {
        stmt = AndroidSafetyNetAttestationStatement.decode(attStmt);
      } catch (ResponseException e) {
        return null;
      }
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
