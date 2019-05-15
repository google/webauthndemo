package com.google.webauthn.gaedemo.objects;

public interface AttestationExtension {
  public enum Type {
    CABLE,
  }

  public Type getType();
}
