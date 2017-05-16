package com.google.webauthn.gaedemo.exceptions;

public class WebAuthnException extends Exception {

  /**
   * @param string
   */
  public WebAuthnException(String string) {
    super(string);
  }
  
  public WebAuthnException(String message, Throwable cause) {
    super(message, cause);
  }
}
