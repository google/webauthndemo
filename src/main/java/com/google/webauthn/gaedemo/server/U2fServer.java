// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.webauthn.gaedemo.server;

import com.google.gson.Gson;
import com.google.webauthn.gaedemo.crypto.Crypto;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.exceptions.WebAuthnException;
import com.google.webauthn.gaedemo.objects.AuthenticatorAssertionResponse;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.EccKey;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.storage.Credential;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;

public class U2fServer extends Server {

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

    List<Credential> savedCreds = Credential.load(currentUser);
    if (savedCreds == null || savedCreds.size() == 0) {
      throw new ServletException("No credentials registered for this user");
    }

    try {
      verifySessionAndChallenge(assertionResponse, currentUser, sessionId);
    } catch (ResponseException e1) {
      throw new ServletException("Unable to verify session and challenge data");
    }

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

    try {
      verifySessionAndChallenge(attResponse, currentUser, session);
    } catch (ResponseException e1) {
      throw new ServletException("Unable to verify session and challenge data", e1);
    }

    if (!attResponse.getClientData().getOrigin().equals(origin)) {
      throw new ServletException("Couldn't verify client data");
    }

    Gson gson = new Gson();
    String clientDataJson = gson.toJson(attResponse.getClientData());
    byte[] clientDataHash = Crypto.sha256Digest(clientDataJson.getBytes());

    byte[] rpIdHash = Crypto.sha256Digest(origin.getBytes());
    if (!Arrays.equals(attResponse.getAttestationObject().getAuthenticatorData().getRpIdHash(),
        rpIdHash)) {
      throw new ServletException("RPID hash incorrect");
    }

    if (!(attResponse.decodedObject.getAuthenticatorData().getAttData()
        .getPublicKey() instanceof EccKey)) {
      throw new ServletException("U2f-capable key not provided");
    }

    EccKey publicKey =
        (EccKey) attResponse.decodedObject.getAuthenticatorData().getAttData().getPublicKey();
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
