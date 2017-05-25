package com.google.webauthn.gaedemo.server;

import com.google.common.io.BaseEncoding;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.storage.AssertionSessionData;
import java.util.Arrays;
import java.util.logging.Logger;

public abstract class Server {
  private static final Logger Log = Logger.getLogger(U2fServer.class.getName());

  public static void verifySessionAndChallenge(AuthenticatorAttestationResponse assertionResponse,
      String currentUser, String sessionId) throws ResponseException {
    Log.info("-- Verifying provided session and challenge data --");

    long id = 0;
    try {
      id = Long.valueOf(sessionId);
    } catch (NumberFormatException e) {
      throw new ResponseException("Provided session id invalid");
    }

    AssertionSessionData session = AssertionSessionData.load(currentUser, Long.valueOf(id));
    if (session == null) {
      throw new ResponseException("Session invalid");
    }

    byte[] sessionChallenge = BaseEncoding.base64().decode(session.getChallenge());
    if (!Arrays.equals(sessionChallenge,
        assertionResponse.getClientData().getChallenge().getBytes())) {
      throw new ResponseException("Returned challenge incorrect");
    }
    Log.info("Successfully verified session and challenge data");
  }
}
