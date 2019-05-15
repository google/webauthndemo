/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.google.webauthn.gaedemo.crypto;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.jose4j.jws.EcdsaUsingShaAlgorithm;

import com.google.common.primitives.Bytes;
import com.google.webauthn.gaedemo.exceptions.WebAuthnException;
import com.google.webauthn.gaedemo.objects.EccKey;
import com.google.webauthn.gaedemo.objects.RsaKey;

public class Crypto {

  private static int EC_COORDINATE_LENGTH = 32;
  private static int EC_PUBLIC_KEY_LENGTH = 65;
  private static byte EC_PUBLIC_KEY_PREFIX = 0x4;

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

  public static byte[] hmacSha256(byte[] key, byte[] data, int outputLength) {
    HMac hmac = new HMac(new SHA256Digest());
    hmac.init(new KeyParameter(key));
    hmac.update(data, 0, data.length);
    byte[] output = new byte[hmac.getMacSize()];
    hmac.doFinal(output, 0);
    return Arrays.copyOf(output, outputLength);
  }

  public static byte[] hkdfSha256(byte[] ikm, byte[] salt, byte[] info, int outputLength) {
    byte[] output = new byte[outputLength];
    HKDFParameters params = new HKDFParameters(ikm, salt, info);
    HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
    hkdf.init(params);
    hkdf.generateBytes(output, 0, outputLength);
    return output;
  }

  public static byte[] digest(byte[] input, String alg) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance(alg);
    return digest.digest(input);
  }

  public static boolean verifySignature(PublicKey publicKey, byte[] signedBytes, byte[] signature)
      throws WebAuthnException {
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

  // TODO add test for this.
  public static boolean verifySignature(X509Certificate attestationCertificate, byte[] signedBytes,
      byte[] signature) throws WebAuthnException {
    return verifySignature(attestationCertificate.getPublicKey(), signedBytes, signature);
  }

  public static boolean verifySignature(X509Certificate attestationCertificate, byte[] signedBytes,
      byte[] signature, String signatureAlgorithm) throws WebAuthnException {
    return verifySignature(attestationCertificate.getPublicKey(), signedBytes, signature,
        signatureAlgorithm);
  }

  public static boolean verifySignature(PublicKey publicKey, byte[] message, byte[] signature,
      String signatureAlgorithm) throws WebAuthnException {
    if (signatureAlgorithm == null || signatureAlgorithm.isEmpty()) {
      throw new WebAuthnException("Signature algorithm is null or empty");
    }
    try {
      Signature sig = Signature.getInstance(signatureAlgorithm);
      sig.initVerify(publicKey);
      sig.update(message);
      return sig.verify(signature);
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      throw new WebAuthnException("Error when verifying signature", e);
    }
  }

  public static PublicKey decodePublicKey(byte[] encodedPublicKey) throws WebAuthnException {
    return decodePublicKey(Arrays.copyOfRange(encodedPublicKey, 1, 1 + EC_COORDINATE_LENGTH),
        Arrays.copyOfRange(encodedPublicKey, 1 + EC_COORDINATE_LENGTH, encodedPublicKey.length));
  }

  public static PublicKey decodePublicKey(byte[] x, byte[] y) throws WebAuthnException {
    try {
      X9ECParameters curve = SECNamedCurves.getByName("secp256r1");
      ECPoint point;
      try {
        byte[] encodedPublicKey = Bytes.concat(new byte[] {0x04}, x, y);
        point = curve.getCurve().decodePoint(encodedPublicKey);
      } catch (RuntimeException e) {
        throw new WebAuthnException("Couldn't parse user public key", e);
      }

      return KeyFactory.getInstance("ECDSA").generatePublic(new ECPublicKeySpec(point,
          new ECParameterSpec(curve.getCurve(), curve.getG(), curve.getN(), curve.getH())));
    } catch (InvalidKeySpecException e) {
      throw new WebAuthnException("Error when decoding public key", e);
    } catch (NoSuchAlgorithmException e) {
      throw new WebAuthnException("Error when decoding public key", e);
    }
  }

  public static PublicKey getRSAPublicKey(RsaKey rsaKey) throws WebAuthnException {
    BigInteger modulus = new BigInteger(Bytes.concat(new byte[] {0}, rsaKey.getN()));
    BigInteger publicExponent = new BigInteger(rsaKey.getE());
    try {
      return getRSAPublicKey(modulus, publicExponent);
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      throw new WebAuthnException("Error when generate RSA public key", e);
    }
  }

  public static byte[] compressECPublicKey(ECPublicKey publicKey) {
    byte[] x = publicKey.getW().getAffineX().toByteArray();
    byte[] y = publicKey.getW().getAffineY().toByteArray();

    byte[] output = new byte[EC_PUBLIC_KEY_LENGTH];
    System.arraycopy(y, y.length - EC_COORDINATE_LENGTH, output,
        output.length - EC_COORDINATE_LENGTH, EC_COORDINATE_LENGTH);
    System.arraycopy(x, x.length - EC_COORDINATE_LENGTH, output, 1, EC_COORDINATE_LENGTH);
    output[0] = EC_PUBLIC_KEY_PREFIX;

    return output;
  }

  public static PublicKey getECPublicKey(EccKey eccKey) throws WebAuthnException {
    BigInteger x = new BigInteger(eccKey.getX());
    BigInteger y = new BigInteger(eccKey.getY());
    java.security.spec.ECPoint w = new java.security.spec.ECPoint(x, y);
    EcdsaUsingShaAlgorithm algorithm =
        (EcdsaUsingShaAlgorithm) AlgorithmIdentifierMapper.get(eccKey.getAlg());
    String curveName = algorithm.getCurveName();
    try {
      return getECPublicKey(w, curveName);
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      throw new WebAuthnException("Error when generate EC public key", e);
    }
  }

  public static PublicKey getRSAPublicKey(BigInteger n, BigInteger e)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    KeySpec keySpec = new RSAPublicKeySpec(n, e);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(keySpec);
  }

  public static PublicKey getECPublicKey(java.security.spec.ECPoint w, String stdCurveName)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec(stdCurveName);
    java.security.spec.ECParameterSpec params = new ECNamedCurveSpec(parameterSpec.getName(),
        parameterSpec.getCurve(), parameterSpec.getG(), parameterSpec.getN(), parameterSpec.getH(),
        parameterSpec.getSeed());
    KeySpec keySpec = new java.security.spec.ECPublicKeySpec(w, params);
    KeyFactory keyFactory = KeyFactory.getInstance("EC");
    return keyFactory.generatePublic(keySpec);
  }

  public static KeyPair generateKeyPair() {
    try {
      ECGenParameterSpec spec = new ECGenParameterSpec("secp256r1");
      KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
      gen.initialize(spec);
      KeyPair keyPair = gen.generateKeyPair();
      return keyPair;
    } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] getS(PrivateKey privateKey, byte[] publicKey) {
    try {
      KeyAgreement agreement = KeyAgreement.getInstance("ECDH");
      agreement.init(privateKey);
      agreement.doPhase(decodePublicKey(publicKey), true);

      return agreement.generateSecret();
    } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException
        | WebAuthnException e) {
      throw new RuntimeException(e);
    }
  }
}
