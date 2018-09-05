<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<link rel="shortcut icon" href="favicon.ico">
<link rel="stylesheet"
  href="https://code.getmdl.io/1.3.0/material.teal-pink.min.css" />
<link href="https://fonts.googleapis.com/icon?family=Material+Icons"
  rel="stylesheet">
<link href="stylesheets/webauthn.css" rel="stylesheet">
<script src="//code.getmdl.io/1.3.0/material.min.js"></script>
<!-- Web Animations polyfill for Edge. -->
<script src="https://rawgit.com/web-animations/web-animations-js/master/web-animations.min.js"></script>
<script src="js/webauthn.js"></script>
<title>WebAuthn Demo</title>
</head>
<body>
  <div class="mdl-layout mdl-js-layout mdl-layout--fixed-header">
    <header class="mdl-layout__header">
      <div class="mdl-layout-icon"></div>
      <div class="mdl-layout__header-row">
        <span class="mdl-layout__title">WebAuthn Demo - ${nickname}</span>
        <div class="mdl-layout-spacer"></div>
        <nav class="mdl-navigation">
          <button id="isuvpaa-button"
            class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect isuvpaa-button">
            Check isUVPAA</button>
          <button id="credential-button"
            class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect make-button">
            Register New Credential</button>
          <button id="authenticate-button"
            class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect auth-button">
            Authenticate</button>
          <a href="${logoutUrl}">
            <button
              class="mdl-button mdl-js-button logout-button mdl-js-ripple-effect">
              Logout</button>
          </a>
        </nav>
      </div>
    </header>
    <main class="mdl-layout__content mdl-color--grey-100">
    <div id="active" class="hidden activity-bar">
      <h3 class="active-text">Waiting for user touch</h3>
    <div class="mdl-progress mdl-js-progress mdl-progress__indeterminate page-width"></div>
    </div>
    <div id="error" class="hidden">
      <h3 class="error-text" id="error-text">An error has occurred</h3>
    </div>
    <div id="success" class="hidden">
      <h3 class="success-text" id="success-text">Success</h3>
    </div>
    <div id="advanced-switch" class="advanced-switch">
      <label class="mdl-switch mdl-js-switch mdl-js-ripple-effect" for="switch-advanced">
        <input type="checkbox" id="switch-advanced" class="mdl-switch__input">
        <span class="mdl-switch__label">Advanced Options</span>
      </label>
    </div>
    <div id="advanced" class="advanced hidden">
      <label class="mdl-switch mdl-js-switch mdl-js-ripple-effect" for="switch-rr">
        <input type="checkbox" id="switch-rr" class="mdl-switch__input">
        <span class="mdl-switch__label">Prevent Reregistration</span>
      </label>
      <label for="attachment" class="attachment">Attachment Type</label>
      <select id="attachment" class="attachment">
        <option value="none">N/A</option>
        <option value="platform">Platform</option>
        <option value="cross-platform">Cross-Platform</option>
      </select>
      <br />
      <label for="conveyance" class="attachment">Conveyance Preference</label>
      <select id="conveyance" class="attachment">
        <option value="NA">N/A</option>
        <option value="none">None</option>
        <option value="indirect">Indirect</option>
        <option value="direct">Direct</option>
      </select>
      <label class="mdl-switch mdl-js-switch mdl-js-ripple-effect" for="switch-rk">
        <input type="checkbox" id="switch-rk" class="mdl-switch__input">
        <span class="mdl-switch__label">Require resident key</span>
      </label>
      <label for="userVerification" class="attachment">User Verification</label>
      <select id="userVerification" class="attachment">
        <option value="none">None</option>
        <option value="required">Required</option>
        <option value="preferred">Preferred</option>
        <option value="discouraged">Discouraged</option>
      </select>
      <br />
      <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label">
        <input class="mdl-textfield__input" type="text" pattern="-?[0-9]*(\.[0-9]+)?" id="customTimeout">
        <label class="mdl-textfield__label" for="customTimeout">Timeout (milliseconds)</label>
        <span class="mdl-textfield__error">Input is not a number!</span>
      </div>
    </div>
    <div id="credentials" class="mdl-grid mdl-grid--no-spacing"></div>
    <div id="github" class="github-link">
      <a href="https://github.com/google/webauthndemo">GitHub</a>
    </div>
    </main>
  </div>
</body>
</html>
