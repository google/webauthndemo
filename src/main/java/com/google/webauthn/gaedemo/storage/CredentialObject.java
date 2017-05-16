package com.google.webauthn.gaedemo.storage;

import com.google.webauthn.gaedemo.objects.PublicKeyCredentialType;
import com.google.webauthn.gaedemo.objects.Transport;
import java.io.Serializable;
import java.util.*;

public class CredentialObject implements Serializable {
  PublicKeyCredentialType type;
  byte[] handle;
  ArrayList<Transport> transports;
}
