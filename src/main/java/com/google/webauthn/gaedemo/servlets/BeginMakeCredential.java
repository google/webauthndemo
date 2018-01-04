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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.objects.AuthenticatorSelectionCriteria;
import com.google.webauthn.gaedemo.objects.MakePublicKeyCredentialOptions;
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
    //String rpId = (request.isSecure() ? "https://" : "http://") + request.getHeader("Host");
    String rpId = request.getHeader("Host").split(":")[0];
    String rpName = getServletContext().getInitParameter("name");
    rpName = (rpName == null ? "" : rpName);

    MakePublicKeyCredentialOptions options =
        new MakePublicKeyCredentialOptions(user.getNickname(), user.getUserId(), rpId, rpName);

    String hasAdvanced = request.getParameter("advanced");
    if (hasAdvanced.equals("true")) {
      AuthenticatorSelectionCriteria criteria =
          AuthenticatorSelectionCriteria.parse(request.getParameter("advancedOptions"));
      options.setCriteria(criteria);
    }

    SessionData session = new SessionData(options.challenge, rpId);
    
    session.save(userService.getCurrentUser().getEmail());
    JsonObject sessionJson = session.getJsonObject();
    JsonObject optionsJson = options.getJsonObject();
    optionsJson.add("session", sessionJson);

    response.setContentType("application/json");
    response.getWriter().println(optionsJson.toString());
  }
}
