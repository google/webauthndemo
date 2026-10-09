<!--
Copyright 2026 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Agent context and guidelines for WebAuthnDemo

## Project overview and architecture

WebAuthnDemo (`https://try-webauthn.appspot.com`) is Google's reference TypeScript Relying Party implementation of the [WebAuthn specification](https://w3c.github.io/webauthn/), designed for testing and inspecting WebAuthn registration (`navigator.credentials.create`) and authentication (`navigator.credentials.get`) ceremonies, CTAP/CBOR structures, and passkey behaviors across web and Android environments.

The backend runs on Node.js (v24+, native ES modules compiled from `.mts` to `.mjs`) using Express, `express-handlebars` for HTML template rendering, and `express-session` persisted to Google Cloud Firestore via `firebase-admin`. Server-side WebAuthn ceremony generation and verification are powered by `@simplewebauthn/server`, with authenticator metadata resolved from `aaguid` (`passkey-authenticator-aaguids`). User sign-in is handled by Firebase Authentication (`firebase` and `firebaseui` on the client, verified on the server via `firebase-admin/auth`).

The frontend lives under `src/public/` and is built with TypeScript, Lit (`lit`), Material Web Components (`@material/mwc-*`, `@material/card`, `@material/ripple`), `cbor-web` for client-side CBOR decoding, and `@alenaksu/json-viewer` for interactive payload inspection. Rollup (`rollup.config.js`) bundles client scripts (`main.ts` and `components.ts`), compiles SCSS (`style.scss`), and copies static assets, Handlebars templates, `firebase.json`, and environment files into `dist/`.

## Directory structure

- `src/server.mts`: Express server entry point, Handlebars view engine setup, Digital Asset Links (`/.well-known/assetlinks.json`), passkey endpoints (`/.well-known/passkey-endpoints`), and mounting of `/auth` and `/webauthn` routers.
- `src/libs/`: Server-side application modules compiled by `tsc --project ./src/tsconfig.json` into `dist/libs/*.mjs`:
    - `config.mts`: Environment configuration, Firebase Admin / Firestore initialization (switching automatically to local emulators when `NODE_ENV` is `localhost` or unset), and `express-session` setup.
    - `auth.mts`: Firebase Auth ID token verification (`POST /auth/verify`), session user info (`POST /auth/userInfo`), and sign-out (`POST /auth/signout`).
    - `webauthn.mts`: WebAuthn registration and authentication option generation and response verification (`/webauthn/registerRequest`, `/webauthn/registerResponse`, `/webauthn/signinRequest`, `/webauthn/signinResponse`), credential management routes, AAGUID lookup (`/webauthn/aaguids`), and Android APK key hash origin resolution.
    - `credential.mts`: Firestore CRUD operations for stored WebAuthn credentials.
    - `helper.mts`: CSRF header validation (`X-Requested-With`) and session authorization middleware.
- `src/public/`: Frontend source code bundled by Rollup into `dist/public/`:
    - `scripts/main.ts`: Main UI logic, Firebase Auth sign-in flow, WebAuthn option form controls, ceremony execution, CBOR/JSON inspection, and credential list rendering.
    - `scripts/components.ts`: Material Web Components and JSON viewer registration bundle.
    - `scripts/common.ts`, `scripts/util.ts`, `scripts/base64url.ts`: Shared interfaces, DOM/fetch helpers, and base64url utilities.
    - `styles/style.scss` & `styles/style.js`: Application styles compiled into `dist/public/styles/style.css`.
    - `tsconfig.json`: Client-side TypeScript configuration used by `@rollup/plugin-typescript`.
- `src/templates/`: Handlebars HTML views (`index.html`, `layouts/main.html`, and `partials/`).
- `app.yaml` & `dev.yaml`: Google App Engine Standard (`nodejs24`) deployment manifests for production (`try-webauthn.appspot.com`) and development (`dev-dot-try-webauthn.appspot.com`).

## Build, local development, and deployment

Always execute Node.js, `npm`, and `npx` commands through `mise` (`/usr/local/google/home/agektmr/.local/bin/mise exec -- ...`) rather than `nvm`.

- `npm run build`: Cleans `./dist`, runs `rollup -c` (bundling `src/public/` and copying templates/assets and `src/.env.development` to `dist/.env`), and runs `tsc --project ./src/tsconfig.json` to compile server `.mts` files into `dist/`.
- `npm run build:prod`: Cleans `./dist` and builds with `NODE_ENV:production` (disabling inline sourcemaps and copying `src/.env` if present).
- `npm run emulator`: Starts local Firebase Firestore (`localhost:8081`) and Auth (`127.0.0.1:9099`) emulators for project `try-webauthn`, importing and exporting seed data from `./.data`.
- `npm run dev`: Starts the compiled server locally (`NODE_ENV=localhost node dist/server.mjs`) on port `8080`.
- `npm run deploy` / `npm run deploy:prod`: Deploys `dev.yaml` or `app.yaml` to Google App Engine (`--project=try-webauthn`).

## Coding standards and Git worktree workflow

All new source files must include the standard Google LLC Apache 2.0 license header at the top of the file. Maintain strict TypeScript typing across both backend (`src/tsconfig.json`) and frontend (`src/public/tsconfig.json`) targets, and always verify that `npm run build` succeeds with zero Rollup or TypeScript errors before committing.

When developing features, updating dependencies, or fixing bugs, always use Git Worktrees under `.worktree/<branch-name>` (`git worktree add .worktree/<branch-name> -b <branch-name> origin/main`) instead of switching branches in the repository root. Carry out all edits, builds, and commits inside `.worktree/<branch-name>`, and remove the worktree (`git worktree remove .worktree/<branch-name>`) once the pull request is merged.

<!-- END_OF_AGENTS_MD -->
