# WebAuthnDemo

An example TypeScript Relying Party implementation of the [WebAuthn
specification](https://w3c.github.io/webauthn/).

## Install

Checkout the repository, then install.

```sh
$ npm install
```

## Set up Firebase

To run this project, you'll need [Cloud
Firestore](https://firebase.google.com/docs/firestore). Follow the instructions
to set one up (You may be able to run using local emulator as well).

Download [the service account
key](https://console.cloud.google.com/iam-admin/serviceaccounts) as a JSON file
and put it under `src` directory as `service-account.json`.

## Run the server locally

```sh
$ npm run dev
```
