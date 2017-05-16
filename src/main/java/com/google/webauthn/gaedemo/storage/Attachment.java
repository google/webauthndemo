package com.google.webauthn.gaedemo.storage;

public enum Attachment {
  PLATFORM("platform"), CROSS_PLATFORM("cross-platform");

  String name;

  private Attachment(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

}
