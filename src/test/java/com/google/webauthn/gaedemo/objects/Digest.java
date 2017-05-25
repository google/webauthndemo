package com.google.webauthn.gaedemo.objects;

import com.google.common.io.BaseEncoding;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.junit.Test;

public class Digest {

  @Test
  public void test() throws NoSuchAlgorithmException {
    byte[] test = String.valueOf("Test").getBytes();
    
    SHA256Digest digest = new SHA256Digest();
    digest.update(test, 0, test.length);
    byte[] result = new byte[digest.getDigestSize()];
    digest.doFinal(result, 0);
    System.out.println("bouncy: " + BaseEncoding.base64().encode(result));

    
    MessageDigest md = MessageDigest.getInstance("SHA-256");

    md.update(test);
    byte[] digest2 = md.digest();
    System.out.println("md: " + BaseEncoding.base64().encode(digest2));
    System.out.println(digest2.length);
    
    
    
    
  }

}
