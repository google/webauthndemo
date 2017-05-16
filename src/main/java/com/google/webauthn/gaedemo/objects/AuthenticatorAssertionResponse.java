package com.google.webauthn.gaedemo.objects;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.webauthn.gaedemo.exceptions.ResponseException;

public class AuthenticatorAssertionResponse extends AuthenticatorResponse {
  @SuppressWarnings("hiding")
  class AssertionResponseJson {
    String clientDataJSON;
    String authenticatorData;
    String signature;
  }

  AuthenticatorData authData;
  byte[] signature;

  /**
   * @param data
   * @throws ResponseException
   */
  public AuthenticatorAssertionResponse(String data) throws ResponseException {
    Gson gson = new Gson();
    try {
      AssertionResponseJson parsedObject = gson.fromJson(data, AssertionResponseJson.class);

      String clientDataString =
          new String(BaseEncoding.base64().decode(parsedObject.clientDataJSON));
      clientData = CollectedClientData.decode(clientDataString);
      authData =
          AuthenticatorData.decode(BaseEncoding.base64().decode(parsedObject.authenticatorData));
      signature = BaseEncoding.base64().decode(parsedObject.signature);
    } catch (JsonSyntaxException e) {
      throw new ResponseException("Response format incorrect");
    }
  }

  /**
   * @return the authData
   */
  public AuthenticatorData getAuthenticatorData() {
    return authData;
  }

  /**
   * @return the signature
   */
  public byte[] getSignature() {
    return signature;
  }
}
