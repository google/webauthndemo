package com.google.webauthn.gaedemo.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.webauthn.gaedemo.storage.Credential;
import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Home
 */
public class Home extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final UserService userService = UserServiceFactory.getUserService();

  public Home() {
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    response.setContentType("text/html;charset=UTF-8");
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/home.jsp");
    String currentUser = userService.getCurrentUser().getUserId();
    List<Credential> savedCreds = Credential.load(currentUser);

    request.setAttribute("credentialList", savedCreds);
    dispatcher.forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    doGet(request, response);
  }

}
