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
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.objects.AuthenticationExtensionsClientInputs;
import com.google.webauthn.gaedemo.objects.PublicKeyCredentialRequestOptions;
import com.google.webauthn.gaedemo.storage.SessionData;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class BeginGetAssertion
 */
public class BeginGetAssertion extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private final UserService userService = UserServiceFactory.getUserService();

  public BeginGetAssertion() {}

  @Override
  protected void doGet(HttpServletRequest q, HttpServletResponse p)
      throws ServletException, IOException {
    doPost(q, p);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String currentUser = userService.getCurrentUser().getEmail();
    String rpId = Iterables.get(Splitter.on(':').split(request.getHeader("Host")), 0);
    // String rpId = (request.isSecure() ? "https://" : "http://") + request.getHeader("Host");
    PublicKeyCredentialRequestOptions assertion = new PublicKeyCredentialRequestOptions(rpId);
    SessionData session = new SessionData(assertion.challenge, rpId);
    session.save(currentUser);
    JsonObject sessionJson = session.getJsonObject();
    assertion.populateAllowList(currentUser);

    JsonObject assertionJson = assertion.getJsonObject();
    assertionJson.add("session", sessionJson);

    response.setContentType("application/json");
    response.getWriter().println(assertionJson.toString());
  }

}
