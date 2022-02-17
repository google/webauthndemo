import path from 'path';
// @ts-ignore The file will be copied with rollup and no problem.
import firebaseJson from './firebase.json';
import dotenv from 'dotenv';
dotenv.config({ path: path.join(__dirname, ".env") });

if (process.env.NODE_ENV === 'localhost') {
  // Ideally this is configured with `.env`;
  process.env.FIRESTORE_EMULATOR_HOST = `localhost:${firebaseJson.emulators.firestore.port}`;
}

import express, { Request, Response, RequestHandler } from 'express';
import session from 'express-session';
import useragent from 'express-useragent';
import { engine } from 'express-handlebars';
import { getFirestore } from 'firebase-admin/firestore';
import { FirestoreStore } from '@google-cloud/connect-firestore';
import helmet from 'helmet';

import { auth } from './libs/auth';
import { webauthn } from './libs/webauthn';

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
  next();
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
      site: process.env.ORIGIN,
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
  res.json(assetlinks);
});

app.get('/', (req: Request, res: Response) => {
  res.render('index.html');
});

// listen for requests :)
app.listen(process.env.PORT || 8080, () => {
  console.log(`Your app is listening on port ${process.env.PORT || 8080}`);
});

app.use('/auth', auth);
app.use('/webauthn', webauthn);
