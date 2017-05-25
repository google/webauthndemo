package com.google.webauthn.gaedemo.objects;

import co.nstant.in.cbor.CborException;
import com.google.common.primitives.Bytes;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 */
public class AttestationData {
  byte[] aaguid;
  byte[] credentialId;
  CredentialPublicKey publicKey;

  /**
   * 
   */
  public AttestationData() {
    aaguid = new byte[16];
    credentialId = new byte[] {};
    publicKey = new EccKey();
  }

  /**
   * @param aaguid
   * @param credentialId
   * @param publicKey
   */
  public AttestationData(byte[] aaguid, byte[] credentialId, CredentialPublicKey publicKey) {
    super();
    this.aaguid = aaguid;
    this.credentialId = credentialId;
    this.publicKey = publicKey;
  }

  /**
   * @param data
   * @return AttestationData object created from the byte sequence
   * @throws ResponseException
   * @throws CborException
   */
  public static AttestationData decode(byte[] data) throws ResponseException, CborException {
    AttestationData result = new AttestationData();
    int index = 0;
    if (data.length < 18)
      throw new ResponseException("Invalid input");
    System.arraycopy(data, 0, result.aaguid, 0, 16);
    index += 16;

    int length = data[index++] << 8;
    length |= data[index++];

    result.credentialId = new byte[length];
    System.arraycopy(data, index, result.credentialId, 0, length);
    index += length;

    byte[] cbor = new byte[data.length - index];
    System.arraycopy(data, index, cbor, 0, data.length - index);
    result.publicKey = CredentialPublicKey.decode(cbor);

    return result;
  }

  /**
   * @return Encoded byte array created from AttestationData object
   * @throws CborException
   */
  public byte[] encode() throws CborException {
    return Bytes.concat(aaguid,
        ByteBuffer.allocate(2).putShort((short) credentialId.length).array(), credentialId,
        publicKey.encode());
  }

  /**
   *
   */
  @Override
  public boolean equals(Object obj) {
    try {
      if (obj instanceof AttestationData) {
        AttestationData other = (AttestationData) obj;
        if (Arrays.equals(other.aaguid, aaguid) && Arrays.equals(credentialId, other.credentialId)
            && ((publicKey == null && other.publicKey == null)
                || (publicKey.equals(other.publicKey)))) {
          return true;
        }
      }
    } catch (NullPointerException e) {
      return false;
    }
    return false;
  }

  /**
   * @return the aaguid
   */
  public byte[] getAaguid() {
    return aaguid;
  }

  /**
   * @return the credentialId
   */
  public byte[] getCredentialId() {
    return credentialId;
  }

  /**
   * @return the publicKey
   */
  public CredentialPublicKey getPublicKey() {
    return publicKey;
  }
}
