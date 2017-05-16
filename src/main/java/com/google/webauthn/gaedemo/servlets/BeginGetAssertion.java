package com.google.webauthn.gaedemo.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.objects.PublicKeyCredentialRequestOptions;
import com.google.webauthn.gaedemo.storage.AssertionSessionData;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class BeginGetAssertion
 */
public class BeginGetAssertion extends HttpServlet {

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
    String currentUser = userService.getCurrentUser().getUserId();
    String rpId = (request.isSecure() ? "https://" : "http://") + request.getHeader("Host");
    PublicKeyCredentialRequestOptions assertion = new PublicKeyCredentialRequestOptions(rpId);
    AssertionSessionData session = new AssertionSessionData(assertion.challenge, rpId);
    session.save(currentUser);
    assertion.populateAllowList(currentUser);

    JsonObject assertionJson = assertion.getJsonObject();
    //assertionJson.add("session", session.getJsonObject());

    response.setContentType("application/json");
    response.getWriter().println(assertionJson.toString());
  }

}
