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

function fetchAllCredentials() {
  $.post('/RegisteredKeys', {}, null, 'json')
   .done(function(tokens) {
     credentials.innerHTML='';
     for (var i=0; i < tokens.length; i++) {
       credentials.innerHTML=credentials.innerHTML+'<div id="credential"' + i + '>' +
       tokens[i].id + ': ' + tokens[i].handle + '</div>';
     }
   });
}

function fetchCredentials() {
  $.post('/RegisteredKeys', {}, null, 'json')
  .done(function(rsp) {
    var credentials = '';
    for (var i in rsp) {
      var handle = rsp[i].handle;
      var publicKey = rsp[i].publicKey;
      var name = rsp[i].name;
      var date = rsp[i].date;
      var buttonId = 'delete' + i;
      credentials +=
        '<div class="mdl-cell mdl-cell--1-offset mdl-cell-4-col">\
           <div class="mdl-card mdl-shadow--4dp">\
             <div class="mdl-card__title mdl-card--border">' + name + '</div>\
             <div class="mdl-card__supporting-text">Enrolled ' + date +'</div>\
             <div class="mdl-card__subtitle-text">Public Key</div>\
             <div class="mdl-card__supporting-text">' + publicKey + '</div>\
             <div class="mdl-card__subtitle-text">Key Handle</div>\
             <div class="mdl-card__supporting-text">' + handle + '</div>\
             <div class="mdl-card__menu">\
               <button id="' + buttonId + '" \
                 class="mdl-button mdl-button--icon mdl-js-button mdl-js-ripple-effect">\
                 <i class="material-icons">delete_forever</i>\
               </button>\
             </div>\
           </div>\
         </div>\
        ';
    }
    $("#credentials").html(credentials);
    deleteCred = [];
    for(let i = 0; i < rsp.length; ++i){
      deleteCred[i] = function() {
        console.log(rsp[i].id);
        $.post('/RemoveCredential', {credentialId : rsp[i].id}, null, 'json')
        .done(function(rsp) {
          fetchCredentials();
        });
      }
      var id = "#delete" + i;
      $(id).click(function() {
        deleteCred[i]();
      });
    }
  });
}

function getFunction(f) {
    return function() { return val; };
}

function assignButtons() {
  $("#credential-button").click(function() {
    addCredential();
  });
  $("#authenticate-button").click(function() {
    getAssertion();
  });
  $("#switch-advanced").click(function() {
    if ($("#switch-advanced").is(":checked")) {
      $("#advanced").show();
    } else {
      $("#advanced").hide();
    }
  });
}

window.onload = function () {
  assignButtons();
  fetchCredentials();
};

$(document).ready(function () {
  $(".hidden").hide().removeClass("hidden");
});

function credentialListConversion(list) {
  var result = [];
  for (var i=0; i < list.length; i++) {
    var credential = {};
    credential.type = list[i].type;
    credential.id = Uint8Array.from(atob(list[i].id), c => c.charCodeAt(0));
    if ('transports' in list) {
      credential.transports = list.transports;
    }
    result.push(credential);
  }
  return result;
}

function finishAddCredential(publicKeyCredential, sessionId) {
  let dataStr = JSON.stringify(publicKeyCredential);
  $.post('/FinishMakeCredential', { data: dataStr, session: sessionId },
    null, 'json')
    .done(function(parameters) {
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
  addSpinner();
  var advancedOptions = {};
  if ($("#switch-advanced").is(":checked")) {
    if ($("#switch-rk").is(":checked")) {
      advancedOptions.rk = $("#switch-rk").is(":checked");
    }
    if ($("#switch-uv").is(":checked")) {
      advancedOptions.uv = $("#switch-uv").is(":checked");
    }
    if ($('#attachment').val() != "none") {
      advancedOptions.attachment = $('#attachment').val();
    }
  }
  $.post('/BeginMakeCredential',
		  { advanced: $("#switch-advanced").is(":checked"), advancedOptions: JSON.stringify(advancedOptions) },
		  null, 'json')
  .done(function(options) {
    var makeCredentialOptions = {};
    makeCredentialOptions.rp = options.rp;
    makeCredentialOptions.user = options.user;
    makeCredentialOptions.challenge = Uint8Array.from(atob(options.challenge), c => c.charCodeAt(0));

    makeCredentialOptions.parameters = options.parameters;
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

    var createParams = {};
    createParams.publicKey = makeCredentialOptions;

    console.log(makeCredentialOptions);

    if (typeof navigator.credentials.create !== "function") {
      addErrorMsg("Browser does not support credential creation");
      return;
    }

    navigator.credentials.create({"publicKey": makeCredentialOptions})
    .then(function (attestation) {
      removeSpinner();
      var publicKeyCredential = {};
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
      removeSpinner();
      console.log(err.toString());
      addErrorMsg("An error occurred during Make Credential operation ["
        + err.toString() + "]");
    });
  });
}

function finishAssertion(publicKeyCredential, sessionId) {
  $.post('/FinishGetAssertion', { data: JSON.stringify(publicKeyCredential), session: sessionId },
    null, 'json')
    .done(function(parameters) {
      console.log(parameters);
      if ('success' in parameters && 'message' in parameters) {
        addSuccessMsg(parameters.message);
      }
      // TODO Validate response and display success/error message
    });
}

function addSpinner() {
  $("#active").show();
}

function removeSpinner() {
  $("#active").hide();
}

function addErrorMsg(msg) {
  document.getElementById("error-text").innerHTML = msg;
  $("#error").show();
}

function removeErrorMsg() {
  $("#error").hide();
}

function addSuccessMsg(msg) {
  document.getElementById("success-text").innerHTML = msg;
  $("#success").show();
}

function removeSuccessMsg() {
  $("#success").hide();
}

function removeMsgs() {
  removeErrorMsg();
  removeSuccessMsg();
}

function getAssertion() {
  removeMsgs();
  addSpinner();
  $.post('/BeginGetAssertion', {}, null, 'json')
  .done(function(parameters) {
    var requestOptions = {};
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

    var credentialRequest = {};
    credentialRequest.publicKey = requestOptions;
    console.log(credentialRequest);

    if (typeof navigator.credentials.get !== "function") {
      addErrorMsg("Browser does not support credential lookup");
      return;
    }

    navigator.credentials.get({"publicKey": requestOptions})
      .then(function (assertion) {
        removeSpinner();
      var publicKeyCredential = {};
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
      removeSpinner();
      console.log(err.toString());
      addErrorMsg("An error occurred during Assertion request ["
        + err.toString() + "]");
    });
  });
}