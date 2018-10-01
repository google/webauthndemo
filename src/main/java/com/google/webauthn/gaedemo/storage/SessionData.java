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

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonObject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
public class SessionData {
  @Parent
  Key<User> user;
  @Id
  public Long id;

  private String challenge;
  private String origin;
  @Index
  private Date created;

  public SessionData() {
    this.created = new Date();
  }

  public SessionData(byte[] challenge, String origin) {
    this.challenge = BaseEncoding.base64().encode(challenge);
    this.origin = origin;
    this.created = new Date();
  }

  /**
   * @return the challenge
   */
  public String getChallenge() {
    return challenge;
  }

  /**
   * @return the origin
   */
  public String getOrigin() {
    return origin;
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, id, challenge, origin, created);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SessionData)) {
      return false;
    }
    SessionData other = (SessionData) obj;
    return (this.challenge != null && other.challenge != null
        && this.challenge.equals(other.challenge))
        && (this.origin != null && other.origin != null && this.origin.equals(other.origin));
  }

  public void save(String currentUser) {
    Key<User> user = Key.create(User.class, currentUser);
    this.user = user;
    ofy().save().entity(this).now();
  }

  public static List<SessionData> load(String currentUser) {
    Key<User> user = Key.create(User.class, currentUser);
    List<SessionData> sessions = ofy().load().type(SessionData.class).ancestor(user).list();
    return sessions;
  }

  public static void removeOldSessions(String currentUser) {
    Key<User> user = Key.create(User.class, currentUser);
    Date date = new Date(System.currentTimeMillis() - (1 * 60 * 60 * 1000));
    List<Key<SessionData>> keys = ofy().load().type(SessionData.class).ancestor(user)
        .filter("created < ", date).keys().list();
    if (keys.size() > 0) {
      ofy().delete().keys(keys).now();
    }
  }

  public static void removeAllOldSessions() {
    Date date = new Date(System.currentTimeMillis() - (1 * 60 * 60 * 1000));
    List<Key<SessionData>> keys =
        ofy().load().type(SessionData.class).filter("created < ", date).keys().list();
    if (keys.size() > 0) {
      ofy().delete().keys(keys).now();
    }
  }

  public static SessionData load(String currentUser, Long id) {
    Key<User> user = Key.create(User.class, currentUser);
    Key<SessionData> session = Key.create(user, SessionData.class, id);
    return ofy().load().key(session).now();
  }

  public static void remove(String currentUser, Long id) {
    Key<User> user = Key.create(User.class, currentUser);
    Key<SessionData> session = Key.create(user, SessionData.class, id);
    ofy().delete().key(session).now();
  }

  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.addProperty("id", id);
    result.addProperty("challenge", challenge);
    result.addProperty("origin", origin);
    return result;
  }
}
