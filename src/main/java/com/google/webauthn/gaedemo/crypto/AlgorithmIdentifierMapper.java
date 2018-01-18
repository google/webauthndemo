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

package com.google.webauthn.gaedemo.crypto;

import com.google.webauthn.gaedemo.objects.Algorithm;
import org.jose4j.jws.BaseSignatureAlgorithm;
import org.jose4j.jws.EcdsaUsingShaAlgorithm;
import org.jose4j.jws.RsaUsingShaAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class AlgorithmIdentifierMapper {
  private static final Map<Algorithm, BaseSignatureAlgorithm> map = new HashMap<>();

  static {
    map.put(Algorithm.ES256, new EcdsaUsingShaAlgorithm.EcdsaP256UsingSha256());
    map.put(Algorithm.ES384, new EcdsaUsingShaAlgorithm.EcdsaP384UsingSha384());
    map.put(Algorithm.ES512, new EcdsaUsingShaAlgorithm.EcdsaP521UsingSha512());
    map.put(Algorithm.RS256, new RsaUsingShaAlgorithm.RsaSha256());
    map.put(Algorithm.RS384, new RsaUsingShaAlgorithm.RsaSha384());
    map.put(Algorithm.RS512, new RsaUsingShaAlgorithm.RsaSha512());
    map.put(Algorithm.PS256, new RsaUsingShaAlgorithm.RsaPssSha256());
    map.put(Algorithm.PS384, new RsaUsingShaAlgorithm.RsaPssSha384());
    map.put(Algorithm.PS512, new RsaUsingShaAlgorithm.RsaPssSha512());
  }

  public static BaseSignatureAlgorithm get(Algorithm algorithm) {
    return map.get(algorithm);
  }
}
