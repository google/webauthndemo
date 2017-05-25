package com.google.webauthn.gaedemo.objects;

import co.nstant.in.cbor.CborException;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.googlecode.objectify.annotation.Subclass;

/**
 *
 */
@Subclass
public class AuthenticatorAttestationResponse extends AuthenticatorResponse {
  /**
   *
   */
  private class AttestationResponseJson {
    String clientDataJSON;
    String attestationObject;
  }

  public AttestationObject decodedObject;

  /**
   * 
   */
  public AuthenticatorAttestationResponse() {}

  /**
   * @param data
   * @throws ResponseException
   */
  public AuthenticatorAttestationResponse(String data) throws ResponseException {
    Gson gson = new Gson();
    AttestationResponseJson parsedObject = gson.fromJson(data, AttestationResponseJson.class);

    String clientDataString =
        new String(BaseEncoding.base64Url().decode(parsedObject.clientDataJSON));
    clientData = gson.fromJson(clientDataString, CollectedClientData.class);

    byte[] attestationObject = BaseEncoding.base64().decode(parsedObject.attestationObject);

    try {
      decodedObject = AttestationObject.decode(attestationObject);
    } catch (CborException e) {
      throw new ResponseException("Cannot decode");
    }
  }

  /**
   * @return json encoded representation of the AuthenticatorAttestationResponse
   */
  public String encode() {
    JsonObject json = new JsonObject();
    json.addProperty("clientDataJSON",
        BaseEncoding.base64().encode(getClientData().encode().getBytes()));
    try {
      json.addProperty("attestationObject", BaseEncoding.base64().encode(decodedObject.encode()));
    } catch (CborException e) {
      return null;
    }
    return json.getAsString();
  }

  /**
   * @return the decodedObject
   */
  public AttestationObject getAttestationObject() {
    return decodedObject;
  }
}
