import path from 'path';

// If this is a local environment, connect to the emulator.
process.env.IS_LOCALHOST = process.env.NODE_ENV == 'localhost' ? 'true' : 'false';

import express, { Request, Response, RequestHandler } from 'express';
import session from 'express-session';
import hbs from 'express-handlebars';
import { getFirestore } from 'firebase-admin/firestore';
import { FirestoreStore } from '@google-cloud/connect-firestore';
import { auth } from './libs/auth';
import { webauthn } from './libs/webauthn';

const views = path.join(__dirname, 'templates');
const app = express();
app.set('view engine', 'html');
app.engine('html', hbs({
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
    sameSite: 'strict',
    maxAge: 1000 * 60 * 60 * 24 * 365, // 1 year
  }
}));

app.use((req, res, next) => {
  process.env.HOSTNAME = process.env.HOSTNAME || req.hostname;
  const protocol = process.env.IS_LOCALHOST == 'true' ? 'http' : 'https';
  process.env.ORIGIN = process.env.ORIGIN || `${protocol}://${req.headers.host}`;

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
