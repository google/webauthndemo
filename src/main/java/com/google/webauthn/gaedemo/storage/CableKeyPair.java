// Copyright 2019 Google Inc.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.io.BaseEncoding;
import com.google.webauthn.gaedemo.server.Datastore;

public class CableKeyPair {
  public static final String KIND = "CableKeyPair";
  public static final String KEY_PAIR_PROPERTY = "keypair";
  public static final String TIMESTAMP_PROPERTY = "created";
  private static final long HOUR_IN_MILLIS = (1 * 60 * 60 * 1000);
  private KeyPair keyPair;
  private long id;

  public CableKeyPair(KeyPair keyPair) {
    this.keyPair = keyPair;
  }

  public long save(Long sessionId) throws IOException {
    Key parentKey = KeyFactory.createKey(SessionData.KIND, sessionId);

    Entity keyEntity = new Entity(KIND, parentKey);

    // Serialize the KeyPair
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(baos);
    out.writeObject(keyPair);

    keyEntity.setProperty(KEY_PAIR_PROPERTY, BaseEncoding.base64().encode(baos.toByteArray()));
    keyEntity.setProperty(TIMESTAMP_PROPERTY, new Date());

    Key stored = Datastore.getDatastore().put(keyEntity);
    this.id = stored.getId();
    return id;
  }

  public static KeyPair get(Long sessionId) throws IOException {
    Key sessionKey = KeyFactory.createKey(SessionData.KIND, sessionId);

    Query query = new Query(KIND).setAncestor(sessionKey);

    List<Entity> results =
        Datastore.getDatastore().prepare(query).asList(FetchOptions.Builder.withDefaults());
    
    for (Entity result : results) {
      if (result.getParent().getId() == sessionId.longValue()) {
        byte[] serializedKeyPair =
            BaseEncoding.base64().decode((String) result.getProperty(KEY_PAIR_PROPERTY));
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedKeyPair));
        try {
          return (KeyPair) in.readObject();
        } catch (ClassNotFoundException e1) {
          throw new IOException(e1);
        }
      }
    }
    throw new IOException("KeyPair " + String.valueOf(sessionId) + "not found");
  }

  /**
   * Remove all stale sessions older than 1 hour.
   */
  public static void removeAllOldKeyPairs() {
    Filter filter = new FilterPredicate(TIMESTAMP_PROPERTY, FilterOperator.LESS_THAN_OR_EQUAL,
        new Date(System.currentTimeMillis() - HOUR_IN_MILLIS));
    Query query = new Query(KIND).setFilter(filter);

    List<Entity> results =
        Datastore.getDatastore().prepare(query).asList(FetchOptions.Builder.withDefaults());

    List<Key> keys = results.stream().map(entity -> entity.getKey()).collect(Collectors.toList());
    Datastore.getDatastore().delete(keys);
  }
}
