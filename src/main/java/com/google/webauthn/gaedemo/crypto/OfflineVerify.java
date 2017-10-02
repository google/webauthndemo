/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.webauthn.gaedemo.crypto;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.util.Base64;
import com.google.api.client.util.Key;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Sample code to verify the device attestation statement offline.
 */
public class OfflineVerify {

  private static final DefaultHostnameVerifier HOSTNAME_VERIFIER = new DefaultHostnameVerifier();

  /**
   * Class for parsing the JSON data.
   */
  public static class AttestationStatement extends JsonWebSignature.Payload {
    @Key
    private String nonce;

    @Key
    private long timestampMs;

    @Key
    private String apkPackageName;

    @Key
    private String apkDigestSha256;

    @Key
    private boolean ctsProfileMatch;

    public byte[] getNonce() {
      return Base64.decodeBase64(nonce);
    }

    public long getTimestampMs() {
      return timestampMs;
    }

    public String getApkPackageName() {
      return apkPackageName;
    }

    public byte[] getApkDigestSha256() {
      return Base64.decodeBase64(apkDigestSha256);
    }

    public boolean isCtsProfileMatch() {
      return ctsProfileMatch;
    }
  }

  public static AttestationStatement parseAndVerify(String signedAttestationStatement) {
    // Parse JSON Web Signature format.
    JsonWebSignature jws;
    try {
      jws = JsonWebSignature.parser(JacksonFactory.getDefaultInstance())
          .setPayloadClass(AttestationStatement.class).parse(signedAttestationStatement);
    } catch (IOException e) {
      System.err
          .println("Failure: " + signedAttestationStatement + " is not valid JWS " + "format.");
      return null;
    }

    // Verify the signature of the JWS and retrieve the signature certificate.
    X509Certificate cert;
    try {
      cert = jws.verifySignature();
      if (cert == null) {
        System.err.println("Failure: Signature verification failed.");
        return null;
      }
    } catch (GeneralSecurityException e) {
      System.err.println("Failure: Error during cryptographic verification of the JWS signature.");
      return null;
    }

    // Verify the hostname of the certificate.
    if (!verifyHostname("attest.android.com", cert)) {
      System.err
          .println("Failure: Certificate isn't issued for the hostname attest.android" + ".com.");
      return null;
    }

    // Extract and use the payload data.
    AttestationStatement stmt = (AttestationStatement) jws.getPayload();
    return stmt;
  }

  /**
   * Verifies that the certificate matches the specified hostname.
   * Uses the {@link DefaultHostnameVerifier} from the Apache HttpClient library
   * to confirm that the hostname matches the certificate.
   *
   * @param hostname
   * @param leafCert
   * @return
   */
  private static boolean verifyHostname(String hostname, X509Certificate leafCert) {
    try {
      // Check that the hostname matches the certificate. This method throws an exception if
      // the cert could not be verified.
      HOSTNAME_VERIFIER.verify(hostname, leafCert);
      return true;
    } catch (SSLException e) {
      e.printStackTrace();
    }

    return false;
  }


  private static void process(String signedAttestationStatement) {
    AttestationStatement stmt = parseAndVerify(signedAttestationStatement);
    if (stmt == null) {
      System.err.println("Failure: Failed to parse and verify the attestation statement.");
      return;
    }

    System.out.println("Successfully verified the attestation statement. The content is:");

    System.out.println("Nonce: " + Arrays.toString(stmt.getNonce()));
    System.out.println("Timestamp: " + stmt.getTimestampMs() + " ms");
    System.out.println("APK package name: " + stmt.getApkPackageName());
    System.out.println("APK digest SHA256: " + Arrays.toString(stmt.getApkDigestSha256()));
    System.out.println("CTS profile match: " + stmt.isCtsProfileMatch());

    System.out.println("\n** This sample only shows how to verify the authenticity of an "
        + "attestation response. Next, you must check that the server response matches the "
        + "request by comparing the nonce, package name, timestamp and digest.");
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: OfflineVerify <signed attestation statement>");
      return;
    }
    process(args[0]);
  }

}
