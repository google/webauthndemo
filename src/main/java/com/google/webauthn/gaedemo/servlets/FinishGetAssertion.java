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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.io.BaseEncoding;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.objects.AuthenticatorAssertionResponse;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.server.PublicKeyCredentialResponse;
import com.google.webauthn.gaedemo.server.Server;
import com.google.webauthn.gaedemo.storage.Credential;


public class FinishGetAssertion extends HttpServlet {
  private static final int FINGERPRINT = 2;
  private static final int SCREEN_LOCK = 134;
  private static final long serialVersionUID = 1L;
  private final UserService userService = UserServiceFactory.getUserService();


  public FinishGetAssertion() {

  }

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
    JsonElement assertionJson = null;

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
          if(uvmElement != null) {
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
      assertionJson = json.get("response");
      if (assertionJson == null) {
        throw new ServletException("Missing element 'response'");
      }
    } catch (IllegalStateException e) {
      throw new ServletException("Passed data not a json object");
    } catch (ClassCastException e) {
      throw new ServletException("Invalid input");
    } catch (JsonParseException e) {
      throw new ServletException("Input not valid json");
    }

    AuthenticatorAssertionResponse assertion = null;
    try {
      assertion = new AuthenticatorAssertionResponse(assertionJson);
    } catch (ResponseException e) {
      throw new ServletException(e.toString());
    }

    // Recoding of credential ID is needed, because the ID from HTTP servlet request doesn't support
    // padding.
    String credentialIdRecoded = BaseEncoding.base64Url().encode(
        BaseEncoding.base64Url().decode(credentialId));
    PublicKeyCredential cred = new PublicKeyCredential(credentialIdRecoded, type,
        BaseEncoding.base64Url().decode(credentialId), assertion);

    Credential savedCredential;
    try {
      savedCredential = Server.validateAndFindCredential(cred, currentUser, session);
    } catch (ResponseException e) {
      throw new ServletException("Unable to validate assertion", e);
    }

    // switch (savedCredential.getCredential().getAttestationType()) {
    // case FIDOU2F:
    // U2fServer.verifyAssertion(cred, currentUser, session, savedCredential);
    // break;
    // case ANDROIDSAFETYNET:
    // AndroidSafetyNetServer.verifyAssertion(cred, currentUser, session, savedCredential);
    // break;
    // case PACKED:
    // PackedServer.verifyAssertion(cred, currentUser, session, savedCredential);
    // break;
    // }

    Server.verifyAssertion(cred, currentUser, session, savedCredential);
    savedCredential.setUserVerificationMethod(uvm);
    savedCredential.save(currentUser);

    response.setContentType("application/json");
    String handle = DatatypeConverter.printHexBinary(savedCredential.getCredential().rawId);
    PublicKeyCredentialResponse rsp =
        new PublicKeyCredentialResponse(true, "Successful assertion", handle);
    response.getWriter().println(rsp.toJson());
  }
}
