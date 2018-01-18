/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.webauthn.gaedemo.objects;

import com.google.webauthn.gaedemo.crypto.Crypto;
import com.google.webauthn.gaedemo.exceptions.WebAuthnException;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.fail;

public class CryptoTest {
  @Test
  public void testGetRSAPublicKey() {
    byte[] n = Hex.decode("A9E167983F39D55FF2A093415EA6798985C8355D9A915BFB1D01DA197026170FBDA522D035856D7A986614415CCFB7B7083B09C991B81969376DF9651E7BD9A93324A37F3BBBAF460186363432CB07035952FC858B3104B8CC18081448E64F1CFB5D60C4E05C1F53D37F53D86901F105F87A70D1BE83C65F38CF1C2CAA6AA7EB");
    byte[] e = Hex.decode("010001");
    Algorithm alg = Algorithm.RS256;
    RsaKey rsaPublicKey = new RsaKey(alg, n, e);
    try {
      Crypto.getRSAPublicKey(rsaPublicKey);
    } catch (WebAuthnException ex) {
      fail("WebAuthnException: " + ex.getMessage());
    }
  }

  @Test
  public void testECPublicKey() {
    byte[] x = Hex.decode("e383082691558e4b78cd80a5f9fa16f768bba6a93fb8801f8f5ad6f3cddd4f94");
    byte[] y = Hex.decode("c9dd9c82ec06bdcdaef0e8602a4900deb08fc3d925c86ae996c6ad4c36f3f179");
    Algorithm alg = Algorithm.ES256;
    EccKey eccPublicKey = new EccKey(alg, x, y);
    try {
      Crypto.getECPublicKey(eccPublicKey);
    } catch (WebAuthnException ex) {
      fail("WebAuthnException: " + ex.getMessage());
    }
  }
}
