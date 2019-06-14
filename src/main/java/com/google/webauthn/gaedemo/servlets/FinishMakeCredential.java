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

package com.google.webauthn.gaedemo.servlets;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.BaseEncoding;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.objects.AttestationExtension;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.CablePairingData;
import com.google.webauthn.gaedemo.objects.CableRegistrationData;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.server.AndroidSafetyNetServer;
import com.google.webauthn.gaedemo.server.PackedServer;
import com.google.webauthn.gaedemo.server.PublicKeyCredentialResponse;
import com.google.webauthn.gaedemo.server.U2fServer;
import com.google.webauthn.gaedemo.storage.CableKeyPair;
import com.google.webauthn.gaedemo.storage.Credential;

public class FinishMakeCredential extends HttpServlet {

  private static final int FINGERPRINT = 2;
  private static final int SCREEN_LOCK = 134;
  private static final long serialVersionUID = 1L;
  private final UserService userService = UserServiceFactory.getUserService();

  public FinishMakeCredential() {}

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String currentUser = userService.getCurrentUser().getEmail();
    String data = request.getParameter("data");
    String session = request.getParameter("session");

    String credentialId = null;
    String type = null;
    String uvm = null;
    JsonElement makeCredentialResponse = null;
    CablePairingData cablePairingData = null;

    try {
      JsonObject json = new JsonParser().parse(data).getAsJsonObject();
      JsonElement idJson = json.get("id");
      if (idJson != null) {
        credentialId = idJson.getAsString();
      }
      JsonElement typeJson = json.get("type");
      if (typeJson != null) {
        type = typeJson.getAsString();
      }
      JsonElement uvmJson = json.get("uvm");
      if (uvmJson != null && uvmJson.isJsonArray()) {
        JsonArray uvmArray = uvmJson.getAsJsonArray();
        if (uvmJson.isJsonArray()) {
          JsonElement uvmElement = uvmArray.get(0);
          if (uvmElement != null) {
            switch (uvmElement.getAsJsonObject().get("userVerificationMethod").getAsInt()){
              case FINGERPRINT:
                uvm = "Fingerprint";
                break;
              case SCREEN_LOCK:
                uvm = "Screen Lock";
                break;
              default:
                uvm = "Others";
                break;
            }
          }
        }
      }
      makeCredentialResponse = json.get("response");
    } catch (IllegalStateException e) {
      throw new ServletException("Passed data not a json object");
    } catch (ClassCastException e) {
      throw new ServletException("Invalid input");
    } catch (JsonParseException e) {
      throw new ServletException("Input not valid json");
    }

    AuthenticatorAttestationResponse attestation = null;
    try {
      attestation = new AuthenticatorAttestationResponse(makeCredentialResponse);
    } catch (ResponseException e) {
      throw new ServletException(e);
    }

    if (attestation.getAttestationObject().getAuthenticatorData().hasExtensionData()) {
      Map<String, AttestationExtension> extensionMap =
          attestation.getAttestationObject().getAuthenticatorData().getExtensionData();
      if (extensionMap.containsKey(CableRegistrationData.KEY)) {
        CableRegistrationData cableData =
            (CableRegistrationData) extensionMap.get(CableRegistrationData.KEY);

        // Get key pair generated during the StartMakeCredential operation
        KeyPair sessionKeyPair = CableKeyPair.get(Long.valueOf(session));

        cablePairingData = CablePairingData.generatePairingData(cableData, sessionKeyPair);
      }
    }

    // Recoding of credential ID is needed, because the ID from HTTP servlet request doesn't support
    // padding.
    String credentialIdRecoded =
        BaseEncoding.base64Url().encode(BaseEncoding.base64Url().decode(credentialId));

    PublicKeyCredential cred = new PublicKeyCredential(credentialIdRecoded, type,
        BaseEncoding.base64Url().decode(credentialId), attestation);

    String domain = (request.isSecure() ? "https://" : "http://") + request.getHeader("Host");
    String rpId = Iterables.get(Splitter.on(':').split(request.getHeader("Host")), 0);
    switch (cred.getAttestationType()) {
      case FIDOU2F:
        U2fServer.registerCredential(cred, currentUser, session, domain, rpId);
        break;
      case ANDROIDSAFETYNET:
        AndroidSafetyNetServer.registerCredential(cred, currentUser, session, rpId);
        break;
      case PACKED:
        PackedServer.registerCredential(cred, currentUser, session, rpId);
        break;
      case NONE:
        break;
    }

    Credential credential = new Credential(cred);
    if (cablePairingData != null) {
      credential.setCablePairingData(cablePairingData);
    }
    credential.setUserVerificationMethod(uvm);
    credential.save(currentUser);

    PublicKeyCredentialResponse rsp =
        new PublicKeyCredentialResponse(true, "Successfully created credential");

    response.setContentType("application/json");
    response.getWriter().println(rsp.toJson());
  }

}
