package com.google.webauthn.gaedemo.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.io.BaseEncoding;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.server.U2fServer;
import com.google.webauthn.gaedemo.storage.Credential;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FinishMakeCredential extends HttpServlet {
  private final UserService userService = UserServiceFactory.getUserService();

  public FinishMakeCredential() {}

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String currentUser = userService.getCurrentUser().getUserId();
    String data = request.getParameter("data");
    String session = request.getParameter("session");

    String credentialId = null;
    String type = null;
    String makeCredentialResponse = null;

    try {
      JsonObject json = new JsonParser().parse(data).getAsJsonObject();
      JsonElement idJson = json.get("id");
      if (idJson != null) {
        credentialId = idJson.getAsString();
      }
      JsonElement typeJson = json.get("type");
      if (typeJson != null) {
        type = typeJson.getAsString();
      }
      JsonElement assertionJson = json.get("response");
      if (assertionJson != null) {
        makeCredentialResponse = assertionJson.getAsString();
      }
    } catch (IllegalStateException e) {
      throw new ServletException("Passed data not a json object");
    } catch (ClassCastException e) {
      throw new ServletException("Invalid input");
    } catch (JsonParseException e) {
      throw new ServletException("Input not valid json");
    }

    AuthenticatorAttestationResponse attestation = null;
    try {
      attestation = new AuthenticatorAttestationResponse(makeCredentialResponse);
    } catch (ResponseException e) {
      throw new ServletException(e.toString());
    }

    PublicKeyCredential cred = new PublicKeyCredential(credentialId, type,
        BaseEncoding.base64().decode(credentialId), attestation);
    
    String rpId = (request.isSecure() ? "https://" : "http://") + request.getHeader("Host");
    U2fServer.registerCredential(cred, currentUser, session, rpId);

    Credential credential = new Credential(cred);
    credential.save(currentUser);

    response.setContentType("application/json");
    response.getWriter().println(credential.toJson());
  }

}
