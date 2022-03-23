import { html, render, $, showSnackbar, loading, _fetch } from './util';
import { WebAuthnRegistrationObject, WebAuthnAuthenticationObject, UserInfo } from './common';
import { base64url } from './base64url';
import { MDCRipple } from '@material/ripple';
import { initializeApp } from 'firebase/app';
import { Checkbox } from '@material/mwc-checkbox';
import * as firebaseui from 'firebaseui';
import {
  getAuth,
  connectAuthEmulator,
  GoogleAuthProvider,
  onAuthStateChanged,
  User
} from 'firebase/auth';
import {
  RegistrationCredential,
  RegistrationCredentialJSON,
  AuthenticationCredential,
  AuthenticationCredentialJSON,
  PublicKeyCredentialCreationOptions,
  PublicKeyCredentialCreationOptionsJSON,
  PublicKeyCredentialRequestOptions,
  PublicKeyCredentialRequestOptionsJSON
} from '@simplewebauthn/typescript-types';

initializeApp({
  apiKey: "AIzaSyBC_U6UbKJE0evrgaITJSk6T_sZmMaZO-4",
  authDomain: "try-webauthn.firebaseapp.com",
  projectId: "try-webauthn",
  storageBucket: "try-webauthn.appspot.com",
  messagingSenderId: "557912693280",
  appId: "1:557912693280:web:c47da88d666eaf0f40fa45",
  measurementId: "G-NWVKPRNL5Q"
});

const auth = getAuth();
if (location.hostname === 'localhost') {
  connectAuthEmulator(auth, 'http://localhost:9099');
}
const ui = new firebaseui.auth.AuthUI(auth);
const icon = $('#user-icon');

/**
 *  Verify ID Token received via Firebase Auth
 * @param authResult 
 * @returns always return `false`
 */
const verifyIdToken = async (user: User): Promise<UserInfo> => {
  const id_token = await user.getIdToken();
  return await _fetch('/auth/verify', { id_token });
}

/**
 * Display Firebase Auth UI
 */
const displaySignin = () => {
  loading.start();
  ui.start('#firebaseui-auth-container', {
    signInOptions: [ GoogleAuthProvider.PROVIDER_ID ],
    signInFlow: 'popup',
    callbacks: { signInSuccessWithAuthResult: () => false, }
  });
  $('#dialog').show();
};

/**
 * Sign out from Firebase Auth
 */
const signout = async () => {
  if (!confirm('Do you want to sign out?')) {
    return;
  }
  await auth.signOut();
  await _fetch('/auth/signout');
  icon.innerHTML = '';
  icon.setAttribute('icon', 'account_circle');
  $('#drawer').open = false;
  $('#credentials').innerHTML = '';
  showSnackbar('You are signed out.');
  displaySignin();
};

/**
 * Invoked when Firebase Auth status is changed.
 */
onAuthStateChanged(auth, async token => {
  if (!window.PublicKeyCredential) {
    render(html`
      <p>Your browser does not support WebAuthn.</p>
    `, $('#firebaseui-auth-container'));
    $('#dialog').show();
    return false;
  }

  let user: UserInfo;

  if (token) {
    try {
      user = await verifyIdToken(token);
    } catch (error) {
      console.error(error);
      showSnackbar('Sign-in failed.');
      return false;
    };
  } else {
    try {
      user = await _fetch('/auth/userInfo');
    } catch {
      // Signed out
      displaySignin();
      return false;
    }
  }
  $('#dialog').close();
  icon.removeAttribute('icon');
  render(html`<img src="${user.picture}">`, icon);
  showSnackbar('You are signed in!');
  loading.stop();
  listCredentials();
  return true;
});

/**
 *  Collect advanced options and return a JSON object.
 * @returns WebAuthnRegistrationObject
 */
const collectOptions = (
  mode: 'registration' | 'authentication' = 'registration'
): WebAuthnRegistrationObject|WebAuthnAuthenticationObject => {
  // const specifyCredentials = $('#switch-rr').checked;
  const authenticatorAttachment = $('#attachment').value;
  const attestation = $('#conveyance').value;
  const residentKey = $('#resident-key').value;
  const userVerification = $('#user-verification').value;
  const uvm = $('#switch-uvm').checked;
  const credProps = $('#switch-cred-props').checked;
  const customTimeout = parseInt($('#custom-timeout').value);
  // const abortTimeout = parseInt($('#abort-timeout').value);

  const cards = document.querySelectorAll<HTMLDivElement>('#credentials .mdc-card__primary-action');
  const credentialsList: string[] = [];

  // This is registration
  if (mode === 'registration') {
    // Force exclude all credentials.
    if ($('#exclude-all-credentials').checked) {
      cards.forEach(card => {
        const checkbox = card.querySelector<Checkbox>('mwc-checkbox');
        if (checkbox?.checked) credentialsList.push(card.id)
      });

    // // Otherewise exclude ones that are checked.
    // } else {
    //   cards.forEach(card => {
    //     const checkbox = card.querySelector<Checkbox>('mwc-checkbox');
    //     if (checkbox?.checked) credentialsList.push(card.id);
    //   });
    }
    const credentialsToExclude: string[] = credentialsList;
    return {
      attestation,
      authenticatorSelection: {
        authenticatorAttachment,
        userVerification,
        residentKey
      },
      extensions: { uvm, credProps },
      credentialsToExclude,
      customTimeout,
      // abortTimeout,
    } as WebAuthnRegistrationObject;
  
  // This is authentication
  } else {
    // "Force empty `allowCredentials`"" is not checked
    if (!$('#empty-allow-credentials').checked) {
       cards.forEach(card => {
         const checkbox = card.querySelector<Checkbox>('mwc-checkbox');
         if (checkbox?.checked) credentialsList.push(card.id);
       });
      //cards.forEach(card => credentialsList.push(card.id));
    }
    // If "Force empty `allowCredentials`" is checked, pass an empty array.
    const credentialsToAllow: string[] = credentialsList;

    return {
      extensions: { uvm, credProps },
      credentialsToAllow,
      customTimeout,
      // abortTimeout,
    } as WebAuthnAuthenticationObject
  }
}

/**
 *  Ripple on the specified credential card to indicate it's found.
 * @param credID 
 */
const rippleCard = (credID: string) => {
  const ripple = new MDCRipple($(`#ID-${credID}`));
  ripple.activate();
  ripple.deactivate();
}

/**
 *  Serialize the User Verification Method Extension result
 * @param uvms 
 * @returns 
 */
function serializeUvm(uvms: any) {
  var uvmJson = [];
  for (let uvm of uvms) {
    const uvmEntry: any = {};
    uvmEntry.userVerificationMethod = uvm[0];
    uvmEntry.keyProtectionType = uvm[1];
    uvmEntry.atchuvmJsonerProtectionType = uvm[2];
    uvmJson.push(uvmEntry);
  }
  return uvmJson;
}

/**
 * Fetch and render the list of credentials.
 */
const listCredentials = async (): Promise<void> => {
  loading.start();
  const transportIconMap = {
    internal: "devices",
    usb: "usb",
    nfc: "nfc",
    ble: "bluetooth",
    cable: "cable",
  } as { [key: string]: string };
  try {
    const credentials = <any[]>await _fetch('/webauthn/getCredentials');
    loading.stop();
    render(credentials.map(cred => {
      cred.id = cred.credentialID.substr(0, 16);
      const extensions = cred.clientExtensionResults;
      const transports = cred.transports as string[];
      const authenticatorType = `${cred.user_verifying?'User Verifying ':''}`+
        `${cred.authenticatorAttachment==='platform'?'Platform ':
           cred.authenticatorAttachment==='cross-platform'?'Roaming ':''}Authenticator`;
      return html`
      <div class="mdc-card">
        <div class="mdc-card__primary-action" id="ID-${cred.credentialID}">
          <div class="card-title mdc-card__action-buttons">
            <div class="cred-title mdc-card__action-button">
              <mwc-formfield label="${cred.id}">
                <mwc-checkbox title="Check to exclude or allow this credential" checked></mwc-checkbox>
              </mwc-formfield>
            </div>
            <div class="mdc-card__action-icons">
              <mwc-icon-button @click="${removeCredential(cred.credentialID)}" icon="delete_forever" title="Removes this credential registration from the server"></mwc-icon>
            </div>
          </div>
          <div class="card-body">
            <dt>Authenticator Type</dt>
            <dd>${authenticatorType}</dd>
            <dt>Environment</dt>
            <dd>${cred.browser} / ${cred.os} / ${cred.platform}</dd>
            <dt>Transports</dt>
            <dd class="transports">
              ${!transports.length ? html`
              <span>N/A</span>
              ` : transports.map(transport => html`
              <mwc-icon-button icon="${transportIconMap[transport]}"></mwc-icon-button>
              `)}
            </dd>
            <dt>Enrolled</dt>
            <dd>${(new Date(cred.registered)).toLocaleString()}</dd>
            ${extensions?.uvm ? html`
            <dt>User Verification Method Extension</dt>
            <dd>${extensions.uvm}</dd>`:''}
            ${extensions?.credProps ? html`
            <dt>Credential Properties Extension</dt>
            <dd>${extensions.credProps.rk ? 'true' : 'false'}</dd>`:''}
            <dt>Public Key</dt>
            <dd>${cred.credentialPublicKey}</dd>
            <dt>Key Handle</dt>
            <dd>${cred.credentialID}</dd>
            <div class="mdc-card__ripple"></div>
          </div>
        </div>
      </div>
    `}), $('#credentials'));
    loading.stop();
    if (!$('#exclude-all-credentials').checked) {
      const cards = document.querySelectorAll<HTMLDivElement>('#credentials .mdc-card__primary-action');
      cards.forEach(card => {
        const checkbox = card.querySelector<Checkbox>('mwc-checkbox');
        if (checkbox) checkbox.checked = false;
      });
    }
  } catch (e) {
    console.error(e);
    showSnackbar('Loading credentials failed.');
    loading.stop();
  }
};

/**
 *  Register a new credential.
 * @param opts 
 */
const registerCredential = async (opts: WebAuthnRegistrationObject): Promise<any> => {
  // Fetch credential creation options from the server.
  const options: PublicKeyCredentialCreationOptionsJSON =
    await _fetch('/webauthn/registerRequest', opts);

  // Decode encoded parameters.
  const user = {
    ...options.user,
    id: base64url.decode(options.user.id)
  } as PublicKeyCredentialUserEntity;
  const challenge = base64url.decode(options.challenge);
  const excludeCredentials: PublicKeyCredentialDescriptor[] = [];
  if (options.excludeCredentials) {
    options.excludeCredentials.map(cred => {
      excludeCredentials.push({
        ...cred,
        id: base64url.decode(cred.id),
      });
    });
  }
  const decodedOptions = {
    ...options,
    user,
    challenge,
    excludeCredentials,
  } as PublicKeyCredentialCreationOptions;

  console.log('[CreationOptions]', decodedOptions);

  // Create a new attestation.
  const credential = await navigator.credentials.create({
    publicKey: decodedOptions
  }) as RegistrationCredential;

  // Encode the attestation.
  const rawId = base64url.encode(credential.rawId);
  const clientDataJSON = base64url.encode(credential.response.clientDataJSON);
  const attestationObject = base64url.encode(credential.response.attestationObject);
  const clientExtensionResults: any = {};

  // if `getClientExtensionResults()` is supported, serialize the result.
  if (credential.getClientExtensionResults) {
    const extensions = credential.getClientExtensionResults();
    if ('uvm' in extensions) {
      clientExtensionResults.uvm = serializeUvm(extensions.uvm);
    }
    if ('credProps' in extensions) {
      clientExtensionResults.credProps = extensions.credProps;
    }
  }
  let transports: any[] = [];

  // if `getTransports()` is supported, serialize the result.
  if (credential.response.getTransports) {
    transports = credential.response.getTransports();
  }

  const encodedCredential = {
    id: credential.id,
    rawId,
    response: {
      clientDataJSON,
      attestationObject
    },
    type: credential.type,
    transports,
    clientExtensionResults, 
  } as RegistrationCredentialJSON;

  console.log('[AttestationCredential]', encodedCredential);

  // Verify and store the attestation.
  await _fetch('/webauthn/registerResponse', encodedCredential);
};

/**
 *  Authenticate the user with a credential.
 * @param opts 
 * @returns 
 */
const authenticate = async (opts: WebAuthnAuthenticationObject): Promise<any> => {
  // Fetch the credential request options.
  const options: PublicKeyCredentialRequestOptionsJSON =
    await _fetch('/webauthn/authRequest', opts);

  // Decode encoded parameters.
  const challenge = base64url.decode(options.challenge);
  const allowCredentials: PublicKeyCredentialDescriptor[] = [];
  if (options.allowCredentials) {
    options.allowCredentials.map(cred => {
      allowCredentials.push({
        ...cred,
        id: base64url.decode(cred.id),
      });
    });
  }
  const decodedOptions = {
    ...options,
    allowCredentials,
    challenge,
  } as PublicKeyCredentialRequestOptions;

  console.log('[RequestOptions]', decodedOptions);

  // Authenticate the user.
  const credential = await navigator.credentials.get({
    publicKey: decodedOptions
  }) as AuthenticationCredential;

  // Encode the credential.
  const rawId = base64url.encode(credential.rawId);
  const authenticatorData = base64url.encode(credential.response.authenticatorData);
  const clientDataJSON = base64url.encode(credential.response.clientDataJSON);
  const signature = base64url.encode(credential.response.signature);
  const userHandle = credential.response.userHandle ?
    base64url.encode(credential.response.userHandle) : undefined;

  const encodedCredential = {
    id: credential.id,
    rawId,
    response: {
      authenticatorData,
      clientDataJSON,
      signature,
      userHandle,
    },
    type: credential.type,
    clientExtensionResults: [],
  } as AuthenticationCredentialJSON;

  console.log('[AssertionCredential]', encodedCredential);

  // Verify and store the credential.
  return _fetch('/webauthn/authResponse', encodedCredential);
};

/**
 *  Remove a credential.
 * @param credId 
 * @returns 
 */
const removeCredential = (credId: string) => async () => {
  if (!confirm('Are you sure you want to remove this credential?')) {
    return;
  }
  try {
    loading.start();
    await _fetch('/webauthn/removeCredential', { credId });
    showSnackbar('The credential has been removed.');
    listCredentials();
  } catch (e) {
    console.error(e);
    showSnackbar('Removing the credential failed.');
  }
};

// const onToggleCheckboxes = (e: any): void => {
//   const checked = !e.target.checked;
//   const cards = document.querySelectorAll<HTMLDivElement>('#credentials .mdc-card__primary-action');
//   cards.forEach(card => {
//     const checkbox = card.querySelector<Checkbox>('mwc-checkbox');
//     if (checkbox) checkbox.checked = checked;
//   });
//   e.target.checked = checked;
// }

const onExcludeAllCredentials = (e: any): void => {
  const checked = !e.target.checked;
  const cards = document.querySelectorAll<HTMLDivElement>('#credentials .mdc-card__primary-action');
  cards.forEach(card => {
    const checkbox = card.querySelector<Checkbox>('mwc-checkbox');
    if (checkbox) checkbox.checked = checked;
  });
  e.target.checked = checked;
}

/**
 * Determine whether
 * `PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable()`
 * function is available.
 */
const onISUVPAA = async (): Promise<void> => {
  if (window.PublicKeyCredential) {
    if (PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable) {
      const result = await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable();
      if (result) {
        showSnackbar('User Verifying Platform Authenticator is *available*.');
      } else {
        showSnackbar('User Verifying Platform Authenticator is not available.');
      }
    } else {
      showSnackbar('IUVPAA function is not available.');
    }
  } else {
    showSnackbar('PublicKeyCredential is not availlable.');
  }
}

/**
 * On "Register New Credential" button click, invoke `registerCredential()`
 * function to register a new credential with advanced options.
 */
const onRegisterNewCredential = async (): Promise<void> => {
  loading.start();
  const opts = <WebAuthnRegistrationObject>collectOptions('registration');
  try {
    await registerCredential(opts);
    showSnackbar('A credential successfully registered!');
    listCredentials();
  } catch (e: any) {
    console.error(e);
    showSnackbar(e.message);
  } finally {
    loading.stop();
  }
};

/**
 * On "Register Platform Authenticator" button click, invoke
 * `registerCredential()` function to register a new credential with advanced
 * options overridden by `authenticatorAttachment == 'platform'` and
 * `userVerification = 'required'`.
 */
const onRegisterPlatformAuthenticator = async (): Promise<void> => {
  loading.start();
  const opts = <WebAuthnRegistrationObject>collectOptions('registration');
  opts.authenticatorSelection = opts.authenticatorSelection || {};
  opts.authenticatorSelection.authenticatorAttachment = 'platform';
  opts.authenticatorSelection.userVerification = 'required';
  try {
    await registerCredential(opts);
    showSnackbar('A credential successfully registered!');
    listCredentials();
  } catch (e: any) {
    console.error(e);
    showSnackbar(e.message);
  } finally {
    loading.stop();
  }
};

/**
 * On "Authenticate" button click, invoke `authenticate()` function to
 * authenticate the user.
 */
const onAuthenticate = async (): Promise<void> => {
  loading.start();
  const opts = <WebAuthnAuthenticationObject>collectOptions('authentication');
  try {
    // throw errors when the allow credential list is empty when discoverable credential is not required
    if (opts?.credentialsToAllow?.length == 0 && $('#resident-key').value !== 'required' ) {
      throw new Error('Authentication failed with NotSupportedError');
    }
    const credential = await authenticate(opts);
    rippleCard(credential.credentialID);
    showSnackbar('Authentication succeeded!');
  } catch (e: any) {
    console.error(e);
    showSnackbar(e.message);
  } finally {
    loading.stop();
  }
};

loading.start();

$('#user-icon').addEventListener('click', signout);
$('#isuvpaa-button').addEventListener('click', onISUVPAA);
$('#credential-button').addEventListener('click', onRegisterNewCredential);
$('#platform-button').addEventListener('click', onRegisterPlatformAuthenticator);
$('#authenticate-button').addEventListener('click', onAuthenticate);
// $('#toggle-checkboxes').addEventListener('click', onToggleCheckboxes);
$('#exclude-all-credentials').addEventListener('click', onExcludeAllCredentials);
