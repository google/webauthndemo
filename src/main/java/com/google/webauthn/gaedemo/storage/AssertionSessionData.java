package com.google.webauthn.gaedemo.storage;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonObject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import java.util.List;

@Entity
public class AssertionSessionData {
  @Parent
  Key<User> user;
  @Id
  public Long id;

  private String challenge;
  private String origin;

  public AssertionSessionData() {

  }
  
  public AssertionSessionData(byte[] challenge, String origin) {
    this.challenge = BaseEncoding.base64().encode(challenge);
    this.origin = origin;
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

  public static List<AssertionSessionData> load(String currentUser) {
    Key<User> user = Key.create(User.class, currentUser);
    List<AssertionSessionData> sessions =
        ofy().load().type(AssertionSessionData.class).ancestor(user).list();
    return sessions;
  }

  public static AssertionSessionData load(String currentUser, Long id) {
    Key<User> user = Key.create(User.class, currentUser);
    Key<AssertionSessionData> session = Key.create(user, AssertionSessionData.class, id);
    return ofy().load().key(session).now();
  }

  public JsonObject getJsonObject() {
    JsonObject result = new JsonObject();
    result.addProperty("id", id);
    result.addProperty("challenge", challenge);
    result.addProperty("origin", origin);
    return result;
  }
}
