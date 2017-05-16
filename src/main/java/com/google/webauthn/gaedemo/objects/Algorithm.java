package com.google.webauthn.gaedemo.objects;

public enum Algorithm {
  ES256("ES256"), ES384("ES384"), ES512("ES512"), RS256("RS256"), RS384("RS384"), RS512(
      "RS512"), PS256("PS256"), PS384("PS384"), PS512("PS512"), UNDEFINED("undefined");

  private String name;

  /**
   * @param name
   */
  private Algorithm(String name) {
    this.name = name;
  }

  public static boolean isEccAlgorithm(Algorithm alg) {
    return alg == ES256 || alg == ES384 || alg == ES512;
  }

  public static boolean isRsaAlgorithm(Algorithm alg) {
    return alg == RS256 || alg == RS384 || alg == RS512 || alg == PS256 || alg == PS384
        || alg == PS512;
  }

  /**
   * @param s
   * @return Transport corresponding to the input string
   */
  public static Algorithm decode(String s) {
    for (Algorithm t : Algorithm.values()) {
      if (t.name.equals(s)) {
        return t;
      }
    }
    throw new IllegalArgumentException(s + " not a valid Algorithm");
  }

  @Override
  public String toString() {
    return name;
  }
}
