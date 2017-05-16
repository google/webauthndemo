package com.google.webauthn.gaedemo.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.webauthn.gaedemo.storage.Credential;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RemoveCredential
 */
public class RemoveCredential extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final UserService userService = UserServiceFactory.getUserService();

  /**
   * @see HttpServlet#HttpServlet()
   */
  public RemoveCredential() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String currentUser = userService.getCurrentUser().getUserId();
    Credential.remove(currentUser, request.getParameter("id"));
    

    List<Credential> savedCreds = Credential.load(currentUser);

    response.setContentType("text/html");
    for (Credential c : savedCreds) {
      response.getWriter()
          .println(c.toJson() + "<a href=RemoveCredential?id=" + c.id + ">[X]</a><br />");
    }
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    doGet(request, response);
  }

}
