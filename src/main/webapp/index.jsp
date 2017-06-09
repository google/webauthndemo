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
<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
<script src="https://code.getmdl.io/1.3.0/material.min.js"></script>
<script src="js/webauthn.js"></script>
<title>WebAuthN Demo</title>
</head>
<body>
  <div class="mdl-layout mdl-js-layout mdl-layout--fixed-header">
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
      <h3 class="error-text">An error has occurred</h3>
    </div>
    <div id="credentials" class="mdl-grid mdl-grid--no-spacing"></div>
    <div id="github" class="github-link">
      <a href="https://github.com/google/webauthndemo">GitHub</a>
    </div>
    </main>
  </div>
</body>
</html>