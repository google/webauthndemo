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

const $ = query => {
  return document.querySelector(query);
}

const show = query => {
  $(query).style.display = 'block';
};

const hide = query => {
  $(query).style.display = 'none';
}

const isChecked = query => {
  return $(query).checked;
}

const onClick = (query, func) => {
  $(query).addEventListener('click', func);
}

const post = (url, obj) => {
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

function fetchAllCredentials() {
  post('/RegisteredKeys').then(tokens => {
    credentials.innerHTML='';
    for (let i in tokens) {
      let token = tokens[i];
      credentials.innerHTML += `<div id="credential${i}">${token.id}: ${token.handle}</div>`;
    }
  });
}

function fetchCredentials() {
  post('/RegisteredKeys').then(rsp => {
    var credentials = '';
    for (let i = 0; i < rsp.length; i++) {
      let { handle, publicKey, name, date } = rsp[i];
      let buttonId = `delete${i}`;
      credentials +=
        `<div class="mdl-cell mdl-cell--1-offset mdl-cell-4-col">
           <div class="mdl-card mdl-shadow--4dp">
             <div class="mdl-card__title mdl-card--border">${name}</div>
             <div class="mdl-card__supporting-text">Enrolled ${date}</div>
             <div class="mdl-card__subtitle-text">Public Key</div>
             <div class="mdl-card__supporting-text">${publicKey}</div>
             <div class="mdl-card__subtitle-text">Key Handle</div>
             <div class="mdl-card__supporting-text">${handle}</div>
             <div class="mdl-card__menu">
               <button id="${buttonId}"
                 class="mdl-button mdl-button--icon mdl-js-button mdl-js-ripple-effect">
                 <i class="material-icons">delete_forever</i>
               </button>
             </div>
           </div>
         </div>
        `;
    }
    $('#credentials').innerHTML = credentials;
    for (let r of rsp){
      onClick(`delete${i}`, i => {
        console.log(r.id);
        post('/RemoveCredential', {
          credentialId : r.id
        }).then(response => {
          fetchCredentials();
        });
      });
    }
  });
}

document.addEventListener('DOMContentLoaded', () => {
  let hiddens = document.querySelectorAll('.hidden');
  for (let hidden of hiddens) {
    hidden.style.display = 'none';
    hidden.classList.remove('hidden');
  }
});

window.addEventListener('load', () => {
  onClick('#credential-button', addCredential);
  onClick('#authenticate-button', getAssertion);
  onClick('#switch-advanced', () => {
    if (isChecked('#switch-advanced')) {
      show('#advanced');
    } else {
      hide('#advanced');
    }
  });
  fetchCredentials();
});

function credentialListConversion(list) {
  return list.map(item => {
    return {
      type: item.type,
      id: Uint8Array.from(atob(item.id), c => c.charCodeAt(0)),
      transports: list.transports || undefined
    }
  });
}

function finishAddCredential(publicKeyCredential, sessionId) {
  post('/FinishMakeCredential', {
      data: JSON.stringify(publicKeyCredential),
      session: sessionId }
  ).then(parameters => {
    console.log(parameters);
    if ('success' in parameters && 'message' in parameters) {
      addSuccessMsg(parameters.message);
      fetchCredentials();
    }
    // TODO Validate response and display success/error message
  });
}

function addCredential() {
  removeMsgs();
  show('#active');
  var advancedOptions = {};
  if (isChecked('#switch-advanced')) {
    if (isChecked('#switch-rk')) {
      advancedOptions.rk = isChecked('#switch-rk');
    }
    if (isChecked('#switch-uv')) {
      advancedOptions.uv = isChecked('#switch-uv');
    }
    if ($('#attachment').value != "none") {
      advancedOptions.attachment = $('#attachment').value;
    }
  }

  let options = {};

  post('/BeginMakeCredential', {
      advanced: isChecked('#switch-advanced'),
      advancedOptions: JSON.stringify(advancedOptions) }
  ).then(_options => {
    options = _options;
    let makeCredentialOptions = {};
    makeCredentialOptions.rp = options.rp;
    makeCredentialOptions.user = options.user;
    makeCredentialOptions.challenge = Uint8Array.from(atob(options.challenge), c => c.charCodeAt(0));

    makeCredentialOptions.pubKeyCredParams = options.parameters.map(entry => {
      let alg;
      switch (entry.algorithm) {
        case 'ES256':
          alg = -7;
          break;
        case 'ES384':
          alg = -35;
          break;
        case 'ES512':
          alg = -36;
          break;
        default:
          alg = 0;
      }
      return {
        type: entry.type,
        alg: alg
      }
    });
    // makeCredentialOptions.parameters = options.parameters;
    if ('timeout' in options) {
      makeCredentialOptions.timeout = options.timeout;
    }
    if ('excludeList' in options) {
      makeCredentialOptions.excludeList = credentialListConversion(parameters.excludeList);
    }
    if ('extensions' in options) {
      makeCredentialOptions.extensions = options.extensions;
    }
    if ('authenticatorSelection' in options) {
      makeCredentialOptions.authenticatorSelection = options.authenticatorSelection;
    }

    let createParams = {};
    createParams.publicKey = makeCredentialOptions;

    console.log(makeCredentialOptions);

    if (typeof navigator.credentials.create !== "function") {
      throw "Browser does not support credential creation";
    }

    return navigator.credentials.create({
      "publicKey": makeCredentialOptions
    });
  }).then(attestation => {
    hide('#active');
    let publicKeyCredential = {};
    if ('id' in attestation) {
      publicKeyCredential.id = attestation.id;
    }
    if ('type' in attestation) {
      publicKeyCredential.type = attestation.type;
    }
    if ('rawId' in attestation) {
      publicKeyCredential.rawId = btoa(
        new Uint8Array(attestation.rawId).reduce((s, byte) =>
        s + String.fromCharCode(byte), ''));
      publicKeyCredential.rawId = attestation.rawId;
    }
    if ('response' in attestation) {
      var response = {};
      response.clientDataJSON = btoa(
        new Uint8Array(attestation.response.clientDataJSON)
        .reduce((s, byte) => s + String.fromCharCode(byte), ''));
      response.attestationObject = btoa(
        new Uint8Array(attestation.response.attestationObject)
        .reduce((s, byte) => s + String.fromCharCode(byte), ''));
      publicKeyCredential.response = response;
      finishAddCredential(publicKeyCredential, options.session.id);
    }
  }).catch(function (err) {
    hide('#active');
    console.log(err.toString());
    addErrorMsg(`An error occurred during Make Credential operation [${err.toString()}]`);
  });
}

function finishAssertion(publicKeyCredential, sessionId) {
  post('/FinishGetAssertion', {
      data: JSON.stringify(publicKeyCredential),
      session: sessionId }
  ).then(parameters => {
    console.log(parameters);
    if ('success' in parameters && 'message' in parameters) {
      addSuccessMsg(parameters.message);
    }
    // TODO Validate response and display success/error message
  });
}

function addErrorMsg(msg) {
  $('#error-text').innerHTML = msg;
  show('#error');
}

function addSuccessMsg(msg) {
  $('#success-text').innerHTML = msg;
  show('#success');
}

function removeMsgs() {
  hide('#error');
  hide('#success');
}

function getAssertion() {
  removeMsgs();
  show('#active');
  post('/BeginGetAssertion').then(parameters => {
    let requestOptions = {};
    requestOptions.challenge = Uint8Array.from(atob(parameters.challenge), c => c.charCodeAt(0));
    if ('timeout' in parameters) {
      requestOptions.timeout = parameters.timeout;
    }
    if ('rpId' in parameters) {
      requestOptions.rpId = parameters.rpId;
    }
    if ('allowList' in parameters) {
      requestOptions.allowList = credentialListConversion(parameters.allowList);
    }

    let credentialRequest = {};
    credentialRequest.publicKey = requestOptions;
    console.log(credentialRequest);

    if (typeof navigator.credentials.get !== "function") {
      throw "Browser does not support credential lookup";
    }

    return navigator.credentials.get({
      "publicKey": requestOptions
    });
  }).then(assertion => {
    hide('#active');
    let publicKeyCredential = {};
    if ('id' in assertion) {
      publicKeyCredential.id = assertion.id;
    }
    if ('type' in assertion) {
      publicKeyCredential.type = assertion.type;
    }
    if ('rawId' in assertion) {
      publicKeyCredential.rawId = btoa(
        new Uint8Array(assertion.rawId).reduce((s, byte) =>
        s + String.fromCharCode(byte), ''));
      publicKeyCredential.rawId = assertion.rawId;
    }
    if ('response' in assertion) {
      var response = {};
      response.clientDataJSON = btoa(
        new Uint8Array(assertion.response.clientDataJSON)
        .reduce((s, byte) => s + String.fromCharCode(byte), ''));
      response.authenticatorData = btoa(
        new Uint8Array(assertion.response.authenticatorData)
        .reduce((s, byte) => s + String.fromCharCode(byte), ''));
      response.signature = btoa(
        new Uint8Array(assertion.response.signature)
        .reduce((s, byte) => s + String.fromCharCode(byte), ''));
      publicKeyCredential.response = response;
      finishAssertion(publicKeyCredential, parameters.session.id);
    }
  }).catch(function (err) {
    hide('#active');
    console.log(err.toString());
    addErrorMsg(`An error occurred during Assertion request [${err.toString()}]`);
  });
}