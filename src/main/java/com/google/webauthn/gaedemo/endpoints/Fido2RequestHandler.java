package com.google.webauthn.gaedemo.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.webauthn.gaedemo.crypto.Crypto;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.exceptions.WebAuthnException;
import com.google.webauthn.gaedemo.objects.AndroidSafetyNetAttestationStatement;
import com.google.webauthn.gaedemo.objects.AttestationObject;
import com.google.webauthn.gaedemo.objects.AuthenticatorAssertionResponse;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.EccKey;
import com.google.webauthn.gaedemo.objects.FidoU2fAttestationStatement;
import com.google.webauthn.gaedemo.objects.MakeCredentialOptions;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.objects.PublicKeyCredentialRequestOptions;
import com.google.webauthn.gaedemo.server.AndroidSafetyNetServer;
import com.google.webauthn.gaedemo.server.U2fServer;
import com.google.webauthn.gaedemo.storage.Credential;
import com.google.webauthn.gaedemo.storage.SessionData;

import com.google.webauthn.gaedemo.objects.PublicKeyCredentialRequestOptions;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import javax.servlet.ServletException;


/**
 * An endpoint class for handling FIDO2 requests.
 *
 * Google Cloud Endpoints generate APIs and client libraries from API backend, to simplify client
 * access to data from other applications.
 * https://cloud.google.com/endpoints/docs/frameworks/java/get-started-frameworks-java
 */
@Api(
  name = "fido2RequestHandler",
  version = "v1",
  scopes = {Constants.EMAIL_SCOPE, Constants.OPENID_SCOPE},
  clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID},
  audiences = {Constants.ANDROID_AUDIENCE},
  namespace = @ApiNamespace(
      ownerName = "gaedemo.webauthn.google.com", ownerDomain = "gaedemo.webauthn.google.com")
)
public class Fido2RequestHandler {

  @ApiMethod(name = "getRegistrationRequest", path="get/register")
  public List<String> getRegistrationRequest(User user) throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }

    MakeCredentialOptions options = new MakeCredentialOptions(
        user.getNickname(), Constants.APP_ID, Constants.APP_ID);
    SessionData session = new SessionData(options.challenge, Constants.APP_ID);
    session.save(user.getEmail());
    JsonObject sessionJson = session.getJsonObject();
    JsonObject optionsJson = options.getJsonObject();
    optionsJson.add("session", sessionJson);

    List<String> resultList = new ArrayList<String>();
    resultList.add(optionsJson.toString());
    return resultList;
  }

  @ApiMethod(name = "processRegistrationResponse")
  public List<String> processRegistrationResponse(
      @Named("responseData") String responseData, User user)
      throws OAuthRequestException, ResponseException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }

    Gson gson = new Gson();
    JsonElement element = gson.fromJson(responseData, JsonElement.class);
    JsonObject object = element.getAsJsonObject();
    String clientDataJSON = object.get("clientDataJSON").getAsString();
    String attestationObject = object.get("attestationObject").getAsString();

    AuthenticatorAttestationResponse attestation =
        new AuthenticatorAttestationResponse(clientDataJSON, attestationObject);

    // TODO
    String credentialId = BaseEncoding.base64Url().encode(
        attestation.getAttestationObject().getAuthenticatorData().getAttData().getCredentialId());
    String type = null;
    String session = null;
    PublicKeyCredential cred = new PublicKeyCredential(credentialId, type,
        BaseEncoding.base64Url().decode(credentialId), attestation);

    try {
      switch (cred.getAttestationType()) {
        case FIDOU2F:
          U2fServer.registerCredential(cred, user.getEmail(), session, Constants.APP_ID);
          break;
        case ANDROIDSAFETYNET:
          AndroidSafetyNetServer.registerCredential(
              cred, user.getEmail(), session, Constants.APP_ID);
          break;
        default:
          // This should never happen.
      }
    } catch (ServletException e) {
      // TODO
    }

    Credential credential = new Credential(cred);
    credential.save(user.getEmail());

    List<String> resultList = new ArrayList<String>();
    resultList.add(credential.toJson());
    return resultList;
  }

  @ApiMethod(name = "getSignRequest", path="get/sign")
  public List<String> getSignRequest(User user) throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }

    PublicKeyCredentialRequestOptions assertion =
        new PublicKeyCredentialRequestOptions(Constants.APP_ID);
    SessionData session = new SessionData(assertion.challenge, Constants.APP_ID);
    session.save(user.getEmail());

    assertion.populateAllowList(user.getEmail());
    JsonObject assertionJson = assertion.getJsonObject();
    JsonObject sessionJson = session.getJsonObject();
    assertionJson.add("session", sessionJson);

    List<String> resultList = new ArrayList<String>();
    resultList.add(assertionJson.toString());
    return resultList;
  }

  @ApiMethod(name = "processSignResponse")
  public List<String> processSignResponse(
      @Named("responseData") String responseData, User user)
      throws OAuthRequestException, ResponseException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }

    Gson gson = new Gson();
    JsonElement element = gson.fromJson(responseData, JsonElement.class);
    JsonObject object = element.getAsJsonObject();
    String clientDataJSON = object.get("clientDataJSON").getAsString();
    String authenticatorData = object.get("authenticatorData").getAsString();
    String credentialId = object.get("credentialId").getAsString();
    String signature = object.get("signature").getAsString();

    AuthenticatorAssertionResponse assertion =
        new AuthenticatorAssertionResponse(clientDataJSON, authenticatorData, signature);

    // TODO
    String type = null;
    String session = null;

    PublicKeyCredential cred = new PublicKeyCredential(credentialId, type,
        BaseEncoding.base64Url().decode(credentialId), assertion);

    try {
      U2fServer.verifyAssertion(cred, user.getEmail(), session);
    } catch (ServletException e) {
      // TODO
    }

    Credential credential = new Credential(cred);
    credential.save(user.getEmail());

    List<String> resultList = new ArrayList<String>();
    resultList.add(credential.toJson());
    return resultList;
  }

  @ApiMethod(name = "getAllSecurityKeys", path = "getAllSecurityKeys")
  public String[] getAllSecurityKeys(User user) throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }

    List<Credential> savedCreds = Credential.load(user.getEmail());
    JsonArray result = new JsonArray();

    for (Credential c : savedCreds) {
      JsonObject cJson = new JsonObject();
      cJson.addProperty("handle", BaseEncoding.base64().encode(c.getCredential().rawId));
      EccKey ecc = (EccKey) ((AuthenticatorAttestationResponse) c.getCredential().getResponse())
          .getAttestationObject().getAuthenticatorData().getAttData().getPublicKey();
      // TODO
/*
      try {
        cJson.addProperty("publicKey", Integer.toHexString(
            Crypto.decodePublicKey(ecc.getX(), ecc.getY()).hashCode()));
      } catch (WebAuthnException e) {
        e.printStackTrace();
        continue;
      }
*/
      AttestationObject attObj =
          ((AuthenticatorAttestationResponse) c.getCredential().getResponse())
              .getAttestationObject();
      if (attObj.getAttestationStatement() instanceof FidoU2fAttestationStatement) {
        cJson.addProperty("name", "FIDO U2F Authenticator");
      } else if (attObj.getAttestationStatement() instanceof AndroidSafetyNetAttestationStatement) {
        cJson.addProperty("name", "Android SafetyNet");
      }
      cJson.addProperty("date", c.getDate().toString());
      cJson.addProperty("id", c.id);
      result.add(cJson);
    }

    return new String[] {result.toString()};
  }

  @ApiMethod(name = "removeSecurityKey")
  public String[] removeSecurityKey(User user, @Named("publicKey") String publicKey)
      throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }

    // TODO
    String id = null;
    String credentialId = null;
    Credential.remove(user.getEmail(), id);
    Credential.remove(user.getEmail(), credentialId);

    return new String[] {"OK"};
  }
}
