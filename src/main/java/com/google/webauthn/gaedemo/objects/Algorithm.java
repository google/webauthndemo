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

package com.google.webauthn.gaedemo.objects;

/**
 * Algorithm enum differentiating between the supported asymmetric key algorithms
 */
public enum Algorithm {
  ES256("ES256"), ES384("ES384"), ES512("ES512"), RS256("RS256"), RS384("RS384"), RS512(
      "RS512"), PS256("PS256"), PS384("PS384"), PS512("PS512"),
	  UNDEFINED("undefined");
  final private String name;

  /**
   * @param name The string representation of the algorithm name
   */
  private Algorithm(String name) {
    this.name = name;
  }

  /**
   * @param alg The Algorithm to check
   * @return If the Algorithm is an ECC Algorithm
   */
  public static boolean isEccAlgorithm(Algorithm alg) {
    return alg == ES256 || alg == ES384 || alg == ES512;
  }

  /**
   * @param alg The Algorithm to check
   * @return If the Algorithm is an RSA Algorithm
   */
  public static boolean isRsaAlgorithm(Algorithm alg) {
    return alg == RS256 || alg == RS384 || alg == RS512 || alg == PS256 || alg == PS384
        || alg == PS512;
  }

  /**
   * @param s Input string to decode
   * @return Transport corresponding to the input string
   */
  public static Algorithm decode(String s) {
    for (Algorithm t : Algorithm.values()) {
      if (t.name.equals(s)) {
        return t;
      }
    }

    // COSE Algorithm Identifiers
    if (s.equals("-7")) {
      return ES256;
    }

    throw new IllegalArgumentException(s + " not a valid Algorithm");
  }

  /**
   * @param alg Input integer to decode
   * @return Transport corresponding to the input string
   */
  public static Algorithm decode(int alg) {
    switch (alg) {
      case -7:
        return ES256;
      case -35:
        return ES384;
      case -36:
        return ES512;
      case -37:
        return PS256;
      case -38:
        return PS384;
      case -39:
        return PS512;
      case -257:
        return RS256;
      case -258:
        return RS384;
      case -259:
        return RS512;
      case -260:
        return ES256;
      case -261:
        return ES512;
    }
    return Algorithm.UNDEFINED;
  }

  public int encodeToInt() {
    switch (this) {
      case ES256:
        return -7;
      case ES384:
        return -35;
      case ES512:
        return -36;
      case PS256:
        return -37;
      case PS384:
        return -38;
      case PS512:
        return -39;
      case RS256:
        return -257;
      case RS384:
        return -258;
      case RS512:
        return -259;
      default:
    }
    return -1;
  }

  @Override
  public String toString() {
    return name;
  }


  public Object toReadableString() {
    return name;
  }
}
