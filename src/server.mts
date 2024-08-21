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
import express, { Request, Response, RequestHandler } from 'express';
import useragent from 'express-useragent';
import { engine } from 'express-handlebars';
import { config, initializeSession } from './libs/config.mjs';
import helmet from 'helmet';

import { auth } from './libs/auth.mjs';
import { webauthn } from './libs/webauthn.mjs';

const views = config.views_root_file_path;
const app = express();
app.set('view engine', 'html');
app.engine('html', engine({
  extname: 'html',
}));
app.set('views', views);
app.use(express.static(path.join(config.dist_root_file_path, 'public')));
app.use(express.json() as RequestHandler);
app.use(useragent.express());
app.use(initializeSession());

// Run helmet only when it's running on a remote server.
if (!config.is_localhost) {
  app.use(helmet.hsts());
}

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
      site: config.origin,
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
  // Temporarily hardcoded.
  const web_endpoint = config.origin;
  const enroll = {
    'web': web_endpoint
  };
  const manage = {
    'web': web_endpoint
  }
  return res.json({ enroll, manage });
});

app.get('/', (req: Request, res: Response) => {
  return res.render('index.html');
});

// listen for requests :)
app.listen(config.port || 8080, () => {
  console.log(`Your app is listening on port ${config.port || 8080}`);
});

app.use('/auth', auth);
app.use('/webauthn', webauthn);
