import path from 'path';

if (process.env.NODE_ENV === 'localhost') {
  // If this is a local environment, connect to the emulator.
  process.env.IS_LOCALHOST = 'true';
  // Ideally this is configured with `.env`;
  process.env.FIRESTORE_EMULATOR_HOST = 'localhost:8081';
} else {
  process.env.IS_LOCALHOST = 'false';
}

import express, { Request, Response, RequestHandler } from 'express';
import session from 'express-session';
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

app.use(session({
  name: process.env.SESSION_STORE_NAME || 'session',
  secret: process.env.SECRET || 'secret',
  resave: false,
  saveUninitialized: false,
  proxy: true,
  store: new FirestoreStore({
    dataset: getFirestore(),
    kind: 'express-sessions',
  }),
  cookie: {
    secure: process.env.IS_LOCALHOST === 'false',
    path: "/",
    sameSite: 'strict',
    httpOnly: true,
    maxAge: 1000 * 60 * 60 * 24 * 365, // 1 year
  }
}));

// Run helmet only when it's running on a remote server.
if (process.env.IS_LOCALHOST === 'false') {
  app.use(helmet.hsts());
}

app.use((req, res, next) => {
  process.env.HOSTNAME = process.env.HOSTNAME || req.hostname;
  const protocol = process.env.IS_LOCALHOST == 'true' ? 'http' : 'https';
  process.env.ORIGIN = process.env.ORIGIN || `${protocol}://${req.headers.host}`;
  if (process.env.IS_LOCALHOST === 'false' && req.protocol === 'http') {
    return res.redirect(301, process.env.ORIGIN);
  }

  res.locals.title = process.env.PROJECT_NAME;
  next();
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
