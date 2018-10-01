// Copyright 2018 Google Inc.
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

import com.googlecode.objectify.annotation.Subclass;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;

@Subclass
public final class NoneAttestationStatement extends AttestationStatement {

  public NoneAttestationStatement() {}

  @Override
  DataItem encode() throws CborException {
    Map result = new Map();
    return result;
  }
  
  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NoneAttestationStatement)
      return true;
    return false;
  }

  @Override
  public String getName() {
    return "NONE ATTESTATION";
  }
}
