/**
 * Copyright 2014-2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
// [START all]
package com.google.webauthn.gaedemo.server;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import com.example.guestbook.Greeting;
import com.example.guestbook.Guestbook;
import com.google.webauthn.gaedemo.objects.AndroidSafetyNetAttestationStatement;
import com.google.webauthn.gaedemo.objects.AuthenticatorAttestationResponse;
import com.google.webauthn.gaedemo.objects.EccKey;
import com.google.webauthn.gaedemo.objects.FidoU2fAttestationStatement;
import com.google.webauthn.gaedemo.objects.RsaKey;
import com.google.webauthn.gaedemo.storage.AssertionSessionData;
import com.google.webauthn.gaedemo.storage.AttestationSessionData;
import com.google.webauthn.gaedemo.storage.Credential;
import com.google.webauthn.gaedemo.storage.User;
import com.googlecode.objectify.ObjectifyService;


/**
 * OfyHelper, a ServletContextListener, is setup in web.xml to run before a JSP is run. This is
 * required to let JSP's access Ofy.
 **/
public class OfyHelper implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    // This will be invoked as part of a warmup request, or the first user request if no warmup
    // request.
    ObjectifyService.register(Guestbook.class);
    ObjectifyService.register(Greeting.class);
    ObjectifyService.register(User.class);
    ObjectifyService.register(Credential.class);
    ObjectifyService.register(AttestationSessionData.class);
    ObjectifyService.register(AssertionSessionData.class);
    ObjectifyService.register(AuthenticatorAttestationResponse.class);
    ObjectifyService.register(RsaKey.class);
    ObjectifyService.register(EccKey.class);
    ObjectifyService.register(FidoU2fAttestationStatement.class);
    ObjectifyService.register(AndroidSafetyNetAttestationStatement.class);
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    // App Engine does not currently invoke this method.
  }
}
// [END all]
