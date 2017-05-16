package com.google.webauthn.gaedemo.servlets;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.objects.MakeCredentialOptions;
import com.google.webauthn.gaedemo.storage.AttestationSessionData;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class BeginMakeCredential extends HttpServlet {

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

    MakeCredentialOptions options = new MakeCredentialOptions(user.getNickname(), rpId, rpName);
    AttestationSessionData session = new AttestationSessionData(options.challenge, rpId);
    session.save(userService.getCurrentUser().getUserId());
    JsonObject sessionJson = session.getJsonObject();
    JsonObject optionsJson = options.getJsonObject();
    optionsJson.add("session", sessionJson);

    response.setContentType("application/json");
    response.getWriter().println(optionsJson.toString());
  }
}
