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

import com.google.gson.*;
import com.google.webauthn.gaedemo.exceptions.ResponseException;
import com.google.webauthn.gaedemo.objects.PublicKeyCredential;
import com.googlecode.objectify.*;
import com.googlecode.objectify.annotation.*;
import java.util.Date;
import java.util.List;

@Entity
public class Credential {
  @Parent
  Key<User> user;
  @Id
  public Long id;

  private Date date;
  private long signCount;
  private PublicKeyCredential credential;

  public Credential() {
    signCount = 0;
    date = new Date();
  }

  public Credential(String json) {
    signCount = 0;
    Gson gson = new Gson();
    credential = gson.fromJson(json, PublicKeyCredential.class);
    date = new Date();
  }

  public Credential(PublicKeyCredential credential) {
    this.signCount = 0;
    this.credential = credential;
    this.date = new Date();
  }

  public void validate() throws ResponseException {
    if (credential == null) {
      throw new ResponseException("Credentials invalid");
    }
  }

  public String toJson() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  public void save(String currentUser) {
    Key<User> user = Key.create(User.class, currentUser);
    this.user = user;
    ofy().save().entity(this).now();
  }

  public static List<Credential> load(String currentUser) {
    Key<User> user = Key.create(User.class, currentUser);
    List<Credential> credentials = ofy().load().type(Credential.class).ancestor(user).list();
    return credentials;
  }

  public static void remove(String currentUser, String id) {
    Key<User> user = Key.create(User.class, currentUser);
    ofy().delete().type(Credential.class).parent(user).id(Long.valueOf(id)).now();
  }

  /**
   * @return the credential
   */
  public PublicKeyCredential getCredential() {
    return credential;
  }

  /**
   * @return the signCount
   */
  public long getSignCount() {
    return signCount;
  }

  /**
   * @param signCount
   */
  public void updateSignCount(long signCount) {
    this.signCount = signCount;
    ofy().save().entity(this).now();
  }

  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }

}
