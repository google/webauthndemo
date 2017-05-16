// Copyright 2014 Google Inc. All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.webauthn.gaedemo.storage;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.security.SecureRandom;

import com.googlecode.objectify.Work;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;

@Entity
public class SecretKeys {

  private static final int AES_KEY_SIZE = 16;

  @Id private String id;
  private byte[] sessionEncryptionKey;

  @Ignore private SecureRandom random = new SecureRandom();

  private static SecretKeys generate() {
    return ofy().transact(new Work<SecretKeys>() {
      @Override
      public SecretKeys run() {
        SecretKeys keys = ofy().load().type(SecretKeys.class).id("singleton").now();
        if (keys != null) {
          return keys;
        } else {
          keys = new SecretKeys();
          keys.generateNewKeys();
          ofy().save().entity(keys).now();
          return keys;
        }
      }
    });
  }

  public static SecretKeys get() {
    SecretKeys keys = ofy().load().type(SecretKeys.class).id("singleton").now();

    if (keys == null) {
      // somebody (we?) need to generate the keys
      return generate();
    } else {
      return keys;
    }
  }

  public SecretKeys() {
    id = "singleton";
  }

  private void generateNewKeys() {
    if (sessionEncryptionKey == null) {
      sessionEncryptionKey = new byte[AES_KEY_SIZE];
      random.nextBytes(sessionEncryptionKey);
    }
  }

  public byte[] sessionEncryptionKey() {
    return sessionEncryptionKey;
  }
}
