package com.google.webauthn.gaedemo.objects;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
            if ((getHashAlg() == null && other.hashAlg == null) || getHashAlg().equals(other.hashAlg)) {
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
