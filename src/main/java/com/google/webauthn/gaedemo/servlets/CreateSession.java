package com.google.webauthn.gaedemo.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.objects.PublicKeyCredentialRequestOptions;
import com.google.webauthn.gaedemo.storage.AssertionSessionData;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class CreateSession
 */
public class CreateSession extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final UserService userService = UserServiceFactory.getUserService();


  /**
   * @see HttpServlet#HttpServlet()
   */
  public CreateSession() {
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    String currentUser = userService.getCurrentUser().getUserId();
    String rpId = (request.isSecure() ? "https://" : "http://") + request.getHeader("Host");
    PublicKeyCredentialRequestOptions assertion = new PublicKeyCredentialRequestOptions(rpId);
    AssertionSessionData session = new AssertionSessionData(assertion.challenge, rpId);
    session.save(currentUser);

    JsonObject assertionJson = new JsonObject();
    assertionJson.add("session", session.getJsonObject());
    

    response.setContentType("application/json");
    response.getWriter().println(assertionJson.toString());
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    doGet(request, response);
  }

}
