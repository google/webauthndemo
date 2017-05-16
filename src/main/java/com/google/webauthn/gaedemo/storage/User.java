// [START all]
package com.google.webauthn.gaedemo.storage;

import com.googlecode.objectify.*;
import com.googlecode.objectify.annotation.*;
import java.util.*;

/**
 * The @Entity tells Objectify about our entity. We also register it in OfyHelper.java -- very
 * important.
 *
 * This is never actually created, but gives a hint to Objectify about our Ancestor key.
 */
@Entity
public class User {
  @Id
  public String id;
}
// [END all]
