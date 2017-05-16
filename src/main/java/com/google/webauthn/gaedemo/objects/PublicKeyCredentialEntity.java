package com.google.webauthn.gaedemo.objects;

import com.google.gson.JsonObject;

public class PublicKeyCredentialEntity {
  private String id;
  private String name;
  private String icon;

  /**
   * @param id
   * @param name
   * @param icon
   */
  PublicKeyCredentialEntity(String id, String name, String icon) {
    this.id = id;
    this.name = name;
    this.icon = icon;
  }

  /**
   * @return Encoded JsonObject representation of PublicKeyCredentialEntity
   */
  JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.addProperty("id", id);
    result.addProperty("name", name);
    result.addProperty("icon", icon);

    return result;
  }
}
