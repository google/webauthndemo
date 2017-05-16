package com.google.webauthn.gaedemo.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.io.BaseEncoding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.storage.Credential;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RegisteredKeys
 */
public class RegisteredKeys extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final UserService userService = UserServiceFactory.getUserService();

  /**
   * @see HttpServlet#HttpServlet()
   */
  public RegisteredKeys() {}

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String currentUser = userService.getCurrentUser().getUserId();
    List<Credential> savedCreds = Credential.load(currentUser);

    JsonArray result = new JsonArray();
    response.setContentType("text/json");
    for (Credential c : savedCreds) {
      JsonObject cJson = new JsonObject();
      cJson.addProperty("id", c.getCredential().id);
      cJson.addProperty("handle", BaseEncoding.base64().encode(c.getCredential().rawId));
      result.add(cJson);
    }
    response.getWriter().print(result.toString());
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

}
