<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="shortcut icon" href="favicon.ico">
<link rel="stylesheet"
  href="https://code.getmdl.io/1.3.0/material.teal-pink.min.css" />
<link href="https://fonts.googleapis.com/icon?family=Material+Icons"
  rel="stylesheet">
<link href="stylesheets/webauthn.css" rel="stylesheet">
<script src="//code.getmdl.io/1.3.0/material.min.js"></script>
<!-- Web Animations polyfill for Edge. -->
<script src="https://rawgit.com/web-animations/web-animations-js/master/web-animations.min.js"></script>
<title>WebAuthn Demo</title>
</head>
<body>
  <div class="mdl-layout mdl-js-layout mdl-layout--fixed-header">
    <header class="mdl-layout__header">
      <div class="mdl-layout__header-row">
        <span class="mdl-layout__title">WebAuthn Demo</span>
      </div>
      <div id="header-buttons" class="mdl-layout__header-row">
        <div class="mdl-layout-spacer"></div>
        <button id="isuvpaa-button"
          class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect make-button"
          title="Calls IsUserVerifyingPlatformAuthenticatorAvailable. Returns &quot;true&quot; if the device supports an internal authenticator, and &quot;false&quot; if the device does not. (E.g., fingerprints on Android or TouchID on MacOS)"
          >
          isUVPAA</button>
        <button id="platform-button"
          class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect make-button"
          title="A convenience method to register an internal authenticator. This is equivalent to calling MakeCredential with AttachmentType=Platform"
          >
          Register platform authenticator</button>
        <button id="credential-button"
          class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect make-button"
          title="Calls MakeCredential to register a new credential from a FIDO device"
          >
          Register new credential</button>
        <button id="authenticate-button"
          class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect auth-button"
          title="Calls GetAssertion to request an assertion from a previously-registered FIDO device"
          >
          Authenticate</button>
      </div>
    </header>
    <div class="mdl-layout__drawer">
      <span class="mdl-layout-title">Advanced Options</span>
      <div class="mdl-list">
        <div class="mdl-list__item">
          <label class="mdl-checkbox mdl-js-checkbox" for="switch-rr">
            <input type="checkbox" id="switch-rr" class="mdl-checkbox__input">
            <span class="mdl-checkbox__label">Prevent Reregistration</span>
          </label>
        </div>
        <div class="mdl-list__item">
        <label for="attachment" class="attachment">Attachment Type</label>
        <select id="attachment" class="attachment">
          <option value="none">N/A</option>
          <option value="platform">Platform</option>
          <option value="cross-platform">Cross-Platform</option>
        </select>
        </div>
        <div class="mdl-list__item">
          <label for="conveyance" class="attachment">Conveyance Preference</label>
          <select id="conveyance" class="attachment">
            <option value="NA">N/A</option>
            <option value="none">None</option>
            <option value="indirect">Indirect</option>
            <option value="direct">Direct</option>
          </select>
        </div>
        <div class="mdl-list__item">
          <label class="mdl-checkbox mdl-js-checkbox mdl-js-ripple-effect" for="switch-rk">
            <input type="checkbox" id="switch-rk" class="mdl-checkbox__input">
            <span class="mdl-checkbox__label">Require resident key</span>
          </label>
        </div>
        <div class="mdl-list__item">
          <label for="userVerification" class="attachment">User Verification</label>
          <select id="userVerification" class="attachment">
            <option value="none">None</option>
            <option value="required">Required</option>
            <option value="preferred">Preferred</option>
            <option value="discouraged">Discouraged</option>
          </select>
        </div>
        <div class="mdl-list__item">
          <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label">
            <input class="mdl-textfield__input" type="text" pattern="-?[0-9]*(\.[0-9]+)?" id="customTimeout">
            <label class="mdl-textfield__label" for="customTimeout">Timeout (milliseconds)</label>
            <span class="mdl-textfield__error">Input is not a number!</span>
          </div>
        </div>
        <div class="mdl-list__item">
          <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label">
            <input class="mdl-textfield__input" type="text" pattern="-?[0-9]*(\.[0-9]+)?" id="abortTimeout">
            <label class="mdl-textfield__label" for="abortTimeout">AbortTimeout (milliseconds)</label>
            <span class="mdl-textfield__error">Input is not a number!</span>
          </div>
        </div>
        <div class="mdl-list__item">
          <a href="${logoutUrl}">Logout</a>
        </div>
      </div>
    </div>
    <main class="flex-layout">
      <div class="mdl-layout__content mdl-color--grey-100 flex-content">
        <div id="active" class="hidden activity-bar">
          <h3 class="active-text">Waiting for user touch</h3>
          <div class="mdl-progress mdl-js-progress mdl-progress__indeterminate page-width"></div>
        </div>
        <div id="credentials" class="mdl-grid mdl-grid--no-spacing"></div>
      </div>
      <footer id="github" class="mdl-mini-footer">
        <div class="mdl-mini-footer__left-section">
          <ul class="mdl-mini-footer__link-list">
            <li>${nickname}</li>
          </ul>
        </div>
        <div class="mdl-mini-footer__right-section">
          <ul class="mdl-mini-footer__link-list">
            <li>
              <a href="https://github.com/google/webauthndemo">GitHub</a>
            </li>
          </ul>
        </div>
      </footer>
      <div id="snack-bar" class="mdl-js-snackbar mdl-snackbar">
        <div class="mdl-snackbar__text"></div>
        <button class="mdl-snackbar__action" type="button"></button>
      </div>
    </main>
  </div>
  <script src="js/webauthn.js"></script>
</body>
</html>
