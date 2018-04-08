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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.io.BaseEncoding;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.server.AndroidSafetyNetServer;
import com.google.webauthn.gaedemo.server.PackedServer;
import com.google.webauthn.gaedemo.server.PublicKeyCredentialResponse;
import com.google.webauthn.gaedemo.server.U2fServer;
import com.google.webauthn.gaedemo.storage.Credential;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FinishMakeCredential extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private final UserService userService = UserServiceFactory.getUserService();

  public FinishMakeCredential() {}

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String currentUser = userService.getCurrentUser().getEmail();
    String data = request.getParameter("data");
    String session = request.getParameter("session");

    String credentialId = null;
    String type = null;
    JsonElement makeCredentialResponse = null;

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
      throw new ServletException(e.toString());
    }

    // Recoding of credential ID is needed, because the ID from HTTP servlet request doesn't support
    // padding.
    String credentialIdRecoded = BaseEncoding.base64Url().encode(
        BaseEncoding.base64Url().decode(credentialId));

    PublicKeyCredential cred = new PublicKeyCredential(credentialIdRecoded, type,
        BaseEncoding.base64Url().decode(credentialId), attestation);

    String domain = (request.isSecure() ? "https://" : "http://") + request.getHeader("Host");
    String rpId = request.getHeader("Host").split(":")[0];
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
    credential.save(currentUser);

    PublicKeyCredentialResponse rsp =
        new PublicKeyCredentialResponse(true, "Successfully created credential");

    response.setContentType("application/json");
    response.getWriter().println(rsp.toJson());
  }

}
