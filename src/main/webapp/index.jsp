<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<link rel="shortcut icon" href="favicon.ico">
<script src="bower_components/webcomponentsjs/webcomponents-loader.js"></script>
<link rel="import" href="bower_components/polymer/polymer.html">
<link rel="import" href="../bower_components/neon-animation/web-animations.html">
<link rel="import" href="../bower_components/polymer/polymer-element.html">
<link rel="import" href="../bower_components/app-layout/app-layout.html">
<link rel="import" href="../bower_components/iron-list/iron-list.html">
<link rel="import" href="../bower_components/paper-button/paper-button.html">
<link rel="import" href="../bower_components/paper-toggle-button/paper-toggle-button.html">
<link rel="import" href="../bower_components/iron-icon/iron-icon.html">
<link rel="import" href="../bower_components/iron-icons/iron-icons.html">
<link rel="import" href="../bower_components/paper-card/paper-card.html">
<link rel="import" href="../bower_components/paper-dropdown-menu/paper-dropdown-menu.html">
<link rel="import" href="../bower_components/paper-listbox/paper-listbox.html">
<link rel="import" href="../bower_components/paper-item/paper-item.html">
<link rel="import" href="../bower_components/paper-toast/paper-toast.html">
<link rel="import" href="../bower_components/paper-progress/paper-progress.html">

<!-- <link rel="import" href="components/wad-app.html"> -->
<style>
  body {
    margin: 0;
    font-family: 'Roboto', 'Noto', sans-serif;
    line-height: 1.5;
    min-height: 100vh;
    background-color: #eeeeee;
  }
  a {
    color: white;
    text-decoration: none;
  }
  app-header {
    background-color: rgb(0,150,136);
    color: white;
  }
  paper-button {
    font-size: 14px;
  }
  app-toolbar paper-button {
    background-color: rgb(255,64,129);
  }
  .logout {
    background-color: transparent;
  }
  .side-padding {
    padding: 0 16px;
  }
  .card-actions {
    text-align: right;
  }
  paper-progress {
    width: 100%;
  }
  #options {
    padding: 16px;
    background-color: white;
  }
  #advanced > * {
    margin-right: 16px;
  }
  #advanced paper-toggle-button {
    display: inline-block;
    vertical-align: middle;
  }
  .subtitle-text {
    font-size: 16px;
    font-weight: bold;
    color: rgba(0,0,0,.54);
  }
  .supporting-text {
    padding-left: .2cm;
    font-size: 14px;
    font-style: italic;
    overflow-wrap: break-word;
    white-space: pre-wrap;
    color: rgba(0,0,0,.54);
  }
</style>
<title>WebAuthN Demo</title>
</head>
<body>
  <dom-bind>
    <template>
      <custom-style>
        <style is="custom-style">
          paper-card {
            margin: 30px;
            max-width: 400px;
            --paper-card-header: {
              border-bottom: 1px solid #e8e8e8;
            }
            --paper-card-header-text: {
              font-size: 18px;
            }
          }
        </style>
      </custom-style>
      <app-header-layout>
        <app-header slot="header" fixed shadow>
          <app-toolbar>
            <div main-title>WebAuthN Demo - ${nickname}</div>
            <paper-button id="add" raised>Register New Credential</paper-button>
            <paper-button id="auth" raised>Authenticate</paper-button>
            <paper-button class="logout">
              <a href="${logoutUrl}">Logout</a>
            </paper-button>
          </app-toolbar>
          <section id="options">
            <paper-toggle-button checked="{{advanced}}">Advanced Options</paper-toggle-button>
            <template is="dom-if" if="[[advanced]]">
              <div id="advanced">
                <paper-toggle-button checked="{{excludeCredentials}}">Prevent Reregistration</paper-toggle-button>
                <paper-dropdown-menu label="Attachment type">
                  <paper-listbox
                    attr-for-selected="item-name"
                    selected="{{authenticatorAttachment}}"
                    slot="dropdown-content">
                    <paper-item item-name="none">N/A</paper-item>
                    <paper-item item-name="platform">Platform</paper-item>
                    <paper-item item-name="cross-platform">Cross-Platform</paper-item>
                  </paper-listbox>
                </paper-dropdown-menu>
                <paper-dropdown-menu label="Conveyance Preference">
                  <paper-listbox
                    attr-for-selected="item-name"
                    selected="{{attestationConveyancePreference}}"
                    slot="dropdown-content">
                    <paper-item item-name="NA">N/A</paper-item>
                    <paper-item item-name="none">None</paper-item>
                    <paper-item item-name="indirect">Indirect</paper-item>
                    <paper-item item-name="direct">Direct</paper-item>
                  </paper-listbox>
                </paper-dropdown-menu>
                <paper-toggle-button checked="{{requireResidentKey}}">Require resident key</paper-toggle-button>
                <paper-dropdown-menu label="User Verification">
                  <paper-listbox
                    attr-for-selected="item-name"
                    selected="{{userVerification}}"
                    slot="dropdown-content">
                    <paper-item item-name="none">None</paper-item>
                    <paper-item item-name="required">Required</paper-item>
                    <paper-item item-name="preferred">Preferred</paper-item>
                    <paper-item item-name="discouraged">Discouraged</paper-item>
                  </paper-listbox>
                </paper-dropdown-menu>
              </div>
            </template>
          </section>
        </app-header>
        <template is="dom-if" if="{{active}}">
          <section id="active">
            <paper-progress indeterminate class="blue"></paper-progress>
            <h3 class="side-padding">Waiting for user touch</h3>
          </section>
        </template>
        <section id="credentials">
          <template is="dom-repeat" items="[[credentials]]">
            <paper-card elevation="1" id="[[item.handle]]" heading="[[item.name]]">
              <div class="card-content">
                <div class="supporting-text">[[item.date]]</div>
                <div class="subtitle-text">Public Key</div>
                <div class="supporting-text">[[item.publicKey]]</div>
                <div class="subtitle-text">Key Handle</div>
                <div class="supporting-text">[[item.handle]]</div>
              </div>
              <div class="card-actions">
                <paper-button raised id="[[item.id]]">
                  <iron-icon icon="delete"></iron-icon>Delete
                </paper-button>
              </div>
            </paper-card>
          </template>
        </section>
      </app-header-layout>
      <paper-toast id="toast" duration="5000"></paper-toast>
    </template>
  </dom-bind>
  <script src="js/webauthn.js"></script>
</body>
</html>