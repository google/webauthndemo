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
  public static final String WEB_CLIENT_ID =
      "294011350698-br3h7bjbe640h1mnq8thnkkf6rc885t1.apps.googleusercontent.com";
  public static final String ANDROID_CLIENT_ID =
      "294011350698-79m03dcrj4dqom8fpe04j7rla2c7u285.apps.googleusercontent.com";
  public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;

  public static final String APP_ID = "webauthndemo-176619.appspot.com";
}
