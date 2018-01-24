package com.google.webauthn.gaedemo.objects;

import org.junit.Test;

public class JsonTest {

  @Test
  public void testDecode() {
    PublicKeyCredentialUserEntity entity = new PublicKeyCredentialUserEntity("Casey", "cpiper".getBytes());
    System.out.println(entity.getJsonObject().toString());
  }

}
