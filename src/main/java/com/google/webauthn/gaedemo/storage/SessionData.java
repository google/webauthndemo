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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.io.BaseEncoding;
import com.google.gson.JsonObject;
import com.google.webauthn.gaedemo.server.Datastore;

public class SessionData {
  public static final String KIND = "SessionData";
  private static final String CHALLENGE_PROPERTY = "challenge";
  private static final String ORIGIN_PROPERTY = "origin";
  private static final String TIMESTAMP_PROPERTY = "created";
  private static final String ID_PROPERTY = "id";
  private static final long HOUR_IN_MILLIS = (1 * 60 * 60 * 1000);

  private long id = 0;
  private String challenge;
  private String origin;
  private Date created;

  public SessionData() {
    this.created = new Date();
  }

  public SessionData(byte[] challenge, String origin) {
    this.challenge = BaseEncoding.base64().encode(challenge);
    this.origin = origin;
    this.created = new Date();
  }

  public SessionData(byte[] challenge, String origin, Date date) {
    this.challenge = BaseEncoding.base64().encode(challenge);
    this.origin = origin;
    this.created = date;
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

  /**
   * Save the session into the datastore.
   * 
   * @param currentUser
   */
  public void save(String currentUser) {
    Key parentKey = KeyFactory.createKey(User.KIND, currentUser);

    Entity session = new Entity(KIND, parentKey);
    session.setProperty(CHALLENGE_PROPERTY, challenge);
    session.setProperty(ORIGIN_PROPERTY, origin);
    session.setProperty(TIMESTAMP_PROPERTY, new Date());

    Key stored = Datastore.getDatastore().put(session);
    this.id = stored.getId();
  }

  /**
   * @return the id
   */
  public long getId() {
    return id;
  }

  private static List<Entity> loadEntities(String currentUser) {
    Key userKey = KeyFactory.createKey(User.KIND, currentUser);

    Query query = new Query(KIND).setAncestor(userKey);

    List<Entity> results =
        Datastore.getDatastore().prepare(query).asList(FetchOptions.Builder.withDefaults());

    return results;
  }

  /**
   * Load all sessions from datastore for a particular user.
   * 
   * @param currentUser
   * @return
   */
  public static List<SessionData> load(String currentUser) {
    List<Entity> sessionEntities = loadEntities(currentUser);

    ArrayList<SessionData> sessions = new ArrayList<>();
    for (Entity e : sessionEntities) {
      SessionData session =
          new SessionData(BaseEncoding.base64().decode((String) e.getProperty(CHALLENGE_PROPERTY)),
              (String) e.getProperty(ORIGIN_PROPERTY), (Date) e.getProperty(TIMESTAMP_PROPERTY));
      sessions.add(session);
    }

    return sessions;
  }

  /**
   * Remove all sessions for a particular user older than 1 hour.
   * 
   * @param currentUser
   */
  public static void removeOldSessions(String currentUser) {
    List<Entity> sessionEntities = loadEntities(currentUser);

    for (Entity e : sessionEntities) {
      Date sessionDate = (Date) e.getProperty(TIMESTAMP_PROPERTY);

      if (sessionDate.getTime() < System.currentTimeMillis() - HOUR_IN_MILLIS) {
        Datastore.getDatastore().delete(e.getKey());
      }
    }
  }

  /**
   * Remove all stale sessions older than 1 hour.
   */
  public static void removeAllOldSessions() {
    Filter filter = new FilterPredicate(TIMESTAMP_PROPERTY, FilterOperator.LESS_THAN_OR_EQUAL,
        new Date(System.currentTimeMillis() - HOUR_IN_MILLIS));
    Query query = new Query(KIND).setFilter(filter);

    List<Entity> results =
        Datastore.getDatastore().prepare(query).asList(FetchOptions.Builder.withDefaults());

    List<Key> keys = results.stream().map(entity -> entity.getKey()).collect(Collectors.toList());
    Datastore.getDatastore().delete(keys);
  }

  /**
   * Query datastore for a session for a particular user via id.
   * 
   * @param currentUser
   * @param id
   * @return
   */
  public static SessionData load(String currentUser, Long id) {
    Key userKey = KeyFactory.createKey(User.KIND, currentUser);
    Key sessionKey = KeyFactory.createKey(userKey, KIND, id);

    try {
      Entity e = Datastore.getDatastore().get(sessionKey);

      return new SessionData(
          BaseEncoding.base64().decode((String) e.getProperty(CHALLENGE_PROPERTY)),
          (String) e.getProperty(ORIGIN_PROPERTY), (Date) e.getProperty(TIMESTAMP_PROPERTY));
    } catch (EntityNotFoundException e1) {
      return null;
    }
  }

  /**
   * Remove a particular session for a user via id.
   * 
   * @param currentUser
   * @param id
   */
  public static void remove(String currentUser, Long id) {
    Key userKey = KeyFactory.createKey(User.KIND, currentUser);
    Key sessionKey = KeyFactory.createKey(userKey, KIND, id);
    Datastore.getDatastore().delete(sessionKey);
  }

  /**
   * @return JsonObject representation of the SessionData object.
   */
  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    if (id != 0L) {
      result.addProperty(ID_PROPERTY, id);
    }
    result.addProperty(CHALLENGE_PROPERTY, challenge);
    result.addProperty(ORIGIN_PROPERTY, origin);
    result.addProperty(TIMESTAMP_PROPERTY, created.toString());
    return result;
  }
}
