package com.google.webauthn.gaedemo.objects;

import com.google.gson.JsonObject;

public class PublicKeyCredentialParameters {
  private PublicKeyCredentialType type;
  private String algorithm;
  
  /**
   * @param type
   * @param algorithm
   */
  public PublicKeyCredentialParameters(PublicKeyCredentialType type, String algorithm) {
    this.type = type;
    this.algorithm = algorithm;
  }

  /**
   * @return JsonObject representation of PublicKeyCredentialParameters
   */
  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.addProperty("type", type.toString());
    result.addProperty("algorithm", algorithm);
    
    return result;
  }
}
