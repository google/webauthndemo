/*
 * @license
 * Copyright 2024 Google Inc. All rights reserved.
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
 * limitations under the License
 */

import url from 'url';
import path from 'path';
import crypto from 'crypto';

import dotenv from 'dotenv';

import session from 'express-session';
import { initializeApp } from 'firebase-admin/app';
import { getFirestore, Firestore } from 'firebase-admin/firestore';

import packageConfig from '../../package.json' with { type: 'json' };
import firebaseConfig from '../../firebase.json' with { type: 'json' };

interface FirestoreStoreOptions {
  dataset: Firestore;
  kind?: string;
}

class FirestoreStore extends session.Store {
  private db: Firestore;
  private kind: string;

  constructor(options: FirestoreStoreOptions) {
    super();
    if (!options.dataset) {
      throw new Error('No dataset provided to Firestore Session.');
    }
    this.db = options.dataset;
    this.kind = options.kind || 'Session';
  }

  override get = (
    sid: string,
    callback: (err: any, session?: session.SessionData | null) => void
  ): void => {
    this.db
      .collection(this.kind)
      .doc(sid)
      .get()
      .then(doc => {
        if (!doc.exists) {
          return callback(null, null);
        }
        try {
          const data = doc.data()?.data;
          if (!data) {
            return callback(null, null);
          }
          const result = typeof data === 'string' ? JSON.parse(data) : data;
          return callback(null, result);
        } catch (err) {
          return callback(err);
        }
      }, callback);
  };

  override set = (
    sid: string,
    sess: session.SessionData,
    callback?: (err?: any) => void
  ): void => {
    let sessJson: string;
    try {
      sessJson = JSON.stringify(sess);
    } catch (err) {
      return callback?.(err);
    }
    this.db
      .collection(this.kind)
      .doc(sid)
      .set({ data: sessJson })
      .then(() => callback?.(), callback);
  };

  override destroy = (
    sid: string,
    callback?: (err?: any) => void
  ): void => {
    this.db
      .collection(this.kind)
      .doc(sid)
      .delete()
      .then(() => callback?.(), callback);
  };
}

const is_localhost =
  process.env.NODE_ENV === 'localhost' || !process.env.NODE_ENV;

/**
 * During development, the server application only receives requests proxied
 * from the frontend tooling (e.g. Vite). This is because the frontend tooling
 * is responsible for serving the frontend application during development, to
 * enable hot module reloading and other development features.
 */
const is_development_proxy = process.env.PROXY;

const project_root_file_path = path.join(
  url.fileURLToPath(import.meta.url),
  '../../..'
);
const dist_root_file_path = path.join(project_root_file_path, 'dist');

console.log('Reading config from', path.join(dist_root_file_path, '/.env'));
dotenv.config({path: path.join(dist_root_file_path, '/.env')});

if (is_localhost) {
  process.env.FIRESTORE_EMULATOR_HOST = `${firebaseConfig.emulators.firestore.host}:${firebaseConfig.emulators.firestore.port}`;
  process.env.FIREBASE_AUTH_EMULATOR_HOST = `${firebaseConfig.emulators.auth.host}:${firebaseConfig.emulators.auth.port}`;
}

initializeApp({
  projectId: process.env.GOOGLE_CLOUD_PROJECT || 'try-webauthn',
});

export const store = getFirestore(process.env.FIRESTORE_DATABASENAME || '');
store.settings({ignoreUndefinedProperties: true});

export function initializeSession() {
  let session_name;
  if (is_localhost) {
    session_name = process.env.SESSION_STORE_NAME || 'session';
  } else {
    session_name = `__Host-${process.env.SESSION_STORE_NAME || 'session'}`;
  }

  return session({
    name: session_name,
    secret: config.secret,
    resave: false,
    saveUninitialized: false,
    proxy: true,
    store: new FirestoreStore({
      dataset: store,
      kind: 'express-sessions',
    }),
    cookie: {
      secure: !is_localhost,
      path: '/',
      sameSite: 'strict',
      httpOnly: true,
      maxAge: 1000 * 60 * 60 * 24 * 365, // 1 year
    }
  });
}

function configureApp() {
  const localhost = `http://localhost:${process.env.PORT || 8080}`;
  const origin = is_localhost ?  localhost : process.env.ORIGIN || localhost;
  const project_name = process.env.PROJECT_NAME || 'try-webauthn';

  return {
    project_name,
    debug: is_localhost || process.env.NODE_ENV === 'development',
    project_root_file_path,
    dist_root_file_path,
    views_root_file_path: path.join(dist_root_file_path, 'templates'),
    is_localhost,
    port: is_development_proxy ? 8080 : process.env.PORT || 8080,
    origin,
    secret: process.env.SECRET || crypto.randomBytes(32).toString('hex'),
    hostname: new URL(origin).hostname,
    title: project_name,
    repository_url: packageConfig.repository?.url,
    id_token_lifetime: parseInt(
      process.env.ID_TOKEN_LIFETIME || `${1 * 24 * 60 * 60 * 1000}`
    ),
    forever_cookie_duration: 1000 * 60 * 60 * 24 * 365,
    short_session_duration: parseInt(
      process.env.SHORT_SESSION_DURATION || `${3 * 60 * 1000}`
    ),
    long_session_duration: parseInt(
      process.env.LONG_SESSION_DURATION || `${1000 * 60 * 60 * 24 * 365}`
    ),
  };
}

export const config = configureApp();
