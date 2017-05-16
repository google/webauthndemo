package com.google.webauthn.gaedemo.objects;

public enum Transport {
  USB("usb"), NFC("nfc"), BLE("ble");

  private String name;

  /**
   * @param name
   */
  private Transport(String name) {
    this.name = name;
  }

  /**
   * @param s
   * @return Transport corresponding to the input string
   */
  public static Transport decode(String s) {
    for (Transport t : Transport.values()) {
      if (t.name.equals(s)) {
        return t;
      }
    }
    throw new IllegalArgumentException(s + " not a valid Transport");
  }

  @Override
  public String toString() {
    return name;
  }
}
