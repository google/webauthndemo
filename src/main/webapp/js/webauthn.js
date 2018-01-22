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

    const options = await _fetch('/BeginMakeCredential', {
      advanced: app.advanced,
      advancedOptions: JSON.stringify(advancedOptions)
    });

    const makeCredentialOptions = {};

    makeCredentialOptions.rp = options.rp;
    makeCredentialOptions.user = options.user;
    makeCredentialOptions.user.id = new TextEncoder().encode(options.user.id);
    makeCredentialOptions.challenge = _strToBin(options.challenge);
    makeCredentialOptions.pubKeyCredParams = options.pubKeyCredParams;

    // Optional parameters
    if ('timeout' in options) {
      makeCredentialOptions.timeout = options.timeout;
    }
    if ('excludeCredentials' in options) {
      makeCredentialOptions.excludeCredentials = convertCredentialList(options.excludeCredentials);
    }
    if ('authenticatorSelection' in options) {
      makeCredentialOptions.authenticatorSelection = options.authenticatorSelection;
    }
    if ('attestation' in options) {
      makeCredentialOptions.attestation = options.attestation;
    }
    if ('extensions' in options) {
      makeCredentialOptions.extensions = options.extensions;
    }

    // Check to see if the browser supports credential creation
    if (typeof navigator.credentials.create !== "function") {
      throw "Browser does not support credential creation";
    }

    console.log('`navigator.credentials.create()` request:', makeCredentialOptions);

    const attestation = await navigator.credentials.create({
      publicKey: makeCredentialOptions
    }).catch(() => {
      throw 'Credential creation failed.';
    });

    console.log('`navigator.credentials.create()`result:', attestation);

    app.active = false;

    const publicKeyCredential = {};

    if ('id' in attestation) {
      publicKeyCredential.id = attestation.id;
    }
    if ('type' in attestation) {
      publicKeyCredential.type = attestation.type;
    }
    if ('rawId' in attestation) {
      publicKeyCredential.rawId = _binToStr(attestation.rawId);
    }
    if (!attestation.response) {
      throw "Make Credential response lacking 'response' attribute";
    }

    const response = {};

    response.clientDataJSON = _binToStr(attestation.response.clientDataJSON);
    response.attestationObject = _binToStr(attestation.response.attestationObject);
    publicKeyCredential.response = response;

    const result = await _fetch('/FinishMakeCredential', {
      data: JSON.stringify(publicKeyCredential),
      session: options.session.id
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
    const parameters = await _fetch('/BeginGetAssertion');
    const requestOptions = {};

    requestOptions.challenge = _strToBin(parameters.challenge);
    if ('timeout' in parameters) {
      requestOptions.timeout = parameters.timeout;
    }
    if ('rpId' in parameters) {
      requestOptions.rpId = parameters.rpId;
    }
    if ('allowCredentials' in parameters) {
      requestOptions.allowCredentials = convertCredentialList(parameters.allowCredentials);
    }

    if (typeof navigator.credentials.get !== "function") {
      throw "Browser does not support credential lookup";
    }

    console.log('`navigator.credentials.get()` request:', requestOptions);

    const assertion = await navigator.credentials.get({
      publicKey: requestOptions
    }).catch(() => {
      throw 'Authentication failed';
    });

    console.log('`navigator.credentials.get()` result:', assertion);

    app.active = false;

    const publicKeyCredential = {};

    if ('id' in assertion) {
      publicKeyCredential.id = assertion.id;
    }
    if ('type' in assertion) {
      publicKeyCredential.type = assertion.type;
    }
    if ('rawId' in assertion) {
      publicKeyCredential.rawId = _binToStr(assertion.rawId);
    }
    if (!assertion.response) {
      throw "Get assertion response lacking 'response' attribute";
    }

    const response = {};

    response.clientDataJSON = _binToStr(assertion.response.clientDataJSON);
    response.authenticatorData = _binToStr(assertion.response.authenticatorData);
    response.signature = _binToStr(assertion.response.signature);
    response.userHandle = _binToStr(assertion.response.userHandle);
    publicKeyCredential.response = response;

    const result = await _fetch('/FinishGetAssertion', {
      data: JSON.stringify(publicKeyCredential),
      session: parameters.session.id
    });

    if (result &&result.success) {
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
      id: _strToBin(item.id),
      transports: list.transports || undefined
    };
  });
}

function _strToBin(str) {
  return Uint8Array.from(atob(str), c => c.charCodeAt(0));
}

function _binToStr(bin) {
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
    e.stopPropagation();
    if (e.target.nodeName == 'PAPER-BUTTON') {
      removeCredential(e.target.id);
    }
  });

  fetchCredentials();
});