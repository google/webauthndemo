package com.google.webauthn.gaedemo.crypto;

import com.google.webauthn.gaedemo.exceptions.WebAuthnException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

public class Crypto {
  
  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static byte[] sha256Digest(byte[] input) {
    SHA256Digest digest = new SHA256Digest();
    digest.update(input, 0, input.length);
    byte[] result = new byte[digest.getDigestSize()];
    digest.doFinal(result, 0);
    return result;
  }
  
  public static byte[] digest(byte[] input, String alg) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance(alg);
    return digest.digest(input);
  }

  public static boolean verifySignature(PublicKey publicKey, byte[] signedBytes,
      byte[] signature) throws WebAuthnException {
    try {
      Signature ecdsaSignature = Signature.getInstance("SHA256withECDSA");
      ecdsaSignature.initVerify(publicKey);
      ecdsaSignature.update(signedBytes);
      return ecdsaSignature.verify(signature);
    } catch (InvalidKeyException e) {
      throw new WebAuthnException("Error when verifying signature", e);
    } catch (SignatureException e) {
      throw new WebAuthnException("Error when verifying signature", e);
    } catch (NoSuchAlgorithmException e) {
      throw new WebAuthnException("Error when verifying signature", e);
    }
  }

  public static PublicKey decodePublicKey(byte[] x, byte[] y) throws WebAuthnException {
    try {
      X9ECParameters curve = SECNamedCurves.getByName("secp256r1");
      ECPoint point;
      try {
        point = curve.getCurve().createPoint(new BigInteger(x), new BigInteger(y));
      } catch (RuntimeException e) {
        throw new WebAuthnException("Couldn't parse user public key", e);
      }
      
      return KeyFactory.getInstance("ECDSA").generatePublic(
          new ECPublicKeySpec(point,
              new ECParameterSpec(
                  curve.getCurve(),
                  curve.getG(),
                  curve.getN(),
                  curve.getH())));
    } catch (InvalidKeySpecException e) {
      throw new WebAuthnException("Error when decoding public key", e);
    } catch (NoSuchAlgorithmException e) {
      throw new WebAuthnException("Error when decoding public key", e);
    }
  }
}
