<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="shortcut icon" href="favicon.ico">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="material/material.deep_purple-pink.min.css">
<link href="https://fonts.googleapis.com/icon?family=Material+Icons"
  rel="stylesheet">
<script src="js/jquery-3.2.1.min.js"></script>
<script src="material/material.min.js"></script>
<script src="js/webauthn.js"></script>
<style>
.mdl-cell {
  padding: 30px;
  text-align: left;
}

.mdl-card__subtitle-text {
  padding-left: .2cm;
  font-weight: bold;
}

.auth-button {
  margin-left: .25cm;
  margin-right: .25cm;
}

.make-button {
  margin-left: .25cm;
  margin-right: .25cm;
}

.logout-button {
  margin-left: .25cm;
  margin-right: .25cm;
  color: white;
}

.hidden {
  display: none;
}

.activity-bar {
  padding-top: 0;
  padding-bottom: .2cm;
  margin: 0;
}

.page-width {
  width: 100%;
}

.active-text {
  margin: 0;
  background-color: white;
  text-align: center;
  font-size: medium;
}
</style>
<title>WebAuthN Demo</title>
</head>
<body>
  <div class="mdl-layout mdl-js-layout mdl-layout--fixed-header">
    <header class="mdl-layout__header">
    <div class="mdl-layout-icon"></div>
    <div class="mdl-layout__header-row">
      <span class="mdl-layout__title">WebAuthN Demo</span>
      <div class="mdl-layout-spacer"></div>
      <nav class="mdl-navigation">
      <button id="credential-button"
        class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect make-button">
        Register New Credential</button>
      <button id="authenticate-button"
        class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect auth-button">
        Authenticate</button>
      <a href="/_ah/logout?continue=%2Findex.jsp">
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
      <div id="p2"
        class="mdl-progress mdl-js-progress mdl-progress__indeterminate page-width"></div>
    </div>
    <div id="credentials" class="mdl-grid mdl-grid--no-spacing"></div>
    </main>
  </div>
</body>
</html>