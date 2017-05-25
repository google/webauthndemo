package com.google.webauthn.gaedemo.objects;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.googlecode.objectify.annotation.Subclass;
import java.util.Arrays;

@Subclass
public class AndroidSafetyNetAttestationStatement extends AttestationStatement {

  public String ver;
  public byte[] response;

  /**
   * @param ver
   * @param response
   */
  public AndroidSafetyNetAttestationStatement() {
    this.ver = null;
    this.response = null;
  }

  /**
   * @param attStmt
   * @return
   * @throws ResponseException
   */
  public static AndroidSafetyNetAttestationStatement decode(DataItem attStmt)
      throws ResponseException {
    AndroidSafetyNetAttestationStatement result = new AndroidSafetyNetAttestationStatement();
    Map given = (Map) attStmt;
    for (DataItem data : given.getKeys()) {
      if (data instanceof UnicodeString) {
        if (((UnicodeString) data).getString().equals("ver")) {
          UnicodeString version = (UnicodeString) given.get(data);
          result.ver = version.getString();
        } else if (((UnicodeString) data).getString().equals("response")) {
          result.response = ((ByteString) (given.get(data))).getBytes();
        }
      }
    }
    if (result.response == null || result.ver == null)
      throw new ResponseException("Invalid JWT Cbor");
    return result;
  }

  @Override
  DataItem encode() throws CborException {
    Map map = new Map();
    map.put(new UnicodeString("ver"), new UnicodeString(ver));
    map.put(new UnicodeString("response"), new ByteString(response));
    return map;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AndroidSafetyNetAttestationStatement) {
      AndroidSafetyNetAttestationStatement other = (AndroidSafetyNetAttestationStatement) obj;
      if (ver == other.ver || ((ver != null && other.ver != null) && ver.equals(other.ver))) {
        if (Arrays.equals(response, other.response)) {
          return true;
        }
      }
    }
    return false;
  }
}
