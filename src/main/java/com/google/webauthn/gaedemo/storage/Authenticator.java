package com.google.webauthn.gaedemo.storage;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.google.appengine.api.datastore.ShortBlob;
import com.googlecode.objectify.Key;

@Entity
public class Authenticator {
  @Parent Key<User> theUser;
  @Id public Long id;
  
  public ShortBlob keyHandle;
  public ShortBlob publicKey;
}
