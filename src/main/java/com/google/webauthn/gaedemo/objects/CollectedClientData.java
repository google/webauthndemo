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

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class CollectedClientData {
  String type;
  String challenge;
  String origin;
  String hashAlgorithm;
  String tokenBindingId;
  AuthenticationExtensions clientExtensions;
  AuthenticationExtensions authenticatorExtensions;


  CollectedClientData() {}

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
      return Crypto.digest(json.getBytes(StandardCharsets.UTF_8), hashAlgorithm);
    } catch (NoSuchAlgorithmException e) {
      return Crypto.sha256Digest(json.getBytes(StandardCharsets.UTF_8));
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
    return hashAlgorithm;
  }

  /**
   * @return the tokenBinding
   */
  public String getTokenBinding() {
    return tokenBindingId;
  }

  public String getType() {
    return type;
  }

  public AuthenticationExtensions getClientExtensions() {
    return clientExtensions;
  }

  public AuthenticationExtensions getAuthenticatorExtensions() {
    return authenticatorExtensions;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, challenge, origin, hashAlgorithm, tokenBindingId, clientExtensions,
        authenticatorExtensions);
  }

  @Override
  public boolean equals(Object obj) {
    try {
      if (!(obj instanceof CollectedClientData)) {
        return false;
      }
      CollectedClientData other = (CollectedClientData) obj;
      if (!getChallenge().equals(other.challenge)) {
        return false;
      }
      if (!getOrigin().equals(other.origin)) {
        return false;
      }
      if (!getHashAlg().equals(other.hashAlgorithm)) {
        return false;
      }
      if (!getTokenBinding().equals(other.tokenBindingId)) {
        return false;
      }
      if (!getType().equals(other.type)) {
        return false;
      }
      if (!getClientExtensions().equals(other.clientExtensions)) {
        return false;
      }
      if (getAuthenticatorExtensions().equals(other.authenticatorExtensions)) {
        return false;
      }
      return true;
    } catch (NullPointerException e) {
      return false;
    }
  }
}
