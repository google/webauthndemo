package com.google.webauthn.gaedemo.objects;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;

public class PublicKeyCredentialDescriptor {
  /**
   * @param type
   * @param id
   */
  public PublicKeyCredentialDescriptor(PublicKeyCredentialType type, byte[] id) {
    this.type = type;
    this.id = id;
    this.transports = new ArrayList<Transport>();
  }

  /**
   * @param type
   * @param id
   * @param transports
   */
  public PublicKeyCredentialDescriptor(PublicKeyCredentialType type, byte[] id,
      ArrayList<Transport> transports) {
    this.type = type;
    this.id = id;
    this.transports = transports;
  }

  private PublicKeyCredentialType type;
  private byte[] id;
  private ArrayList<Transport> transports;

  /**
   * @return Encoded JsonObject representation of PublicKeyCredentialDescriptor
   */
  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.addProperty("type", type.toString());
    
    result.addProperty("id", BaseEncoding.base64().encode(id));
    JsonArray transports = new JsonArray();
    for (Transport t : this.transports) {
      JsonPrimitive element = new JsonPrimitive(t.toString());
      transports.add(element);
    }
    if (transports.size() > 0)
      result.add("transports", transports);
    
    return result;
  }
}
