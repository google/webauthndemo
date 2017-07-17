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
import com.google.webauthn.gaedemo.crypto.Crypto;
import com.google.webauthn.gaedemo.objects.AttestationData;
import com.google.webauthn.gaedemo.objects.AttestationObject;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.AuthenticatorData;
import com.google.webauthn.gaedemo.objects.EccKey;
import com.google.webauthn.gaedemo.objects.FidoU2fAttestationStatement;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.storage.Credential;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Servlet implementation class SaveCredential
 */
public class SaveCredential extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final UserService userService = UserServiceFactory.getUserService();

  /**
   * @see HttpServlet#HttpServlet()
   */
  public SaveCredential() {
    super();
  }

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String currentUser = userService.getCurrentUser().getUserId();
    String id = request.getParameter("id");

    Random rand = new Random();
    byte[] aaguid = new byte[16];
    rand.nextBytes(aaguid);
    byte[] x = new byte[5], y = new byte[5];
    try {
      ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("secp256r1");
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
      keyGen.initialize(ecGenSpec, new SecureRandom());
      KeyPair keyPair = keyGen.generateKeyPair();
      PublicKey pub = keyPair.getPublic();
      ECPublicKey publicKey = (ECPublicKey) pub;
      x = publicKey.getW().getAffineX().toByteArray();
      y = publicKey.getW().getAffineY().toByteArray();

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchProviderException e) {
      // TODO(piperc): Auto-generated catch block
      e.printStackTrace();
    } catch (InvalidAlgorithmParameterException e) {
      // TODO(piperc): Auto-generated catch block
      e.printStackTrace();
    }

    EccKey ecc = new EccKey(x, y);
    AttestationData attData = new AttestationData(aaguid, aaguid, ecc);
    byte[] rpIdHash = Crypto.sha256Digest(
        ((request.isSecure() ? "https://" : "http://") + request.getHeader("Host")).getBytes());
    AuthenticatorData authData = new AuthenticatorData(rpIdHash, (byte) (1 << 6), 0, attData);
    FidoU2fAttestationStatement attStmt = new FidoU2fAttestationStatement();
    AttestationObject attObj = new AttestationObject(authData, "fido-u2f", attStmt);
    AuthenticatorAttestationResponse attRsp = new AuthenticatorAttestationResponse();
    attRsp.decodedObject = attObj;

    PublicKeyCredential pkc = new PublicKeyCredential(id, "", id.getBytes(), attRsp);
    Credential credential = new Credential(pkc);
    credential.save(currentUser);

    response.setContentType("application/json");
    response.getWriter().println(credential.toJson());
  }

}
