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

package com.google.webauthn.gaedemo.objects;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PublicKeyCredentialCreationOptions {
  private static final int CHALLENGE_LENGTH = 32;
  private final SecureRandom random = new SecureRandom();

  PublicKeyCredentialEntity rp;
  PublicKeyCredentialUserEntity user;
  public byte[] challenge;
  ArrayList<PublicKeyCredentialParameters> pubKeyCredParams;

  long timeout;
  ArrayList<PublicKeyCredentialDescriptor> excludeCredentials;
  protected AuthenticatorSelectionCriteria authenticatorSelection;
  protected AttestationConveyancePreference attestation;
  protected AuthenticationExtensionsClientInputs extensions;

  /**
   * 
   */
  public PublicKeyCredentialCreationOptions() {
    pubKeyCredParams = new ArrayList<PublicKeyCredentialParameters>();
    excludeCredentials = new ArrayList<PublicKeyCredentialDescriptor>();
    extensions = null;
    authenticatorSelection = null;
  }

  /**
   * @param userId
   * @param rpId
   * @param rpName
   */
  public PublicKeyCredentialCreationOptions(String userName, String userId, String rpId,
      String rpName) {
    pubKeyCredParams = new ArrayList<PublicKeyCredentialParameters>();
    excludeCredentials = new ArrayList<PublicKeyCredentialDescriptor>();
    rp = new PublicKeyCredentialRpEntity(rpId, rpName, null);
    user = new PublicKeyCredentialUserEntity(userName, userId.getBytes(StandardCharsets.UTF_8));

    challenge = new byte[CHALLENGE_LENGTH];
    random.nextBytes(challenge);
    pubKeyCredParams.add(
        new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, Algorithm.ES256));
    pubKeyCredParams.add(
        new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, Algorithm.ES384));
    pubKeyCredParams.add(
        new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, Algorithm.ES512));
    pubKeyCredParams.add(
        new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, Algorithm.RS256));
    pubKeyCredParams.add(
        new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, Algorithm.RS384));
    pubKeyCredParams.add(
        new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, Algorithm.RS512));
    pubKeyCredParams.add(
        new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, Algorithm.PS256));
    pubKeyCredParams.add(
        new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, Algorithm.PS384));
    pubKeyCredParams.add(
        new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, Algorithm.PS512));
    extensions = null;
  }

  public void setExtensions(AuthenticationExtensionsClientInputs extensions) {
    this.extensions = extensions;
  }

  public void setCriteria(AuthenticatorSelectionCriteria criteria) {
    this.authenticatorSelection = criteria;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public void excludeCredential(PublicKeyCredentialDescriptor credential) {
    excludeCredentials.add(credential);
  }

  public void setExcludeCredentials(Collection<PublicKeyCredentialDescriptor> excludeCredentials) {
    this.excludeCredentials.clear();
    this.excludeCredentials.addAll(excludeCredentials);
  }

  public void setAttestationConveyancePreference(AttestationConveyancePreference attestation) {
    this.attestation = attestation;
  }

  /**
   * @return Encoded JsonObect representation of MakeCredentialOptions
   */
  public JsonObject getJsonObject() {
    // Required parameters
    JsonObject result = new JsonObject();
    result.add("rp", rp.getJsonObject());
    result.add("user", user.getJsonObject());
    result.addProperty("challenge", BaseEncoding.base64().encode(challenge));
    JsonArray params = new JsonArray();
    for (PublicKeyCredentialParameters param : pubKeyCredParams)
      params.add(param.getJsonObject());
    result.add("pubKeyCredParams", params);

    // Optional parameters
    if (this.timeout > 0) {
      result.addProperty("timeout", timeout);
    }
    if (this.excludeCredentials != null && this.excludeCredentials.size() > 0) {
      JsonArray excludeParams = new JsonArray();
      for (PublicKeyCredentialDescriptor descriptor : excludeCredentials) {
        excludeParams.add(descriptor.getJsonObject());
      }
      result.add("excludeCredentials", excludeParams);
    }
    if (this.authenticatorSelection != null) {
      result.add("authenticatorSelection", authenticatorSelection.getJsonObject());
    }
    if (this.attestation != null) {
      result.addProperty("attestation", this.attestation.toString());
    }
    if (extensions != null) {
      // TODO
    }

    return result;
  }
}
