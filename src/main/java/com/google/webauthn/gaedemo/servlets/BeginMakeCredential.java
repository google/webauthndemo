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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.webauthn.gaedemo.objects.AttestationConveyancePreference;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttachment;
import com.google.webauthn.gaedemo.objects.AuthenticatorSelectionCriteria;
import com.google.webauthn.gaedemo.objects.AuthenticatorTransport;
import com.google.webauthn.gaedemo.objects.PublicKeyCredentialCreationOptions;
import com.google.webauthn.gaedemo.objects.PublicKeyCredentialDescriptor;
import com.google.webauthn.gaedemo.objects.PublicKeyCredentialType;
import com.google.webauthn.gaedemo.objects.UserVerificationRequirement;
import com.google.webauthn.gaedemo.storage.Credential;
import com.google.webauthn.gaedemo.storage.SessionData;

public class BeginMakeCredential extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private final UserService userService = UserServiceFactory.getUserService();

  public BeginMakeCredential() {}

  @Override
  protected void doGet(HttpServletRequest a, HttpServletResponse b)
      throws ServletException, IOException {
    doPost(a, b);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    User user = userService.getCurrentUser();
    // String rpId = (request.isSecure() ? "https://" : "http://") + request.getHeader("Host");
    String rpId = Iterables.get(Splitter.on(':').split(request.getHeader("Host")), 0);
    String rpName = getServletContext().getInitParameter("name");
    rpName = (rpName == null ? "" : rpName);

    PublicKeyCredentialCreationOptions options =
        new PublicKeyCredentialCreationOptions(user.getNickname(), user.getEmail(), rpId, rpName);

    String hasAdvanced = request.getParameter("advanced");
    if (hasAdvanced.equals("true")) {
      parseAdvancedOptions(request.getParameter("advancedOptions"), options);
    }

    SessionData session = new SessionData(options.challenge, rpId);
    
    session.save(userService.getCurrentUser().getEmail());
    JsonObject sessionJson = session.getJsonObject();
    JsonObject optionsJson = options.getJsonObject();
    optionsJson.add("session", sessionJson);

    response.setContentType("application/json");
    response.getWriter().println(optionsJson.toString());
  }

  private void parseAdvancedOptions(String jsonString, PublicKeyCredentialCreationOptions options) {
    JsonElement jsonElement = new JsonParser().parse(jsonString);
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();

    boolean rk = false;
    boolean excludeCredentials = false;
    UserVerificationRequirement uv = null;
    AuthenticatorAttachment attachment = null;
    for (Map.Entry<String, JsonElement> entry : entries) {
      if (entry.getKey().equals("requireResidentKey")) {
        rk = entry.getValue().getAsBoolean();
      } else if (entry.getKey().equals("excludeCredentials")) {
        excludeCredentials = entry.getValue().getAsBoolean();

        if (excludeCredentials) {
          List<PublicKeyCredentialDescriptor> credentials = new ArrayList<>();
          String currentUser = userService.getCurrentUser().getEmail();
          List<Credential> savedCreds = Credential.load(currentUser);
          for (Credential c : savedCreds) {
            credentials.add(convertCredentialToCredentialDescriptor(c));
          }
          options.setExcludeCredentials(credentials);
        }
      } else if (entry.getKey().equals("userVerification")) {
        uv = UserVerificationRequirement.decode(entry.getValue().getAsString());
      } else if (entry.getKey().equals("authenticatorAttachment")) {
        attachment = AuthenticatorAttachment.decode(entry.getValue().getAsString());
      } else if (entry.getKey().equals("attestationConveyancePreference")) {
        AttestationConveyancePreference conveyance =
            AttestationConveyancePreference.decode(entry.getValue().getAsString());
        options.setAttestationConveyancePreference(conveyance);
      }
    }

    options.setCriteria(new AuthenticatorSelectionCriteria(attachment, rk, uv));
  }

  private PublicKeyCredentialDescriptor convertCredentialToCredentialDescriptor(Credential c) {
    PublicKeyCredentialType type = PublicKeyCredentialType.PUBLIC_KEY;
    byte[] id = c.getCredential().getRawId();

    return new PublicKeyCredentialDescriptor(type, id, null);
  }

}
