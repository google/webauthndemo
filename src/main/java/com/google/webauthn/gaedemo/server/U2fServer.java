package com.google.webauthn.gaedemo.server;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.webauthn.gaedemo.crypto.Crypto;
import com.google.webauthn.gaedemo.exceptions.WebAuthnException;
import com.google.webauthn.gaedemo.objects.AuthenticatorAssertionResponse;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.CollectedClientData;
import com.google.webauthn.gaedemo.objects.EccKey;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.storage.AssertionSessionData;
import com.google.webauthn.gaedemo.storage.Credential;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;

public class U2fServer {

  private static final Logger Log = Logger.getLogger(U2fServer.class.getName());

  /**
   * @param cred
   * @param currentUser
   * @param sessionId
   * @throws ServletException
   */
  public static void verifyAssertion(PublicKeyCredential cred, String currentUser, String sessionId)
      throws ServletException {

    if (!(cred.getResponse() instanceof AuthenticatorAssertionResponse)) {
      throw new ServletException("Invalid authenticator response");
    }

    AuthenticatorAssertionResponse assertionResponse =
        (AuthenticatorAssertionResponse) cred.getResponse();

    Log.info("-- Verifying provided session and challenge data --");
    List<Credential> savedCreds = Credential.load(currentUser);
    if (savedCreds == null || savedCreds.size() == 0) {
      throw new ServletException("No credentials registered for this user");
    }

    long id = 0;
    try {
      id = Long.valueOf(sessionId);
    } catch (NumberFormatException e) {
      throw new ServletException("Provided session id invalid");
    }

    AssertionSessionData session = AssertionSessionData.load(currentUser, Long.valueOf(id));
    if (session == null) {
      throw new ServletException("Session invalid");
    }

    byte[] sessionChallenge = BaseEncoding.base64().decode(session.getChallenge());
    if (!Arrays.equals(sessionChallenge,
        assertionResponse.getClientData().getChallenge().getBytes())) {
      throw new ServletException("Returned challenge incorrect");
    }
    Log.info("Successfully verified session and challenge data");

    Credential credential = null;
    for (Credential saved : savedCreds) {
      if (saved.getCredential().getId().equals(cred.getId())) {
        credential = saved;
        break;
      }
    }

    if (credential == null) {
      Log.info("Credential not registered with this user");
      throw new ServletException("Received response from credential not associated with user");
    }


    Gson gson = new Gson();
    String clientDataJson = gson.toJson(assertionResponse.getClientData());
    byte[] clientDataHash = Crypto.sha256Digest(clientDataJson.getBytes());

    Log.info("-- Verifying signature --");
    if (!(credential.getCredential().getResponse() instanceof AuthenticatorAttestationResponse)) {
      throw new ServletException("Stored attestation missing");
    }
    AuthenticatorAttestationResponse storedAttData =
        (AuthenticatorAttestationResponse) credential.getCredential().getResponse();

    if (!(storedAttData.decodedObject.getAuthenticatorData().getAttData()
        .getPublicKey() instanceof EccKey)) {
      throw new ServletException("U2f-capable key not provided");
    }

    EccKey publicKey =
        (EccKey) storedAttData.decodedObject.getAuthenticatorData().getAttData().getPublicKey();
    try {
      if (!Crypto.verifySignature(Crypto.decodePublicKey(publicKey.getX(), publicKey.getY()),
          clientDataHash, assertionResponse.getClientData().getChallenge().getBytes())) {
        throw new ServletException("Signature invalid");
      }
    } catch (WebAuthnException e) {
      throw new ServletException("Failure while verifying signature");
    }

    if (assertionResponse.getAuthenticatorData().getSignCount() <= credential.getSignCount()) {
      throw new ServletException("Sign count invalid");
    }

    credential.updateSignCount(assertionResponse.getAuthenticatorData().getSignCount());

    Log.info("Signature verified");
  }

  /**
   * @param cred
   * @param currentUser
   * @param session
   * @param origin
   * @throws ServletException
   */
  public static void registerCredential(PublicKeyCredential cred, String currentUser,
      String session, String origin) throws ServletException {

    if (!(cred.getResponse() instanceof AuthenticatorAttestationResponse)) {
      throw new ServletException("Invalid response structure");
    }

    AuthenticatorAttestationResponse attResponse =
        (AuthenticatorAttestationResponse) cred.getResponse();

    List<Credential> savedCreds = Credential.load(currentUser);
    for (Credential c : savedCreds) {
      if (c.getCredential().id.equals(cred.id)) {
        throw new ServletException("Credential already registerd for this user");
      }
    }

    Log.info("-- Verifying provided session and challenge data --");
    long id = 0;
    try {
      id = Long.valueOf(session);
    } catch (NumberFormatException e) {
      throw new ServletException("Provided session id invalid");
    }

    AssertionSessionData sessionData = AssertionSessionData.load(currentUser, Long.valueOf(id));
    if (session == null) {
      throw new ServletException("Session invalid");
    }

    CollectedClientData clientData = attResponse.getClientData();
    if (clientData == null) {
      throw new ServletException("No client data present");
    }

    byte[] sessionChallenge = BaseEncoding.base64().decode(sessionData.getChallenge());
    if (!Arrays.equals(sessionChallenge, attResponse.getClientData().getChallenge().getBytes())) {
      throw new ServletException("Returned challenge incorrect");
    }
    Log.info("Successfully verified session and challenge data");

    if (!clientData.getOrigin().equals(origin)) {
      throw new ServletException("Couldn't verify client data");
    }

    Gson gson = new Gson();
    String clientDataJson = gson.toJson(clientData);
    byte[] clientDataHash = Crypto.sha256Digest(clientDataJson.getBytes());

    byte[] rpIdHash = Crypto.sha256Digest(origin.getBytes());
    if (!Arrays.equals(attResponse.getAttestationObject().getAuthenticatorData().getRpIdHash(),
        rpIdHash)) {
      throw new ServletException("RPID hash incorrect");
    }

    if (!(attResponse.decodedObject.getAuthenticatorData().getAttData().getPublicKey() instanceof EccKey)) {
      throw new ServletException("U2f-capable key not provided");
    }

    EccKey publicKey = (EccKey) attResponse.decodedObject.getAuthenticatorData().getAttData().getPublicKey();
    try {
      if (!Crypto.verifySignature(Crypto.decodePublicKey(publicKey.getX(), publicKey.getY()),
          clientDataHash, attResponse.getClientData().getChallenge().getBytes())) {
        throw new ServletException("Signature invalid");
      }
    } catch (WebAuthnException e) {
      throw new ServletException("Failure while verifying signature");
    }

    // TODO Check trust anchors
    // TODO Check if self-attestation(/is allowed)
    // TODO Check X.509 certs

  }

}
