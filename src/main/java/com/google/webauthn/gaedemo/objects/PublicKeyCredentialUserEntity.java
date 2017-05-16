package com.google.webauthn.gaedemo.objects;

import com.google.gson.JsonObject;

public class PublicKeyCredentialUserEntity {
  private String displayName;

  /**
   * @param displayName
   */
  public PublicKeyCredentialUserEntity(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @return
   */
  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.addProperty("displayName", displayName);

    return result;
  }
}
