package com.google.webauthn.gaedemo.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.storage.Attachment;
import java.security.SecureRandom;
import java.util.ArrayList;
import com.google.common.io.BaseEncoding;

public class MakeCredentialOptions {
  private static final int CHALLENGE_LENGTH = 32;
  private final SecureRandom random = new SecureRandom();
  
  /**
   * 
   */
  public MakeCredentialOptions() {
    parameters = new ArrayList<PublicKeyCredentialParameters>();
    excludeList = new ArrayList<PublicKeyCredentialDescriptor>();
  }

  /**
   * @param userId
   * @param rpId
   * @param rpName 
   */
  public MakeCredentialOptions(String userId, String rpId, String rpName) {
    parameters = new ArrayList<PublicKeyCredentialParameters>();
    excludeList = new ArrayList<PublicKeyCredentialDescriptor>();
    rp = new PublicKeyCredentialEntity(rpId, rpName, "");
    user = new PublicKeyCredentialUserEntity(userId);
    
    challenge = new byte[CHALLENGE_LENGTH];
    random.nextBytes(challenge);
    parameters.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, "none"));
  }

  PublicKeyCredentialEntity rp;
  PublicKeyCredentialUserEntity user;

  public byte[] challenge;
  ArrayList<PublicKeyCredentialParameters> parameters;
  long timeout;
  ArrayList<PublicKeyCredentialDescriptor> excludeList;
  Attachment attachment;

  /**
   * @return Encoded JsonObect representation of MakeCredentialOptions
   */
  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.add("rp", rp.getJsonObject());
    result.add("user", user.getJsonObject());
    result.addProperty("challenge", BaseEncoding.base64().encode(challenge));
    if (parameters.size() > 0) {
      JsonArray params = new JsonArray();
      for (PublicKeyCredentialParameters param : parameters) {
        params.add(param.getJsonObject());
      }
      result.add("parameters", params);
    }
    if (timeout > 0) {
      result.addProperty("timeout", timeout);
    }
    if (excludeList.size() > 0) {
      JsonArray params = new JsonArray();
      for (PublicKeyCredentialDescriptor descriptor : excludeList) {
        params.add(descriptor.getJsonObject());
      }
      result.add("excludeList", params);
    }
    if (attachment != null) {
      result.addProperty("attachment", attachment.toString());
    }
    return result;
  }
}
