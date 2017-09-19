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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.google.common.primitives.Bytes;
import com.google.gson.Gson;
import com.google.webauthn.gaedemo.crypto.Crypto;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.exceptions.WebAuthnException;
import com.google.webauthn.gaedemo.objects.AuthenticatorAssertionResponse;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.EccKey;
import com.google.webauthn.gaedemo.objects.FidoU2fAttestationStatement;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.storage.Credential;

public class U2fServer extends Server {

  private static final Logger Log = Logger.getLogger(U2fServer.class.getName());

  /**
   * @param cred
   * @param currentUser
   * @param sessionId
   * @throws ServletException
   */
  public static void verifyAssertion(PublicKeyCredential cred, String currentUser, String sessionId,
      Credential savedCredential) throws ServletException {
    AuthenticatorAssertionResponse assertionResponse = (AuthenticatorAssertionResponse) cred.getResponse();

    Log.info("-- Verifying signature --");
    if (!(savedCredential.getCredential().getResponse() instanceof AuthenticatorAttestationResponse)) {
      throw new ServletException("Stored attestation missing");
    }
    AuthenticatorAttestationResponse storedAttData = (AuthenticatorAttestationResponse) savedCredential.getCredential()
        .getResponse();

    if (!(storedAttData.decodedObject.getAuthenticatorData().getAttData().getPublicKey() instanceof EccKey)) {
      throw new ServletException("U2f-capable key not provided");
    }

    EccKey publicKey = (EccKey) storedAttData.decodedObject.getAuthenticatorData().getAttData().getPublicKey();
    try {
      /*
       * U2F authentication signatures are signed over the concatenation of
       *
       * 32 byte application parameter hash
       *
       * 1 byte user presence
       *
       * 4 byte big-endian representation of the counter
       *
       * 32 byte challenge parameter (ie SHA256 hash of clientData)
       */
      String clientDataJson = assertionResponse.getClientDataString();
      byte[] clientDataHash = Crypto.sha256Digest(clientDataJson.getBytes());

      byte[] signedBytes = Bytes.concat(storedAttData.getAttestationObject().getAuthenticatorData().getRpIdHash(),
          new byte[] { (assertionResponse.getAuthenticatorData().isUP() == true ? (byte) 1 : (byte) 0) },
          ByteBuffer.allocate(4).putInt(assertionResponse.getAuthenticatorData().getSignCount()).array(),
          clientDataHash);
      if (!Crypto.verifySignature(Crypto.decodePublicKey(publicKey.getX(), publicKey.getY()), signedBytes,
          assertionResponse.getSignature())) {
        throw new ServletException("Signature invalid");
      }
    } catch (WebAuthnException e) {
      throw new ServletException("Failure while verifying signature");
    }

    if (assertionResponse.getAuthenticatorData().getSignCount() <= savedCredential.getSignCount()) {
      throw new ServletException("Sign count invalid");
    }

    savedCredential.updateSignCount(assertionResponse.getAuthenticatorData().getSignCount());

    Log.info("Signature verified");
  }

  /**
   * @param cred
   * @param currentUser
   * @param session
   * @param originString
   * @throws ServletException
   */
  public static void registerCredential(PublicKeyCredential cred, String currentUser, String session,
      String originString, String rpId) throws ServletException {

    if (!(cred.getResponse() instanceof AuthenticatorAttestationResponse)) {
      throw new ServletException("Invalid response structure");
    }

    AuthenticatorAttestationResponse attResponse = (AuthenticatorAttestationResponse) cred.getResponse();

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

    if (!attResponse.getClientData().getOrigin().equals(originString)) {
      throw new ServletException("Client data origin: " + attResponse.getClientData().getOrigin()
          + " does not match server origin:" + originString);
    }

    String clientDataJson = attResponse.getClientDataString();
    System.out.println(clientDataJson);
    byte[] clientDataHash = Crypto.sha256Digest(clientDataJson.getBytes());

    byte[] rpIdHash = Crypto.sha256Digest(rpId.getBytes());
    if (!Arrays.equals(attResponse.getAttestationObject().getAuthenticatorData().getRpIdHash(), rpIdHash)) {
      throw new ServletException("RPID hash incorrect");
    }

    if (!(attResponse.decodedObject.getAuthenticatorData().getAttData().getPublicKey() instanceof EccKey)) {
      throw new ServletException("U2f-capable key not provided");
    }

    FidoU2fAttestationStatement attStmt = (FidoU2fAttestationStatement) attResponse.decodedObject
        .getAttestationStatement();

    EccKey publicKey = (EccKey) attResponse.decodedObject.getAuthenticatorData().getAttData().getPublicKey();

    try {
      /*
       * U2F registration signatures are signed over the concatenation of
       *
       * 1 byte RFU (0)
       *
       * 32 byte application parameter hash
       *
       * 32 byte challenge parameter
       *
       * key handle
       *
       * 65 byte user public key represented as {0x4, X, Y}
       */
      byte[] signedBytes = Bytes.concat(new byte[] { 0 }, rpIdHash, clientDataHash, cred.rawId, new byte[] { 0x04 },
          publicKey.getX(), publicKey.getY());

      // TODO Make attStmt.attestnCert an X509Certificate right off the
      // bat.
      DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(attStmt.attestnCert));
      X509Certificate attestationCertificate = (X509Certificate) CertificateFactory.getInstance("X.509")
          .generateCertificate(inputStream);
      if (!Crypto.verifySignature(attestationCertificate, signedBytes, attStmt.sig)) {
        throw new ServletException("Signature invalid");
      }
    } catch (CertificateException e) {
      throw new ServletException("Error when parsing attestationCertificate");
    } catch (WebAuthnException e) {
      throw new ServletException("Failure while verifying signature", e);
    }

    // TODO Check trust anchors
    // TODO Check if self-attestation(/is allowed)
    // TODO Check X.509 certs

  }

}
