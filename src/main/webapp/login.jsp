<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<link rel="shortcut icon" href="favicon.ico">
<link rel="stylesheet"
  href="https://code.getmdl.io/1.3.0/material.teal-pink.min.css" />
<link href="https://fonts.googleapis.com/icon?family=Material+Icons"
  rel="stylesheet">
<link href="stylesheets/webauthn.css" rel="stylesheet">
<script src="//code.getmdl.io/1.3.0/material.min.js"></script>
<script src="js/login.js"></script>
<title>WebAuthN Login</title>
</head>
<body>
  <div class="mdl-layout mdl-js-layout mdl-layout--fixed-header">
    <header class="mdl-layout__header">
      <div class="mdl-layout-icon"></div>
      <div class="mdl-layout__header-row">
        <span class="mdl-layout__title">WebAuthN Login Demo</span>
        <div class="mdl-layout-spacer"></div>
        <nav class="mdl-navigation">
          <a href="${logoutUrl}">
            <button
              class="mdl-button mdl-js-button logout-button mdl-js-ripple-effect">
              Logout</button>
          </a>
        </nav>
      </div>
    </header>
    <main class="mdl-layout__content mdl-color--grey-100">
    <div id="outer-card" class="gone username">
      <div id="username-card" class="login-card mdl-card mdl-shadow--2dp">
        <div class="signin-title">
          <div class="mdl-card__title mdl-card--expand">
            <h2 class="mdl-card__title-text">Sign in</h2>
          </div>
        </div>
        <div id="instruction-text" class="instruction-text mdl-card__supporting-text">
          <div id="instructions">
          Please enter a username. For your privacy, this data is never sent to the server.
          </div>
          <div class="boxes">
            <div id="top-box"
              class="top-box hidden mdl-textfield mdl-js-textfield mdl-textfield--floating-label">
              <input type="text" id="username-text" maxlength="25"
                class="mdl-textfield__input"> <label
                class="mdl-textfield__label" for="username-text">Username</label>
            </div>
            <div id="bottom-box"
              class="bottom-box hidden mdl-textfield mdl-js-textfield mdl-textfield--floating-label">
              <input type="password" id="password-text"
                class="mdl-textfield__input"> <label
                class="mdl-textfield__label" for="password-text">Password</label>
            </div>
          </div>
          <div id="auth-spinner" class="auth-spinner hidden mdl-spinner mdl-js-spinner is-active"></div>
        </div>
        <div class="outer-next">
          <button id="next-button"
            class="next-button mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect mdl-button--colored">
            Next</button>
        </div>
      </div>
    </div>
    <div id="password" class="hidden password">Password</div>
    <div id="touch" class="hidden touch">Touch</div>
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
    </main>
  </div>
</body>
</html>
