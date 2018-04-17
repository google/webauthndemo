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
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.google.common.primitives.Bytes;
import com.google.webauthn.gaedemo.crypto.AlgorithmIdentifierMapper;
import com.google.webauthn.gaedemo.crypto.Crypto;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.exceptions.WebAuthnException;
import com.google.webauthn.gaedemo.objects.AuthenticatorAssertionResponse;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.EccKey;
import com.google.webauthn.gaedemo.objects.PackedAttestationStatement;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.objects.RsaKey;
import com.google.webauthn.gaedemo.storage.Credential;

import co.nstant.in.cbor.CborException;


public class PackedServer extends Server {

  private static final Logger Log = Logger.getLogger(PackedServer.class.getName());

  /**
   * @param cred
   * @param currentUser
   * @param sessionId
   * @throws ServletException
   */
  @Deprecated
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
      throw new ServletException("U2f-capable key not provided");
    }



    // if (Integer.compareUnsigned(assertionResponse.getAuthenticatorData().getSignCount(),
    // savedCredential.getSignCount()) <= 0) {
    // throw new ServletException("Sign count invalid");
    // }

    savedCredential.updateSignCount(assertionResponse.getAuthenticatorData().getSignCount());

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

    byte[] clientDataHash = Crypto.sha256Digest(attResponse.getClientDataBytes());

    byte[] rpIdHash = Crypto.sha256Digest(origin.getBytes());

    if (!Arrays.equals(attResponse.getAttestationObject().getAuthenticatorData().getRpIdHash(),
        rpIdHash)) {
      throw new ServletException("RPID hash incorrect");
    }

    if (!(attResponse.decodedObject.getAuthenticatorData().getAttData()
        .getPublicKey() instanceof EccKey) && !(attResponse.decodedObject.getAuthenticatorData().getAttData()
            .getPublicKey() instanceof RsaKey)) {
      throw new ServletException("Supported key not provided");
    }

    PackedAttestationStatement attStmt =
        (PackedAttestationStatement) attResponse.decodedObject.getAttestationStatement();

    try {
      /*
       * Signatures are signed over the concatenation of Authenticator data and Client Data Hash
       */
      byte[] signedBytes =
          Bytes.concat(attResponse.decodedObject.getAuthenticatorData().encode(), clientDataHash);

      StringBuffer buf = new StringBuffer();
      for (byte b : signedBytes) {
        buf.append(String.format("%02X ", b));
      }

      Log.info("Signed bytes: " + buf.toString());

      // TODO Make attStmt.attestnCert an X509Certificate right off the
      // bat.
      DataInputStream inputStream =
          new DataInputStream(new ByteArrayInputStream(attStmt.attestnCert));
      X509Certificate attestationCertificate = (X509Certificate) CertificateFactory
          .getInstance("X.509").generateCertificate(inputStream);

      String signatureAlgorithm;
      try {
        signatureAlgorithm = AlgorithmIdentifierMapper.get(
            attResponse.decodedObject.getAuthenticatorData().getAttData().getPublicKey().getAlg())
            .getJavaAlgorithm();
      } catch (Exception e) {
        // Default to ES256
        signatureAlgorithm = "SHA256withECDSA";
      }

      if (!Crypto.verifySignature(attestationCertificate, signedBytes, attStmt.sig,
          signatureAlgorithm)) {
        throw new ServletException("Signature invalid");
      }
    } catch (CertificateException e) {
      throw new ServletException("Error when parsing attestationCertificate");
    } catch (WebAuthnException e) {
      throw new ServletException("Failure while verifying signature", e);
    } catch (CborException e) {
      throw new ServletException("Unable to reencode authenticator data");
    }

    // TODO Check trust anchors
    // TODO Check if self-attestation(/is allowed)
    // TODO Check X.509 certs

  }

}
