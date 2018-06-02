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
    // Add parameters to body manually if browser doesn't support
    // URLSearchParams
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

function getAssertion() {
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
      let tmpName = $('#username-text').value;
      $('#instructions').textContent = 'Thanks, ' + tmpName + '! Login was a success.';
      hide('#auth-spinner');
    }
  }).catch(err => {
    hide('#auth-spinner');
    console.log(err.toString());
    $('#instructions').textContent = `Sorry, an error occurred. [${err.toString()}]`;
    $('#next-button').disabled = false;
    $('#next-button').textContent = 'Retry';
    $('#instructions').style.color = 'red';
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

function transitionToPassword() {
  $('#instruction-text').style.opacity = 0;
  window.setTimeout(completePasswordTransition, 800);
  $('#next-button').removeEventListener('click', transitionToPassword);
  $('#next-button').disabled = true;
}

function completePasswordTransition() {
  let tmpName = $('#username-text').value;
  hide('#top-box');
  $('#instructions').textContent = 'Hello, ' + tmpName + ', please enter a password. For your privacy, this data is never stored nor sent to the server.';
  $('#instruction-text').style.opacity = 1;
  show('#bottom-box');
  onClick('#next-button', transitionToAuthentication);
  window.setTimeout(enableButton, 800);
}

function enableButton() {
  $('#password-text').addEventListener("input", passwordValidation);
  passwordValidation();
}

function passwordValidation() {
  if ($('#password-text').value.length < 1) {
    $('#next-button').disabled = true;
  } else {
    $('#next-button').disabled = false;
  }
}

function transitionToAuthentication() {
  $('#next-button').disabled = true;
  $('#instruction-text').style.opacity = 0;
  window.setTimeout(waitingForTouch, 800);
  getAssertion();
}

function waitingForTouch() {
  $('#instructions').style.color = 'gray';
  hide('#bottom-box');
  show('#auth-spinner');
  $('#instructions').textContent = 'Please use your security key or authenticator to finish authentication.';
  $('#instruction-text').style.opacity = 1;
}

function usernameValidation() {
  if ($('#username-text').value.length < 1) {
    $('#next-button').disabled = true;
  } else {
    $('#next-button').disabled = false;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  let hiddens = Array.from(document.querySelectorAll('.hidden'));
  for (let hidden of hiddens) {
    hidden.style.display = 'none';
    hidden.classList.remove('hidden');
  }
  if (navigator.credentials && navigator.credentials.create) {
  } else {
    addErrorMsg('Your browser doesn\'t support WebAuthn');
    fetchCredentials();
  }
});

window.addEventListener('load', () => {
  $('#outer-card').style.opacity = 1;
  show('#top-box');
  onClick('#next-button', transitionToPassword);
  $('#next-button').disabled = true;
  $('#username-text').addEventListener("input", usernameValidation);
  usernameValidation();
});
