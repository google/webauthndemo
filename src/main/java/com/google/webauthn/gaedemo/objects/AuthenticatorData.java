package com.google.webauthn.gaedemo.objects;

import co.nstant.in.cbor.CborException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class AuthenticatorData {
  private byte[] rpIdHash;
  private byte flags;
  private int signCount;
  // optional
  AttestationData attData;

  /**
   * @param rpIdHash
   * @param flags
   * @param signCount
   * @param attData
   */
  public AuthenticatorData(byte[] rpIdHash, byte flags, int signCount, AttestationData attData) { 
    this.rpIdHash = rpIdHash;
    this.flags = flags;
    this.signCount = signCount;
    this.attData = attData;
  }

  AuthenticatorData() {
    rpIdHash = new byte[32];
    attData = new AttestationData();
  }

  /**
   * @return the rpIdHash
   */
  public byte[] getRpIdHash() {
    return rpIdHash;
  }

  /**
   * @return the flags
   */
  public byte getFlags() {
    return flags;
  }

  /**
   * @return the signCount
   */
  public int getSignCount() {
    return signCount;
  }

  /**
   * @return the attData
   */
  public AttestationData getAttData() {
    return attData;
  }

  /**
   * @param authData
   * @return Decoded AuthenticatorData object
   * @throws ResponseException
   */
  public static AuthenticatorData decode(byte[] authData) throws ResponseException {
    if (authData.length < 37) {
      throw new ResponseException("Invalid input");
    }

    int index = 0;
    byte[] rpIdHash = new byte[32];
    System.arraycopy(authData, 0, rpIdHash, 0, 32);
    index += 32;
    byte flags = authData[index++];
    int signCount =
        Ints.fromBytes(authData[index++], authData[index++], authData[index++], authData[index++]);

    AttestationData attData = null;
    // Bit 6 determines whether attestation data was included
    if ((flags & 1 << 6) != 0) {
      byte[] remainder = new byte[authData.length - index];
      System.arraycopy(authData, index, remainder, 0, authData.length - index);
      try {
        attData = AttestationData.decode(remainder);
      } catch (CborException e) {
        throw new ResponseException("Error decoding");
      }
    }

    return new AuthenticatorData(rpIdHash, flags, signCount, attData);
  }

  /**
   * @return Encoded byte array
   * @throws CborException
   */
  @VisibleForTesting
  byte[] encode() throws CborException {
    byte[] flags = {this.flags};
    byte[] signCount = ByteBuffer.allocate(4).putInt(this.signCount).array();
    byte[] attData = this.attData.encode();
    byte[] result = Bytes.concat(rpIdHash, flags, signCount, attData);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    try {
      if (obj instanceof AuthenticatorData) {
        AuthenticatorData other = (AuthenticatorData) obj;
        if (other.flags == other.flags) {
          if (Arrays.equals(other.rpIdHash, rpIdHash)) {
            if (signCount == other.signCount) {
              if (attData == null && other.attData == null) {
                return true;
              }
              if (attData.equals(other.attData)) {
                return true;
              }
            }
          }
        }
      }
    } catch (NullPointerException e) {
      return false;
    }
    return false;
  }

}
