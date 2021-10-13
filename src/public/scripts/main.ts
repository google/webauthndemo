import { html, render, $, showSnackbar, _fetch } from './bundle';

const register = $('#credential-button');

const collectOptions = (): any => {
  const excludeCredentials = $('#switch-rr').checked;
  const authenticatorAttachment = $('#attachment').value;
  const attestation = $('#conveyance').value;
  const residentKey = $('#residentKey').value;
  const userVerification = $('userVerification').value;
  const UVM = $('#switch-uvm').checked;
  const customTimeout = $('#customTimeout').value;
  const abortTimeout = $('#abortTimeout').value;

  return {
    attestation,
    authenticatorSelection: {
      authenticatorAttachment,
      userVerification,
      residentKey
    },
    excludeCredentials,
    customTimeout,
    abortTimeout,
  };
}

const encodeCredential = (options: any): any => {
};

const decodeCredential = (options: any): any => {
};

const listCredentials = (options: any): any => {
  const credentials = _fetch('/webauthn/getCredentials');
  render(credentials.map(credential => html`
  <div class="mdl-cell mdl-cell--1-offset-desktop mdl-cell-4-col">
    <div class="mdl-card mdl-shadow--4dp" id="${handle}">
      <div class="mdl-card__title mdl-card--border">
        <label class="mdl-switch mdl-js-switch mdl-js-ripple-effect" for="switch-${trimmedHandle}">
          <input type="checkbox" id="switch-${trimmedHandle}" class="mdl-switch__input" checked>
          <span class="mdl-switch__label">${name}</span>
        </label>
      </div>
      <dt>Enrolled ${date}</dt>
      <dt>Public Key</dt>
      <dd>${publicKey}</dd>
      <dt>Key Handle</dt>
      <dd>${handle}</dd>
      <dt>User Verification Method</dt>
      <dd>${userVerificationMethod}</dd>
      <dt>Transports</dt>
      <dd>
        <mwc-formfield label="${transport}">
          <mwc-checkbox id="${transport}${trimmedHandle}" value="${transport}${trimmedHandle}" checked></mwc-checkbox>
        </mwc-formfield>
      </dd>
      <div class="mdl-card__menu">
        <mwc-button id="${buttonId}"
          icon="delete_forever"
          class="mdl-button mdl-button--icon mdl-js-button mdl-js-ripple-effect"
          title="Removes this credential registration from the server">
        </mwc-button>
      </div>
    </div>
  </div>
  `), $('#credentials'));
};

const registerCredential = async (opts: any): any => {
  const options = _fetch('/webauthn/registerRequest', opts);
  const decodedOptions = decodeOptions(options);
  const credential = await navigator.credentials.create({
    publicKey: decodedOptions
  });
  const encodedCredential = encodeCredential(credential);
  return _fetch('/webauthn/registerResponse', encodedCredential);
};

const authenticate = (options: any): any => {
};

register.addEventListener('click', async e => {
  const opts = collectOptions();
  try {
    await registerCredential(opts);
    listCredentials();
  } catch (e) {
    showSnackbar(e);
  }
});
