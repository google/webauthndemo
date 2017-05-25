package com.google.webauthn.gaedemo.server;

import co.nstant.in.cbor.CborException;
import com.google.common.primitives.Bytes;
import com.google.webauthn.gaedemo.crypto.OfflineVerify;
import com.google.webauthn.gaedemo.crypto.OfflineVerify.AttestationStatement;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.objects.AndroidSafetyNetAttestationStatement;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.google.webauthn.gaedemo.storage.Credential;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;

public class AndroidSafetyNetServer extends Server {
  private static final Logger Log = Logger.getLogger(AndroidSafetyNetServer.class.getName());

  /**
   * @param cred
   * @param rpId
   * @param session
   * @param currentUser
   * @throws ServletException
   */
  public static void registerCredential(PublicKeyCredential cred, String currentUser,
      String session, String rpId) throws ServletException {

    if (!(cred.getResponse() instanceof AuthenticatorAttestationResponse)) {
      throw new ServletException("Invalid response structure");
    }

    AuthenticatorAttestationResponse attResponse =
        (AuthenticatorAttestationResponse) cred.getResponse();

    List<Credential> savedCreds = Credential.load(currentUser);
    for (Credential c : savedCreds) {
      if (c.getCredential().id.equals(cred.id)) {
        throw new ServletException("Credential already registerd for this user");
      }
    }
    
    try {
      verifySessionAndChallenge(attResponse, currentUser, session);
    } catch (ResponseException e1) {
      throw new ServletException("Unable to verify session and challenge data");
    }

    if (!attResponse.getClientData().getOrigin().equals(rpId)) {
      throw new ServletException("Couldn't verify client data");
    }
    
    AndroidSafetyNetAttestationStatement attStmt =
        (AndroidSafetyNetAttestationStatement) attResponse.decodedObject.getAttestationStatement();

    AttestationStatement stmt = OfflineVerify.parseAndVerify(new String(attStmt.response));
    if (stmt == null) {
      Log.info("Failure: Failed to parse and verify the attestation statement.");
      throw new ServletException("Failed to verify attestation statement");
    }

    try {
      byte[] expectedNonce =
          Bytes.concat(attResponse.getAttestationObject().getAuthenticatorData().encode(),
              attResponse.getClientData().getHash());
      if (!Arrays.equals(expectedNonce, stmt.getNonce())) {
        throw new ServletException("Nonce does not match");
      }
    } catch (CborException e) {
      throw new ServletException("Error encoding authdata");
    }

    if (!stmt.isCtsProfileMatch()) {
      throw new ServletException("No cts profile match");
    }

  }
}
