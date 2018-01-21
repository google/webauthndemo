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

class WadApp {
  constructor() {
    this.app = document.querySelector('dom-bind');
    this.app.active = false;
    this.app.advanced = false;
    this.app.excludeCredentials = false;
    this.app.authenticatorAttachment = 'none';
    this.app.attestationConveyancePreference = 'NA';
    this.app.requireResidentKey = false;
    this.app.userVerification = 'none';

    this.toast = document.querySelector('#toast');

    document.getElementById('add').addEventListener('click', () => {
      this.addCredential();
    });
    document.getElementById('auth').addEventListener('click', () => {
      this.getAssertion();
    });
    document.getElementById('credentials').addEventListener('click', e => {
      e.stopPropagation();
      if (e.target.nodeName == 'PAPER-BUTTON') {
        this.delete(e.target.id);
      }
    });

    this.fetchCredentials();
  }

  _fetch(url, obj) {
    let headers = new Headers({
      'Content-Type': 'application/x-www-form-urlencoded'
    });
    let body = new URLSearchParams();
    for (let key in obj) {
      body.append(key, obj[key]);
    }
    return fetch(url, {
      method: 'POST',
      headers: headers,
      credentials: 'include',
      body: body
    }).then(response => {
      if (response.status === 200) {
        return response.json();
      } else {
        throw response.statusText;
      }
    });
  }

  fetchCredentials() {
console.log('[/RegisteredKeys] request');

    this._fetch('/RegisteredKeys').then(response => {
      this.app.credentials = response;
    });
  }

  async addCredential() {
    this.app.active = true;

    try {
      const advancedOptions = {};

      if (this.app.advanced) {
        advancedOptions.requireResidentKey = this.app.requireResidentKey;
        advancedOptions.excludeCredentials = this.app.excludeCredentials;
        advancedOptions.userVerification = this.app.userVerification;
        advancedOptions.authenticatorAttachment = this.app.authenticatorAttachment;
        advancedOptions.attestationConveyancePreference = this.app.attestationConveyancePreference;
      }

  console.log('[/BeginMakeCredential] request options:', advancedOptions);

      const options = await this._fetch('/BeginMakeCredential', {
        advanced: this.app.advanced,
        advancedOptions: JSON.stringify(advancedOptions)
      });

  console.log('[/BeginMakeCredential] response:', options);

      const makeCredentialOptions = {};

      makeCredentialOptions.rp = options.rp;
      makeCredentialOptions.user = options.user;
      makeCredentialOptions.user.id = new TextEncoder().encode(options.user.id);
      makeCredentialOptions.challenge = this._strToBin(options.challenge);
      makeCredentialOptions.pubKeyCredParams = options.pubKeyCredParams;

      // Optional parameters
      if ('timeout' in options) {
        makeCredentialOptions.timeout = options.timeout;
      }
      if ('excludeCredentials' in options) {
        makeCredentialOptions.excludeCredentials = this._credentialListConversion(options.excludeCredentials);
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

  console.log('`navigator.credentials.create()` options:', makeCredentialOptions);

      // Check to see if the browser supports credential creation
      if (typeof navigator.credentials.create !== "function") {
        throw "Browser does not support credential creation";
      }

      const attestation = await navigator.credentials.create({
        publicKey: makeCredentialOptions
      });

      this.app.active = false;

  console.log('`navigator.credentials.create()`result:', attestation);

      const publicKeyCredential = {};

      if ('id' in attestation) {
        publicKeyCredential.id = attestation.id;
      }
      if ('type' in attestation) {
        publicKeyCredential.type = attestation.type;
      }
      if ('rawId' in attestation) {
        publicKeyCredential.rawId = this._binToStr(attestation.rawId);
      }
      if (!attestation.response) {
        throw "Make Credential response lacking 'response' attribute";
      }

      const response = {};

      response.clientDataJSON = this._binToStr(attestation.response.clientDataJSON);
      response.attestationObject = this._binToStr(attestation.response.attestationObject);
      publicKeyCredential.response = response;

  console.log('[/FinishMakeCredential] request options:', publicKeyCredential);

      const result = await this._fetch('/FinishMakeCredential', {
        data: JSON.stringify(publicKeyCredential),
        session: options.session.id
      });

  console.log('[/FinishMakeCredential] response:', result);

      if (result && result.success) {
        this._showMessage(result.message);
        this.fetchCredentials();
      } else {
        throw 'Unexpected response received.';
      }
    } catch (err) {
      this.app.active = false;

      this._showMessage(`An error occurred during Make Credential operation [${err.toString()}]`);
    }
  }

  async getAssertion() {
    this.app.active = true;

    try {
console.log('[/BeingGetAssertion] request');

      const parameters = await this._fetch('/BeginGetAssertion');
      const requestOptions = {};

      requestOptions.challenge = this._strToBin(parameters.challenge);
      if ('timeout' in parameters) {
        requestOptions.timeout = parameters.timeout;
      }
      if ('rpId' in parameters) {
        requestOptions.rpId = parameters.rpId;
      }
      if ('allowCredentials' in parameters) {
        requestOptions.allowCredentials = this._credentialListConversion(parameters.allowCredentials);
      }

console.log('`navigator.credentials.get()` result:', requestOptions);

      if (typeof navigator.credentials.get !== "function") {
        throw "Browser does not support credential lookup";
      }

      const assertion = await navigator.credentials.get({
        publicKey: requestOptions
      });

      this.app.active = false;

console.log('`navigator.credentials.get()` result:', assertion);

      const publicKeyCredential = {};

      if ('id' in assertion) {
        publicKeyCredential.id = assertion.id;
      }
      if ('type' in assertion) {
        publicKeyCredential.type = assertion.type;
      }
      if ('rawId' in assertion) {
        publicKeyCredential.rawId = this._binToStr(assertion.rawId);
      }
      if (!assertion.response) {
        throw "Get assertion response lacking 'response' attribute";
      }

      const response = {};

      response.clientDataJSON = this._binToStr(assertion.response.clientDataJSON);
      response.authenticatorData = this._binToStr(assertion.response.authenticatorData);
      response.signature = this._binToStr(assertion.response.signature);
      response.userHandle = this._binToStr(assertion.response.userHandle);
      publicKeyCredential.response = response;

console.log('[/FinishGetAssertion] request options:', publicKeyCredential);

      const result = await this._fetch('/FinishGetAssertion', {
        data: JSON.stringify(publicKeyCredential),
        session: parameters.session.id
      });

console.log('[/FinishGetAssertion] response:', result);

      if (result && result.success) {
        this._showMessage(result.message);
        if ('handle' in result) {
          this._blinkCard(result.handle);
        }
      } else {
        throw 'Unexpected response received.';
      }
    } catch (err) {
      this.app.active = false;

      this._showMessage(`An error occurred during Assertion request [${err.toString()}]`);
    };
  }

  _blinkCard(id) {
    let card = document.getElementById(id);
    card.animate([{
      backgroundColor: 'rgb(255,64,129)'
    },{
      backgroundColor: 'white'
    }], {
      duration: 2000,
      easing: 'ease-out'
    });
  }

  _credentialListConversion(list) {
    return list.map(item => {
      return {
        type: item.type,
        id: this._strToBin(item.id),
        transports: list.transports || undefined
      };
    });
  }

  _strToBin(str) {
    return Uint8Array.from(atob(str), c => c.charCodeAt(0));
  }

  _binToStr(bin) {
    return btoa(new Uint8Array(bin).reduce(
      (s, byte) => s + String.fromCharCode(byte), ''
    ));
  }

  _showMessage(text) {
    this.toast.text = text;
    this.toast.show();
  }

  delete(id) {
console.log('[/RemoveCredential] id:', id);

    this._fetch('/RemoveCredential', {
      credentialId : id
    }).then(() => {
      this.fetchCredentials();
    });
  }
}

const app = new WadApp();