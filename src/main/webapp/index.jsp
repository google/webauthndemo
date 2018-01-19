<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<link rel="shortcut icon" href="favicon.ico">
<!-- <link rel="stylesheet"
  href="https://code.getmdl.io/1.3.0/material.teal-pink.min.css" /> -->
<!-- <link href="https://fonts.googleapis.com/icon?family=Material+Icons"
  rel="stylesheet"> -->
<script src="bower_components/webcomponentsjs/webcomponents-loader.js"></script>
<link href="stylesheets/webauthn.css" rel="stylesheet">
<link rel="import" href="bower_components/polymer/polymer.html">
<link rel="import" href="bower_components/app-layout/app-layout.html">
<link rel="import" href="bower_components/iron-list/iron-list.html">
<link rel="import" href="bower_components/paper-button/paper-button.html">
<link rel="import" href="bower_components/paper-toggle-button/paper-toggle-button.html">
<!-- <script src="//ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js"></script> -->
<!-- <script src="//code.getmdl.io/1.3.0/material.min.js"></script> -->
<script src="js/webauthn.js"></script>
<style>
  body {
    margin: 0;
    font-family: 'Roboto', 'Noto', sans-serif;
    line-height: 1.5;
    min-height: 100vh;
    background-color: #eeeeee;
  }
  app-header {
    background-color: #0b8043;
    color: white;
    --app-header-background-front-layer: {
      background-color: #4285f4;
    }
    font-size: var(--app-toolbar-font-size, 20px);
  }
  .primary {
    font-size: 16px;
    font-weight: bold;
  }
  .secondary {
    font-size: 14px;
  }
</style>
<title>WebAuthN Demo</title>
</head>
<body>
  <app-header>
    <app-toolbar>
      <div main-title>WebAuthN Demo</div>
      <paper-button raised>Register New Credential</paper-button>
      <paper-button raised>Authenticate</paper-button>
      <paper-button>Logout</paper-button>
    </app-toolbar>
  </app-header>
  <section>
    <paper-toggle-button>Advanced Options</paper-toggle-button>
  </section>
  <iron-list>
    test
  </iron-list>
  <!-- <div class="mdl-layout mdl-js-layout mdl-layout--fixed-header">
    <header class="mdl-layout__header">
      <div class="mdl-layout-icon"></div>
      <div class="mdl-layout__header-row">
        <span class="mdl-layout__title">WebAuthN Demo - ${nickname}</span>
        <div class="mdl-layout-spacer"></div>
        <nav class="mdl-navigation">
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
      <div
        class="mdl-progress mdl-js-progress mdl-progress__indeterminate page-width"></div>
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
    </div>
    <div id="credentials" class="mdl-grid mdl-grid--no-spacing"></div>
    <div id="github" class="github-link">
      <a href="https://github.com/google/webauthndemo">GitHub</a>
    </div>
    </main>
  </div> -->
</body>
</html>