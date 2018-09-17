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

package com.google.webauthn.gaedemo.storage;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.Objects;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonObject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

@Entity
public class AttestationSessionData {
  @Parent
  Key<User> user;
  @Id
  public Long id;

  String challenge;
  String origin;

  public AttestationSessionData(byte[] challenge, String origin) {
    this.challenge = BaseEncoding.base64().encode(challenge);
    this.origin = origin;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(challenge, origin, id, user);
  }

  boolean equalsThis(AttestationSessionData other) {
    return (this.challenge != null && other.challenge != null
        && this.challenge.equals(other.challenge))
        && (this.origin != null && other.origin != null && this.origin.equals(other.origin));
  }
  
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof AttestationSessionData)) {
      return false;
    }
    return this.equalsThis((AttestationSessionData)other);
  }

  public void save(String currentUser) {
    Key<User> user = Key.create(User.class, currentUser);
    this.user = user;
    ofy().save().entity(this).now();
  }

  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.addProperty("challenge", challenge);
    result.addProperty("origin", origin);
    return result;
  }

}
