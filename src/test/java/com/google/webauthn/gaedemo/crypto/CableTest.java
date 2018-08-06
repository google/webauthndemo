/*
 * Copyright 2018 Google Inc. All Rights Reserved.
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

package com.google.webauthn.gaedemo.crypto;

import com.google.webauthn.gaedemo.objects.CablePairingData;
import com.google.webauthn.gaedemo.objects.CableSessionData;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class CableTest {

  @Test
  public void testGenerateSessionData_vectors() {
    Cable cable = new Cable(new Random() {
      @Override
      public void nextBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; ++i) {
          bytes[i] = (byte) 0xAA;
        }
      }
    });

    CablePairingData pairingData = new CablePairingData(
        1,
        Hex.decode("202122232425262728292A2B2C2D2E2F202122232425262728292A2B2C2D2E2F"),
        Hex.decode("101112131415161718191A1B1C1D1E1F101112131415161718191A1B1C1D1E1F"));

    CableSessionData actual = cable.generateSessionData(pairingData);

    CableSessionData expected = new CableSessionData(
        1,
        Hex.decode("AAAAAAAAAAAAAAAAC50F4E92238F1BE7"),
        Hex.decode("75B83487AE3DB1C1159C00EB992C984D"),
        Hex.decode("073B97D8D142EA3A04B16BD3DC81553334577A20F398DBBC02FBD18B9354BAD2"));
    Assert.assertEquals(actual, expected);
  }
}
