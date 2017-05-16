package com.google.webauthn.gaedemo.storage;

import static com.googlecode.objectify.ObjectifyService.ofy;

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

  boolean equals(AttestationSessionData other) {
    return (this.challenge != null && other.challenge != null
        && this.challenge.equals(other.challenge))
        && (this.origin != null && other.origin != null && this.origin.equals(other.origin));
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
