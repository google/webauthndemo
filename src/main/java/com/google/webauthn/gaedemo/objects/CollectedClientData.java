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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.webauthn.gaedemo.crypto.Crypto;
import java.security.NoSuchAlgorithmException;

public class CollectedClientData {
  String challenge;
  String origin;
  String hashAlg;
  String tokenBinding;

  CollectedClientData() {
    challenge = null;
    origin = null;
    hashAlg = null;
    tokenBinding = null;
  }

  /**
   * @param json
   * @return Decoded CollectedClientData object
   */
  public static CollectedClientData decode(String json) {
    Gson gson = new Gson();
    try {
      return gson.fromJson(json, CollectedClientData.class);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }

  /**
   * @return json encoded representation of CollectedClientData
   */
  public String encode() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  public byte[] getHash() {
    String json = encode();
    try {
      return Crypto.digest(json.getBytes(), hashAlg);
    } catch (NoSuchAlgorithmException e) {
      return Crypto.sha256Digest(json.getBytes());
    }
  }

  /**
   * @return the challenge
   */
  public String getChallenge() {
    return challenge;
  }

  /**
   * @return the origin
   */
  public String getOrigin() {
    return origin;
  }

  /**
   * @return the hashAlg
   */
  public String getHashAlg() {
    return hashAlg;
  }

  /**
   * @return the tokenBinding
   */
  public String getTokenBinding() {
    return tokenBinding;
  }

  @Override
  public boolean equals(Object obj) {
    try {
      if (obj instanceof CollectedClientData) {
        CollectedClientData other = (CollectedClientData) obj;
        if ((getChallenge() == null && other.challenge == null)
            || getChallenge().equals(other.challenge)) {
          if ((getOrigin() == null && other.origin == null) || getOrigin().equals(other.origin)) {
            if ((getHashAlg() == null && other.hashAlg == null)
                || getHashAlg().equals(other.hashAlg)) {
              if ((getTokenBinding() == null && other.tokenBinding == null)
                  || (getTokenBinding().equals(other.tokenBinding))) {
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
