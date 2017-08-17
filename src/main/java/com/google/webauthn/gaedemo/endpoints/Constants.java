package com.google.webauthn.gaedemo.endpoints;

/**
 * Contains the client IDs and scopes for allowed clients consuming FIDO2 API.
 */
public class Constants {

  // Scopes: https://developers.google.com/identity/protocols/googlescopes
  public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
  public static final String OPENID_SCOPE = "openid";

  // TODO: configure Client IDs for android and web clients.
  // ClientIds:
  // https://cloud.google.com/endpoints/docs/frameworks/java/creating-client-ids
  public static final String WEB_CLIENT_ID = "";
  public static final String ANDROID_CLIENT_ID = "";
  public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;
}
