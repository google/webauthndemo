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

async function fetchCredentials() {
  const response = await _fetch('/RegisteredKeys').catch(err => {
    showMessage(`An error occurred during fetch [${err.toString()}]`);
  });
  app.credentials = response;
}

async function removeCredential(id) {
  try {
    await _fetch('/RemoveCredential', { credentialId : id });
  } catch (err) {
    showMessage(`An error occurred during removal [${err.toString()}]`);
  }
  fetchCredentials();
}

async function addCredential() {
  app.active = true;

  try {
    const advancedOptions = {};

    if (app.advanced) {
      advancedOptions.requireResidentKey = app.requireResidentKey;
      advancedOptions.excludeCredentials = app.excludeCredentials;
      advancedOptions.userVerification = app.userVerification;
      advancedOptions.authenticatorAttachment = app.authenticatorAttachment;
      advancedOptions.attestationConveyancePreference = app.attestationConveyancePreference;
    }

    const _options = await _fetch('/BeginMakeCredential', {
      advanced: app.advanced,
      advancedOptions: JSON.stringify(advancedOptions)
    });

    const options = {};
    /**
     * interface MakePublicKeyCredentialOptions {
     *   rp: PublicKeyCredentialRpEntity;
     *   user: PublicKeyCredentialUserEntity;
     *   challenge: BufferSource;
     *   pubKeyCredParams: PublicKeyCredentialParameters[];
     *   timeout?: number;
     *   excludeCredentials?: PublicKeyCredentialDescriptor[];
     *   authenticatorSelection?: AuthenticatorSelectionCriteria;
     *   attestation?: AttestationConveyancePreference;
     *   extensions?: any;
     * }
     */

    options.rp = _options.rp;
    /**
     * interface PublicKeyCredentialRpEntity {
     *   id: string;
     *   name: string;
     * }
     */
    options.user = _options.user;
    options.user.id = strToBin(_options.user.id);
    /**
     * interface PublicKeyCredentialUserEntity {
     *   id: BufferSource;
     *   name: string;
     *   displayName: string;
     * }
     */
    options.challenge = strToBin(_options.challenge);
    options.pubKeyCredParams = _options.pubKeyCredParams;
    /**
     * interface PublicKeyCredentialParameters {
     *   type: 'public-key';
     *   alg: number;
     * }
     */

    // Optional parameters
    if ('timeout' in _options) {
      options.timeout = _options.timeout;
    }
    if ('excludeCredentials' in _options) {
      options.excludeCredentials = convertCredentialList(_options.excludeCredentials);
      /**
       * interface PublicKeyCredentialDescriptor {
       *   type: 'public-key';
       *   id: BufferSource;
       *   transports?: AuthenticatorTransport[];
       * }
       * 
       * type AuthenticatorTransport = 'usb'|'nfc'|'ble';
       */
    }
    if ('authenticatorSelection' in _options) {
      options.authenticatorSelection = _options.authenticatorSelection;
      /**
       * interface AuthenticatorSelectionCriteria {
       *   authenticatorAttachment?: AuthenticatorAttachment;
       *   requireResidentKey?: boolean;
       *   requireUserVerification?: string;
       * }
       * 
       * type AuthenticatorAttachment = 'platform'|'cross-platform';
       */
    }
    if ('attestation' in _options) {
      options.attestation = _options.attestation;
      /**
       * type AttestationConveyancePreference = 'none'|'indirect'|'direct';
       */
    }
    if ('extensions' in _options) {
      options.extensions = _options.extensions;
    }

    console.log('`navigator.credentials.create()` request:', options);

    // Create public key and attestation object
    const credential = await navigator.credentials.create({
      publicKey: options
    }).catch(() => {
      throw 'Credential creation failed.';
    });

    console.log('`navigator.credentials.create()`result:', credential);

    app.active = false;

    const attestation = {};
    /**
     * interface PublicKeyCredential {
     *   id: string;
     *   readonly type: 'public-key';
     *   readonly rawId: ArrayBuffer;
     *   readonly response: AuthenticatorAttestationResponse;
     * }
     */

    if ('id' in credential) {
      attestation.id = credential.id;
    }
    if ('type' in credential) {
      attestation.type = credential.type;
    }
    if ('rawId' in credential) {
      attestation.rawId = binToStr(credential.rawId);
    }
    if (!credential.response) {
      throw "Make Credential response lacking 'response' attribute";
    }

    const response = credential.response;
    /**
     * interface AuthenticatorAttestationResponse extends AuthenticatorResponse {
     *   readonly attestationObject: ArrayBuffer;
     * }
     * 
     * interface AuthenticatorResponse {
     *   readonly clientDataJSON: ArrayBuffer;
     * }
     */
    attestation.response = {
      clientDataJSON:     binToStr(response.clientDataJSON),
      attestationObject:  binToStr(response.attestationObject)
    };

    const result = await _fetch('/FinishMakeCredential', {
      data: JSON.stringify(attestation),
      session: _options.session.id
    });

    if (result && result.success) {
      showMessage(result.message);
      fetchCredentials();
    } else {
      throw 'Unexpected response received.';
    }
  } catch (err) {
    app.active = false;

    showMessage(`An error occurred during Make Credential operation [${err.toString()}]`);
  }
}

async function getAssertion() {
  app.active = true;

  try {
    const _options = await _fetch('/BeginGetAssertion');
    const options = {};
    /**
     * interface PublicKeyCredentialRequestOptions {
     *   challenge: BufferSource;
     *   timeout: number;
     *   rpId: string;
     *   allowCredentials: PublicKeyCredentialDescriptor[];
     *   userVerification?: 'required' | 'preferred' | 'discouraged';
     *   extensions?: any;
     * }
     */

    options.challenge = strToBin(_options.challenge);
    if ('timeout' in _options) {
      options.timeout = _options.timeout;
    }
    if ('rpId' in _options) {
      options.rpId = _options.rpId;
    }
    if ('allowCredentials' in _options) {
      options.allowCredentials = convertCredentialList(_options.allowCredentials);
      /**
       * interface PublicKeyCredentialDescriptor {
       *   type: 'public-key';
       *   id: BufferSource;
       *   transports?: string[];
       * }
       */
    }

    console.log('`navigator.credentials.get()` request:', options);

    const credential = await navigator.credentials.get({
      publicKey:options 
    }).catch(() => {
      throw 'Authentication failed';
    });

    console.log('`navigator.credentials.get()` result:', credential);

    app.active = false;

    const assertion = {};
    /**
     * interface PublicKeyCredential {
     *   id: string;
     *   readonly type: 'public-key';
     *   readonly rawId: ArrayBuffer;
     *   readonly response: AuthenticatorAssertionResponse;
     * }
     */

    if ('id' in credential) {
      assertion.id = credential.id;
    }
    if ('type' in credential) {
      assertion.type = credential.type;
    }
    if ('rawId' in credential) {
      assertion.rawId = binToStr(credential.rawId);
    }
    if (!credential.response) {
      throw "Get assertion response lacking 'response' attribute";
    }

    const response = credential.response;
    /**
     * interface AuthenticatorAssertionResponse extends AuthenticatorResponse {
     *   readonly authenticatorData: ArrayBuffer;
     *   readonly signature: ArrayBuffer;
     *   readonly userHandle: ArrayBuffer;
     * }
     * 
     * interface AuthenticatorResponse {
     *   readonly clientDataJSON: ArrayBuffer;
     * }
     */

    assertion.response = {
      clientDataJSON:     binToStr(response.clientDataJSON),
      authenticatorData:  binToStr(response.authenticatorData),
      signature:          binToStr(response.signature),
      userHandle:         binToStr(response.userHandle)
    };

    const result = await _fetch('/FinishGetAssertion', {
      data: JSON.stringify(assertion),
      session: _options.session.id
    });

    if (result && result.success) {
      if (result.message) {
        showMessage(result.message);
      }
      if (result.handle) {
        let card = document.getElementById(result.handle);
        card.animate([{
          backgroundColor: 'rgb(255,64,129)'
        },{
          backgroundColor: 'white'
        }], {
          duration: 2000,
          easing: 'ease-out'
        });
      }
    } else {
      throw 'Unexpected response received.';
    }
  } catch (err) {
    app.active = false;

    showMessage(`An error occurred during Assertion operation [${err.toString()}]`);
  }
}

function convertCredentialList(list) {
  return list.map(item => {
    return {
      type: item.type,
      id: strToBin(item.id),
      transports: list.transports || undefined
    };
  });
}

function strToBin(str) {
  return Uint8Array.from(atob(str), c => c.charCodeAt(0));
}

function binToStr(bin) {
  return btoa(new Uint8Array(bin).reduce(
    (s, byte) => s + String.fromCharCode(byte), ''
  ));
}

function showMessage(text) {
  toast.text = text;
  toast.show();
}

async function _fetch(url, obj) {
  console.log(`[${url}] request:`, obj);

  let headers = new Headers({
    'Content-Type': 'application/x-www-form-urlencoded'
  });
  let body = new URLSearchParams();
  for (let key in obj) {
    body.append(key, obj[key]);
  }
  const response = await fetch(url, {
    method: 'POST',
    headers: headers,
    credentials: 'include',
    body: body
  });

  if (response.status === 200) {
    const result = await response.json();
    console.log(`[${url}] response:`, result);
    return result;
  } else {
    throw response.statusText;
  }
}

const app = document.querySelector('dom-bind');
const toast = document.querySelector('#toast');

document.addEventListener('DOMContentLoaded', () => {
  if (!navigator.credentials) {
    showMessage('Your browser is not compatible with Credential Management API.');
    return;
  }
  if (!window.PublicKeyCredential) {
    showMessage('Your browser is not compatible with Web Authentication.');
    return;
  }

  app.active = false;
  app.advanced = false;
  app.excludeCredentials = false;
  app.authenticatorAttachment = 'none';
  app.attestationConveyancePreference = 'NA';
  app.requireResidentKey = false;
  app.userVerification = 'none';

  document.getElementById('add').addEventListener('click', () => {
    addCredential();
  });
  document.getElementById('auth').addEventListener('click', () => {
    getAssertion();
  });
  document.getElementById('credentials').addEventListener('click', e => {
    if (e.target.nodeName == 'PAPER-BUTTON') {
      removeCredential(e.target.id);
    }
  });

  fetchCredentials();
});