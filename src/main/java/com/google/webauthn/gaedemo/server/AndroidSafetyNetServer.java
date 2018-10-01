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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.google.common.primitives.Bytes;
import com.google.webauthn.gaedemo.crypto.Crypto;
import com.google.webauthn.gaedemo.crypto.OfflineVerify;
import com.google.webauthn.gaedemo.crypto.OfflineVerify.AttestationStatement;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.exceptions.WebAuthnException;
import com.google.webauthn.gaedemo.objects.AndroidSafetyNetAttestationStatement;
import com.google.webauthn.gaedemo.objects.AuthenticatorAssertionResponse;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.EccKey;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.storage.Credential;

import co.nstant.in.cbor.CborException;

public class AndroidSafetyNetServer extends Server {
  private static final Logger Log = Logger.getLogger(AndroidSafetyNetServer.class.getName());

  /**
   * @param cred
   * @param rpId
   * @param session
   * @param currentUser
   * @throws ServletException
   */
  public static void registerCredential(PublicKeyCredential cred, String currentUser,
      String session, String rpId) throws ServletException {

    if (!(cred.getResponse() instanceof AuthenticatorAttestationResponse)) {
      throw new ServletException("Invalid response structure");
    }

    AuthenticatorAttestationResponse attResponse =
        (AuthenticatorAttestationResponse) cred.getResponse();

    List<Credential> savedCreds = Credential.load(currentUser);
    for (Credential c : savedCreds) {
      if (c.getCredential().id.equals(cred.id)) {
        throw new ServletException("Credential already registered for this user");
      }
    }

    try {
      verifySessionAndChallenge(attResponse, currentUser, session);
    } catch (ResponseException e1) {
      throw new ServletException("Unable to verify session and challenge data");
    }

    AndroidSafetyNetAttestationStatement attStmt =
        (AndroidSafetyNetAttestationStatement) attResponse.decodedObject.getAttestationStatement();

    AttestationStatement stmt =
        OfflineVerify.parseAndVerify(new String(attStmt.getResponse(), StandardCharsets.UTF_8));
    if (stmt == null) {
      Log.info("Failure: Failed to parse and verify the attestation statement.");
      throw new ServletException("Failed to verify attestation statement");
    }

    byte[] clientDataHash = Crypto.sha256Digest(attResponse.getClientDataBytes());

    try {
      // Nonce was changed from [authenticatorData, clientDataHash] to
      // sha256 [authenticatorData, clientDataHash]
      // https://github.com/w3c/webauthn/pull/869
      byte[] expectedNonce = Crypto.sha256Digest(Bytes.concat(
          attResponse.getAttestationObject().getAuthenticatorData().encode(), clientDataHash));
      if (!Arrays.equals(expectedNonce, stmt.getNonce())) {
        // TODO(cpiper) Remove this hack.
        expectedNonce = Bytes.concat(
            attResponse.getAttestationObject().getAuthenticatorData().encode(), clientDataHash);
        if (!Arrays.equals(expectedNonce, stmt.getNonce())) {
          throw new ServletException("Nonce does not match");
        }
        //
      }
    } catch (CborException e) {
      throw new ServletException("Error encoding authdata");
    }

    /*
     * // Test devices won't pass this. if (!stmt.isCtsProfileMatch()) { throw new
     * ServletException("No cts profile match"); }
     */
  }

  // TODO Remove after switch to generic verification
  /**
   * @param cred
   * @param currentUser
   * @param sessionId
   * @throws ServletException
   */
  public static void verifyAssertion(PublicKeyCredential cred, String currentUser, String sessionId,
      Credential savedCredential) throws ServletException {

    AuthenticatorAssertionResponse assertionResponse =
        (AuthenticatorAssertionResponse) cred.getResponse();

    Log.info("-- Verifying signature --");
    if (!(savedCredential.getCredential()
        .getResponse() instanceof AuthenticatorAttestationResponse)) {
      throw new ServletException("Stored attestation missing");
    }
    AuthenticatorAttestationResponse storedAttData =
        (AuthenticatorAttestationResponse) savedCredential.getCredential().getResponse();

    if (!(storedAttData.decodedObject.getAuthenticatorData().getAttData()
        .getPublicKey() instanceof EccKey)) {
      throw new ServletException("Ecc key not provided");
    }

    EccKey publicKey =
        (EccKey) storedAttData.decodedObject.getAuthenticatorData().getAttData().getPublicKey();
    try {
      byte[] clientDataHash = Crypto.sha256Digest(assertionResponse.getClientDataBytes());
      byte[] signedBytes =
          Bytes.concat(assertionResponse.getAuthenticatorData().encode(), clientDataHash);
      if (!Crypto.verifySignature(Crypto.decodePublicKey(publicKey.getX(), publicKey.getY()),
          signedBytes, assertionResponse.getSignature())) {
        throw new ServletException("Signature invalid");
      }
    } catch (WebAuthnException e) {
      throw new ServletException("Failure while verifying signature", e);
    } catch (CborException e) {
      throw new ServletException("Failure while verifying authenticator data");
    }

    if (Integer.compareUnsigned(assertionResponse.getAuthenticatorData().getSignCount(),
        savedCredential.getSignCount()) <= 0 && savedCredential.getSignCount() != 0) {
      throw new ServletException("Sign count invalid");
    }

    savedCredential.updateSignCount(assertionResponse.getAuthenticatorData().getSignCount());

    Log.info("Signature verified");
  }
}
