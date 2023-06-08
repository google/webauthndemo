/**
 * Copyright 2022 Google LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import path from 'path';
import url from 'url';
// @ts-ignore The file will be copied with rollup and no problem.
import firebaseJson from './firebase.json' assert { type: 'json' };
import dotenv from 'dotenv';
const __dirname = url.fileURLToPath(new URL('.', import.meta.url));
dotenv.config({ path: path.join(__dirname, ".env") });

if (process.env.NODE_ENV === 'localhost') {
  // Ideally this is configured with `.env`;
  process.env.FIRESTORE_EMULATOR_HOST = `${firebaseJson.emulators.firestore.host}:${firebaseJson.emulators.firestore.port}`;
  process.env.FIREBASE_AUTH_EMULATOR_HOST = `${firebaseJson.emulators.auth.host}:${firebaseJson.emulators.auth.port}`;
}

import express, { Request, Response, RequestHandler } from 'express';
import session from 'express-session';
import useragent from 'express-useragent';
import { engine } from 'express-handlebars';
import { getFirestore } from 'firebase-admin/firestore';
import { FirestoreStore } from '@google-cloud/connect-firestore';
import helmet from 'helmet';

import { auth } from './libs/auth.mjs';
import { webauthn } from './libs/webauthn.mjs';

const views = path.join(__dirname, 'templates');
const app = express();
app.set('view engine', 'html');
app.engine('html', engine({
  extname: 'html',
}));
app.set('views', views);
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json() as RequestHandler);
app.use(useragent.express());

let session_name;
if (process.env.NODE_ENV === 'localhost') {
  session_name = process.env.SESSION_STORE_NAME || 'session';
} else {
  session_name = `__Host-${process.env.SESSION_STORE_NAME || 'session'}`;
}

// TODO: The session seems to live very short.
app.use(session({
  name: session_name,
  secret: process.env.SECRET || 'secret',
  resave: false,
  saveUninitialized: false,
  proxy: true,
  store: new FirestoreStore({
    dataset: getFirestore(),
    kind: 'express-sessions',
  }),
  cookie: {
    secure: process.env.NODE_ENV !== 'localhost',
    path: '/',
    sameSite: 'strict',
    httpOnly: true,
    maxAge: 1000 * 60 * 60 * 24 * 365, // 1 year
  }
}));

// Run helmet only when it's running on a remote server.
if (process.env.NODE_ENV !== 'localhost') {
  app.use(helmet.hsts());
}

app.use((req, res, next) => {
  res.locals.hostname = req.hostname;
  const protocol = process.env.NODE_ENV === 'localhost' ? 'http' : 'https';
  res.locals.origin = `${protocol}://${req.headers.host}`;
  res.locals.title = process.env.PROJECT_NAME;
  return next();
});

app.get('/.well-known/assetlinks.json', (req, res) => {
  const assetlinks = [];
  const relation = [
    'delegate_permission/common.handle_all_urls',
    'delegate_permission/common.get_login_creds',
  ];
  assetlinks.push({
    relation: relation,
    target: {
      namespace: 'web',
      site: res.locals.origin,
    },
  });
  if (process.env.ANDROID_PACKAGENAME && process.env.ANDROID_SHA256HASH) {
    const package_names = process.env.ANDROID_PACKAGENAME.split(",").map(name => name.trim());
    const hashes = process.env.ANDROID_SHA256HASH.split(",").map(hash => hash.trim());
    for (let i = 0; i < package_names.length; i++) {
      assetlinks.push({
        relation: relation,
        target: {
          namespace: 'android_app',
          package_name: package_names[i],
          sha256_cert_fingerprints: [hashes[i]],
        },
      });
    }
  }
  return res.json(assetlinks);
});

app.get('/.well-known/passkey-endpoints', (req, res) => {
  return res.json();
});

app.get('/', (req: Request, res: Response) => {
  return res.render('index.html');
});

// listen for requests :)
app.listen(process.env.PORT || 8080, () => {
  console.log(`Your app is listening on port ${process.env.PORT || 8080}`);
});

app.use('/auth', auth);
app.use('/webauthn', webauthn);
