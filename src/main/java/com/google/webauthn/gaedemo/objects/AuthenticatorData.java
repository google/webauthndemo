// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.webauthn.gaedemo.objects;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.webauthn.gaedemo.exceptions.ResponseException;

import co.nstant.in.cbor.CborException;

public class AuthenticatorData {
  private byte[] rpIdHash;
  private byte flags;
  private int signCount;
  // optional
  AttestationData attData;
  private byte[] extensions;

  /**
   * @param rpIdHash
   * @param flags
   * @param signCount
   * @param attData
   */
  public AuthenticatorData(byte[] rpIdHash, byte flags, int signCount,
		  AttestationData attData, byte[] extensions) {
    this.rpIdHash = rpIdHash;
    this.flags = flags;
    this.signCount = signCount;
    this.attData = attData;
    this.extensions = extensions;
  }

  AuthenticatorData() {
    rpIdHash = new byte[32];
    attData = new AttestationData();
    this.extensions = null;
  }


  AuthenticatorData(byte[] rpIdHash, byte flags, int signCount) {
    this.rpIdHash = rpIdHash;
    this.flags = flags;
    this.signCount = signCount;
    this.attData = null;
    this.extensions = null;
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
   * @return the UP bit of the flags
   */
  public boolean isUP() {
    return (flags & 1) != 0;
  }

  /**
   * @return the UP bit of the flags
   */
  public boolean isUV() {
    return (flags & 1 << 2) != 0;
  }

  /**
   * @return the UP bit of the flags
   */
  public boolean hasAttestationData() {
    return (flags & 1 << 6) != 0;
  }

  /**
   * @return the UP bit of the flags
   */
  public boolean hasExtensionData() {
    return (flags & 1 << 7) != 0;
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

    int definedIndex = index;

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

    byte[] extensions = null;
    // Bit 7 determines whether extensions are included.
    if ((flags & 1 << 7) != 0) {
      try {
        int start = definedIndex + attData.encode().length;
        if (authData.length > start) {
          byte[] remainder = new byte[authData.length - start];
          System.arraycopy(authData, start, remainder, 0, authData.length - start);
          extensions = remainder;
        }
      } catch (CborException e) {
        throw new ResponseException("Error decoding authenticator extensions");
      }
    }

    return new AuthenticatorData(rpIdHash, flags, signCount, attData, extensions);
  }

  /**
   * @return Encoded byte array
   * @throws CborException
   */
  public byte[] encode() throws CborException {
    byte[] flags = {this.flags};
    byte[] signCount = ByteBuffer.allocate(4).putInt(this.signCount).array();
    byte[] result;
    if (this.attData != null) {
      byte[] attData = this.attData.encode();
      result = Bytes.concat(rpIdHash, flags, signCount, attData);
    } else {
      result = Bytes.concat(rpIdHash, flags, signCount);
    }
    if (this.extensions != null) {
      result = Bytes.concat(result, extensions);
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    try {
      if (obj instanceof AuthenticatorData) {
        AuthenticatorData other = (AuthenticatorData) obj;
        if (flags == other.flags) {
          if (Arrays.equals(other.rpIdHash, rpIdHash)) {
            if (Integer.compareUnsigned(signCount, other.signCount) == 0) {
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
