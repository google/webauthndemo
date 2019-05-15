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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.crypto.Cable;
import com.google.webauthn.gaedemo.storage.Credential;

public class PublicKeyCredentialRequestOptions {
  private static final int CHALLENGE_LENGTH = 32;
  private final SecureRandom random = new SecureRandom();

  // Required parameters
  public byte[] challenge;
  
  // Optional parameters
  public long timeout;
  public String rpId;
  protected ArrayList<PublicKeyCredentialDescriptor> allowCredentials;
  protected UserVerificationRequirement userVerification;
  AuthenticationExtensionsClientInputs extensions;
  

  /**
   * @param rpId
   */
  public PublicKeyCredentialRequestOptions(String rpId) {
    challenge = new byte[CHALLENGE_LENGTH];
    random.nextBytes(challenge);
    allowCredentials = new ArrayList<PublicKeyCredentialDescriptor>();
    this.rpId = rpId;
  }

  /**
   * @return JsonObject representation of PublicKeyCredentialRequestOptions
   */
  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();

    result.addProperty("challenge", BaseEncoding.base64().encode(challenge));
    if (timeout > 0) {
      result.addProperty("timeout", timeout);
    }
    result.addProperty("rpId", rpId);
    JsonArray allowCredentials = new JsonArray();
    for (PublicKeyCredentialDescriptor credential : this.allowCredentials) {
      allowCredentials.add(credential.getJsonObject());
    }
    result.add("allowCredentials", allowCredentials);
    if (extensions != null) {
      result.add("extensions", extensions.getJsonObject());
    }

    return result;
  }

  public List<PublicKeyCredentialDescriptor> getAllowCredentials() {
    return allowCredentials;
  }

  /**
   * @param currentUser
   */
  public void populateAllowList(String currentUser) {
    List<Credential> credentialList = Credential.load(currentUser);
    for (Credential c : credentialList) {
      PublicKeyCredential storedCred = c.getCredential();
      if (storedCred == null)
        continue;

      PublicKeyCredentialDescriptor pkcd =
          new PublicKeyCredentialDescriptor(PublicKeyCredentialType.PUBLIC_KEY, storedCred.rawId);

      if (((AuthenticatorAttestationResponse) storedCred.getResponse()).getTransports() != null) {
        ArrayList<AuthenticatorTransport> transportList = new ArrayList<>();
        for (String transport : ((AuthenticatorAttestationResponse) storedCred.getResponse()).getTransports()) {
          transportList.add(AuthenticatorTransport.decode(transport));
        }
        pkcd.setTransports(transportList);
      }

      allowCredentials.add(pkcd);

      Cable cableCrypto = new Cable();
      CablePairingData cablePairingData = c.getCablePairingData();
      if (cablePairingData != null) {
        if (extensions == null) {
          extensions = new AuthenticationExtensionsClientInputs();
        }
        extensions.addCableSessionData(cableCrypto.generateSessionData(cablePairingData));
      }
    }
  }
}
