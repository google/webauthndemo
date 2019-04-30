package com.google.webauthn.gaedemo.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class Datastore {
  private static DatastoreService datastore = null;

  public static DatastoreService getDatastore() {
    if (datastore == null) {
      synchronized (Datastore.class) {
        if (datastore == null) {
          datastore = DatastoreServiceFactory.getDatastoreService();
        }
      }
    }
    return datastore;
  }

}
