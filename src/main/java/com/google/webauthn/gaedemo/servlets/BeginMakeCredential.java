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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.objects.MakeCredentialOptions;
import com.google.webauthn.gaedemo.storage.SessionData;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    String rpId = (request.isSecure() ? "https://" : "http://") + request.getHeader("Host");
    String rpName = getServletContext().getInitParameter("name");
    rpName = (rpName == null ? "" : rpName);

    MakeCredentialOptions options =
        new MakeCredentialOptions(user.getNickname(), user.getUserId(), rpId, rpName);
    SessionData session = new SessionData(options.challenge, rpId);

    session.save(userService.getCurrentUser().getUserId());
    JsonObject sessionJson = session.getJsonObject();
    JsonObject optionsJson = options.getJsonObject();
    optionsJson.add("session", sessionJson);

    response.setContentType("application/json");
    response.getWriter().println(optionsJson.toString());
  }
}
