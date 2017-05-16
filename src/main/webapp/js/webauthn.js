/**
 * 
 */

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

function getAssertion() {
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
  });
}