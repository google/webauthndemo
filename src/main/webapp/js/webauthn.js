/**
 * 
 */

var encoder = new TextEncoder();

function fetchAllCredentials(/* callback */) {
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
             <div class="mdl-card__supporting-text"><em>' + publicKey + '</em></div>\
             <div class="mdl-card__subtitle-text">Key Handle</div>\
             <div class="mdl-card__supporting-text"><em>' + handle + '</em></div>\
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
    for (var i in rsp) {
      var id = "#delete" + i;
      $(id).click(function() {
        console.log(rsp[i].id);
        $.post('/RemoveCredential', {credentialId : rsp[i].id}, null, 'json')
        .done(function(rsp) {
          fetchCredentials();
        });
      });
    }
  });
}

function assignButtons() {
  $("#credential-button").click(function() {
    addCredential();
  });
  $("#authenticate-button").click(function() {
    getAssertion();
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

function addCredential() {
  $.post('/BeginMakeCredential', {}, null, 'json')
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
    
    var createParams = {};
    createParams.publicKey = makeCredentialOptions;
    
    console.log(makeCredentialOptions);
  });
}

function finishAssertion(publicKeyCredential) {
  $.post('/FinishGetAssertion', { data: JSON.stringify(publicKeyCredential) },
    null, 'json')
    .done(function(parameters) {
      
    });
}

function addSpinner() {
  $("#active").show();
}

function removeSpinner() {
  $("#active").html('');
}

function getAssertion() {
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
    
    navigator.credentials.get({"publicKey": requestOptions})
      .then(function (assertion) {
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
        response.clientDataJSON = assertion.response.clientDataJSON;
        response.authenticatorData = btoa(
          new Uint8Array(assertion.response.authenticatorData)
          .reduce((s, byte) => s + String.fromCharCode(byte), ''));
        response.signature = btoa(
          new Uint8Array(assertion.response.signature)
          .reduce((s, byte) => s + String.fromCharCode(byte), ''));
        publicKeyCredential.response = response;
        finishAssertion(publicKeyCredential);
      }
    }).catch(function (err) {
      
    });
  });
}