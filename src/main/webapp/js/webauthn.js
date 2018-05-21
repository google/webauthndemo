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

const $ = q => {
  return document.querySelector(q);
};

const show = q => {
  $(q).style.display = 'block';
};

const hide = q => {
  $(q).style.display = 'none';
};

const isChecked = q => {
  return $(q).checked;
};

const onClick = (q, func) => {
  $(q).addEventListener('click', func);
};

function addErrorMsg(msg) {
  $('#error-text').innerHTML = msg;
  show('#error');
};

function addSuccessMsg(msg) {
  $('#success-text').innerHTML = msg;
  show('#success');
};

function removeMsgs() {
  hide('#error');
  hide('#success');
};

function _fetch(url, obj) {
  let headers = new Headers({
    'Content-Type': 'application/x-www-form-urlencoded'
  });
  let body;
  if (typeof URLSearchParams === "function") {
    body = new URLSearchParams();
    for (let key in obj) {
      body.append(key, obj[key]);
    }
    // Set body to string value to handle an Edge case
    body = body.toString();
  } else {
    // Add parameters to body manually if browser doesn't support URLSearchParams
    body = "";
    for (let key in obj) {
      body += encodeURIComponent(key) + "=" + encodeURIComponent(obj[key]) + "&";
    }
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
};


function fetchCredentials() {
  _fetch('/RegisteredKeys').then(response => {
    let credentials = '';
    for (let i in response) {
      let { handle, publicKey, name, date, id } = response[i];
      let buttonId = `delete${i}`;
      credentials +=
        `<div class="mdl-cell mdl-cell--1-offset mdl-cell-4-col">
           <div class="mdl-card mdl-shadow--4dp" id="${handle}">
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

    for (let i in response) {
      let { handle, publicKey, name, date, id } = response[i];
      onClick(`#delete${i}`, removeCredential(id));
    }
  });
}

function removeCredential(id) {
  return () => {
    _fetch('/RemoveCredential', {
      credentialId : id
    }).then(() => {
      fetchCredentials();
    }).catch(err => {
      addErrorMsg(`An error occurred during removal [${err.toString()}]`);
    });
  }
}

function credentialListConversion(list) {
  return list.map(item => {
    const cred = {
      type: item.type,
      id: strToBin(item.id)
    };
    if (item.transports) {
      cred.transports = list.transports;
    }
    return cred;
  });
}

function addCredential() {
  removeMsgs();
  show('#active');

  let _options;
  const advancedOptions = {};
  if (isChecked('#switch-advanced')) {
    if (isChecked('#switch-rk')) {
      advancedOptions.requireResidentKey = isChecked('#switch-rk');
    }
    if (isChecked('#switch-rr')) {
      advancedOptions.excludeCredentials = isChecked('#switch-rr');
    }
    if ($('#userVerification').value != "none") {
      advancedOptions.userVerification = $('#userVerification').value;
    }
    if ($('#attachment').value != "none") {
      advancedOptions.authenticatorAttachment = $('#attachment').value;
    }
    if ($('#conveyance').value != "NA") {
      advancedOptions.attestationConveyancePreference = $('#conveyance').value;
    }
  }

  return _fetch('/BeginMakeCredential', {
    advanced: isChecked('#switch-advanced'),
    advancedOptions: JSON.stringify(advancedOptions)

  }).then(options => {
    const makeCredentialOptions = {};
    _options = options;

    makeCredentialOptions.rp = options.rp;
    makeCredentialOptions.user = options.user;
    makeCredentialOptions.user.id = strToBin(options.user.id);
    makeCredentialOptions.challenge = strToBin(options.challenge);
    makeCredentialOptions.pubKeyCredParams = options.pubKeyCredParams;

    // Optional parameters
    if ('timeout' in options) {
      makeCredentialOptions.timeout = options.timeout;
    }
    if ('excludeCredentials' in options) {
      makeCredentialOptions.excludeCredentials = credentialListConversion(options.excludeCredentials);
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

    console.log(makeCredentialOptions);

    return navigator.credentials.create({
      "publicKey": makeCredentialOptions
    });

  }).then(attestation => {
    hide('#active');

    const publicKeyCredential = {};

    if ('id' in attestation) {
      publicKeyCredential.id = attestation.id;
    }
    if ('type' in attestation) {
      publicKeyCredential.type = attestation.type;
    }
    if ('rawId' in attestation) {
      publicKeyCredential.rawId = binToStr(attestation.rawId);
    }
    if (!attestation.response) {
      addErrorMsg("Make Credential response lacking 'response' attribute");
    }

    const response = {};
    response.clientDataJSON = binToStr(attestation.response.clientDataJSON);
    response.attestationObject = binToStr(attestation.response.attestationObject);
    publicKeyCredential.response = response;

    return _fetch('/FinishMakeCredential', {
      data: JSON.stringify(publicKeyCredential),
      session: _options.session.id
    });

  }).then(parameters => {
    console.log(parameters);

    if (parameters && parameters.success) {
      addSuccessMsg(parameters.message);
      fetchCredentials();
    } else {
      throw 'Unexpected response received.';
    }

  }).catch(err => {
    hide('#active');
    console.log(err.toString());
    addErrorMsg(`An error occurred during Make Credential operation [${err.toString()}]`);
  });
}

function getAssertion() {
  removeMsgs();
  show('#active');

  let _parameters;
  _fetch('/BeginGetAssertion').then(parameters => {
    const requestOptions = {};
    _parameters = parameters;

    requestOptions.challenge = strToBin(parameters.challenge);
    if ('timeout' in parameters) {
      requestOptions.timeout = parameters.timeout;
    }
    if ('rpId' in parameters) {
      requestOptions.rpId = parameters.rpId;
    }
    if ('allowCredentials' in parameters) {
      requestOptions.allowCredentials = credentialListConversion(parameters.allowCredentials);
    }

    console.log(requestOptions);

    return navigator.credentials.get({
      "publicKey": requestOptions
    });

  }).then(assertion => {
    hide('#active');

    const publicKeyCredential = {};

    if ('id' in assertion) {
      publicKeyCredential.id = assertion.id;
    }
    if ('type' in assertion) {
      publicKeyCredential.type = assertion.type;
    }
    if ('rawId' in assertion) {
      publicKeyCredential.rawId = binToStr(assertion.rawId);
    }
    if (!assertion.response) {
      throw "Get assertion response lacking 'response' attribute";
    }

    const _response = assertion.response;

    publicKeyCredential.response = {
      clientDataJSON:     binToStr(_response.clientDataJSON),
      authenticatorData:  binToStr(_response.authenticatorData),
      signature:          binToStr(_response.signature),
      userHandle:         binToStr(_response.userHandle)
    };

    return _fetch('/FinishGetAssertion', {
      data: JSON.stringify(publicKeyCredential),
      session: _parameters.session.id
    });

  }).then(result => {
    console.log(result);

    if (result && result.success) {
      addSuccessMsg(result.message);
      if ('handle' in result) {
        let card = document.getElementById(result.handle);
        card.animate([{
          backgroundColor: '#009688'
        },{
          backgroundColor: 'white'
        }], {
          duration: 2000,
          easing: 'ease-out'
        });
      }
    }
  }).catch(err => {
    hide('#active');
    console.log(err.toString());
    addErrorMsg(`An error occurred during Assertion request [${err.toString()}]`);
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

document.addEventListener('DOMContentLoaded', () => {
  let hiddens = Array.from(document.querySelectorAll('.hidden'));
  for (let hidden of hiddens) {
    hidden.style.display = 'none';
    hidden.classList.remove('hidden');
  }
  if (navigator.credentials && navigator.credentials.create) {
    fetchCredentials();
  } else {
    addErrorMsg('Your browser doesn\'t support WebAuthn');
    fetchCredentials();
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
});
