package com.google.webauthn.gaedemo.objects;

public enum PublicKeyCredentialType {
  PUBLIC_KEY("public-key");
  String name;

  /**
   * @param name
   */
  private PublicKeyCredentialType(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
