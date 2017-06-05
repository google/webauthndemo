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

package com.google.webauthn.gaedemo.server;

import com.google.common.io.BaseEncoding;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.objects.AuthenticatorResponse;
import com.google.webauthn.gaedemo.storage.SessionData;
import java.util.Arrays;
import java.util.logging.Logger;

public abstract class Server {
  private static final Logger Log = Logger.getLogger(U2fServer.class.getName());

  public static void verifySessionAndChallenge(AuthenticatorResponse assertionResponse,
      String currentUser, String sessionId) throws ResponseException {
    Log.info("-- Verifying provided session and challenge data --");

    long id = 0;
    try {
      id = Long.valueOf(sessionId);
    } catch (NumberFormatException e) {
      throw new ResponseException("Provided session id invalid");
    }

    /* Invalidate old sessions */
    SessionData.removeOldSessions(currentUser);

    SessionData session = SessionData.load(currentUser, Long.valueOf(id));
    if (session == null) {
      throw new ResponseException("Session invalid");
    }
    SessionData.remove(currentUser, Long.valueOf(id));

    byte[] sessionChallenge = BaseEncoding.base64().decode(session.getChallenge());
    if (!Arrays.equals(sessionChallenge,
        assertionResponse.getClientData().getChallenge().getBytes())) {
      throw new ResponseException("Returned challenge incorrect");
    }
    Log.info("Successfully verified session and challenge data");
  }
}
