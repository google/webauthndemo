import express, { Request, Response } from 'express';
import { body, validationResult } from 'express-validator';
import base64url from 'base64url';
import { User, UserManager, StoredCredential } from '../libs/user';
import { getNow, csrfCheck, authzAPI, SESSION_REQUIREMENT } from '../libs/helper';
import {
  generateAttestationOptions,
  verifyAttestationResponse,
  generateAssertionOptions,
  verifyAssertionResponse,
} from '@simplewebauthn/server';
import {
  AttestationConveyancePreference,
  PublicKeyCredentialDescriptor,
  PublicKeyCredentialParameters,
  AuthenticatorDevice,
  AttestationCredentialJSON,
  AssertionCredentialJSON,
} from '@simplewebauthn/typescript-types';

const router = express.Router();

router.use(csrfCheck);

const RP_NAME = process.env.PROJECT_NAME || 'WebAuthn';
const WEBAUTHN_TIMEOUT = 1000 * 60 * 5; // 5 minutes

/**
 * Returns a list of credentials
 **/
router.post('/getKeys', authzAPI(SESSION_REQUIREMENT.RECENT), (
  req: Request,
  res: Response
): void => {
  if (!res.locals.user) {
    res.status(401).json({ error: 'Unauthorized' });
    return;
  }
  res.json(res.locals.user.credentials);
});

router.post('/renameKey',
  authzAPI(SESSION_REQUIREMENT.RECENT),
  body('deviceName').isLength({ min: 3, max: 30 }),
  async (
    req: Request,
    res: Response
  ): Promise<void> => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      res.sendStatus(400).json({ status: false, error: 'Invalid device name.' });
      return;
    }

    const user = <User>res.locals.user;
    const { credId, deviceName } = req.body;

    // Find the credential with the same credential ID
    const cred = user?.credentials.find(cred => cred.credentialID === credId);

    if (user && cred) {
      // Update the credential's device name.
      cred.deviceName = deviceName;
      UserManager.saveUser(user);
    }
    res.json({ status: true });
  }
);

/**
 * Removes a credential id attached to the user
 * Responds with empty JSON `{}`
 **/
router.post('/removeKey', authzAPI(SESSION_REQUIREMENT.RECENT), async (
  req: Request,
  res: Response
): Promise<void> => {
  // Unlikely exception
  if (!res.locals.user) throw 'Unauthorized.';

  const user = <User>res.locals.user;

  const newCreds = user.credentials.filter((cred) => {
    // Leave credential ids that do not match
    return cred.credentialID !== req.query.credId;
  });

  user.credentials = newCreds;
  await UserManager.saveUser(user);

  res.json({ status: true });
});

// router.get('/resetDB', (req, res) => {
//   db.set('users', []).write();
//   const users = db.get('users').value();
//   res.json(users);
// });

/**
 * Respond with required information to call navigator.credential.create()
 * Input is passed via `req.body` with similar format as output
 * Output format:
 * ```{
     rp: {
       id: String,
       name: String
     },
     user: {
       displayName: String,
       id: String,
       name: String
     },
     publicKeyCredParams: [{  // @herrjemand
       type: 'public-key', alg: -7
     }],
     timeout: Number,
     challenge: String,
     excludeCredentials: [{
       id: String,
       type: 'public-key',
       transports: [('ble'|'nfc'|'usb'|'internal'), ...]
     }, ...],
     authenticatorSelection: {
       authenticatorAttachment: ('platform'|'cross-platform'),
       requireResidentKey: Boolean,
       userVerification: ('required'|'preferred'|'discouraged')
     },
     attestation: ('none'|'indirect'|'direct')
 * }```
 **/
router.post('/registerRequest',
  authzAPI(SESSION_REQUIREMENT.RECENT),
  body('deviceName').isLength({ min: 3, max: 30 }),
  async (
    req: Request,
    res: Response
  ): Promise<void> => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        throw 'Invalid device name.';
      }

      // Unlikely exception
      if (!res.locals.user) throw 'Unauthorized.';

      // Unlikely exception
      if (!process.env.HOSTNAME) throw 'HOSTNAME not configured as an environment variable.';

      const user = <User>res.locals.user;
      const creationOptions = <PublicKeyCredentialCreationOptions>req.body || {};
      const deviceName = req.body.deviceName;

      const excludeCredentials: PublicKeyCredentialDescriptor[] = [];
      if (user.credentials.length > 0) {
        for (let cred of user.credentials) {
          excludeCredentials.push({
            id: base64url.toBuffer(cred.credentialID),
            type: 'public-key',
            transports: cred.transports,
          });
        }
      }
      const pubKeyCredParams: PublicKeyCredentialParameters[] = [];
      // const params = [-7, -35, -36, -257, -258, -259, -37, -38, -39, -8];
      const params = [-7, -257];
      for (let param of params) {
        pubKeyCredParams.push({ type: 'public-key', alg: param });
      }
      const as: AuthenticatorSelectionCriteria = {}; // authenticatorSelection
      const aa = creationOptions.authenticatorSelection?.authenticatorAttachment;
      const rk = creationOptions.authenticatorSelection?.residentKey;
      const rr = creationOptions.authenticatorSelection?.requireResidentKey;
      const uv = creationOptions.authenticatorSelection?.userVerification;
      const cp = creationOptions.attestation; // attestationConveyancePreference
      let asFlag = false;
      let authenticatorSelection;
      let attestation: AttestationConveyancePreference = 'none';

      if (aa && (aa == 'platform' || aa == 'cross-platform')) {
        asFlag = true;
        as.authenticatorAttachment = aa;
      }
      if (rk && (rk == 'required' || rk == 'preferred' || rk == 'discouraged')) {
        asFlag = true;
        as.residentKey = rk;
      }
      if (rr && typeof rr == 'boolean') {
        asFlag = true;
        as.requireResidentKey = rr;
      }
      if (uv && (uv == 'required' || uv == 'preferred' || uv == 'discouraged')) {
        asFlag = true;
        as.userVerification = uv;
      }
      if (asFlag) {
        authenticatorSelection = as;
      }
      if (cp && (cp == 'none' || cp == 'indirect' || cp == 'direct')) {
        attestation = cp;
      }

      const options = generateAttestationOptions({
        rpName: RP_NAME,
        rpID: process.env.HOSTNAME,
        userID: user.id,
        userName: user.username || 'Unnamed User',
        timeout: WEBAUTHN_TIMEOUT,
        // Prompt users for additional information about the authenticator.
        attestationType: attestation,
        // Prevent users from re-registering existing authenticators
        excludeCredentials,
        authenticatorSelection,
      });

      // TODO: Are you sure using AuthenticationSession is a good idea?
      req.session.enrollment = {
        username: user.username,
        expires_on: getNow() + WEBAUTHN_TIMEOUT,
        challenge: options.challenge,
        device: deviceName,
      };

      res.json(options);
    } catch (e) {
      res.status(400).send({ status: false, error: e });
    }
  }
);

/**
 * Register user credential.
 * Input format:
 * ```{
     id: String,
     type: 'public-key',
     rawId: String,
     response: {
       clientDataJSON: String,
       attestationObject: String,
       signature: String,
       userHandle: String
     }
 * }```
 **/
router.post('/registerResponse', authzAPI(SESSION_REQUIREMENT.RECENT), async (
  req: Request,
  res: Response
) => {
  try {
    // Unlikely exception
    if (!res.locals.user) throw 'Unauthorized.';

    if (!req.session.enrollment) throw 'No enrollment session.';

    // Unlikely exception
    if (!process.env.HOSTNAME) throw 'HOSTNAME not configured as an environment variable.';
    if (!process.env.ORIGIN) throw 'ORIGIN not configured as an environment variable.';

    const user = <User>res.locals.user;
    const credential = <AttestationCredentialJSON>req.body;

    const expectedChallenge = req.session.enrollment.challenge;
    const deviceName = req.session.enrollment.device;
    const expectedRPID = process.env.HOSTNAME;

    let expectedOrigin = '';
    const ua = req.get('User-Agent');

    // We don't plan to support Android native FIDO2 authenticators.
    if (ua && ua.indexOf('okhttp') > -1) {
      const hash = process.env.ANDROID_SHA256HASH;
      if (!hash) {
        throw 'ANDROID_SHA256HASH not configured as an environment variable.'
      }
      const octArray = hash.split(':').map(h => parseInt(h, 16));
      // @ts-ignore
      const androidHash = base64url.encode(octArray);
      expectedOrigin = `android:apk-key-hash:${androidHash}`; // TODO: Generate
    } else {
      expectedOrigin = process.env.ORIGIN;
    }

    const verification = await verifyAttestationResponse({
      credential,
      expectedChallenge,
      expectedOrigin,
      expectedRPID,
    });

    const { verified, attestationInfo } = verification;

    if (!verified || !attestationInfo) {
      throw 'User verification failed.';
    }

    const { credentialPublicKey, credentialID, counter } = attestationInfo;
    const base64PublicKey = base64url.encode(credentialPublicKey);
    const base64CredentialID = base64url.encode(credentialID);
    const { transports } = credential;

    const existingCred = user.credentials.find(
      cred => cred.credentialID === base64CredentialID,
    );

    if (!existingCred) {
      /**
       * Add the returned device to the user's list of devices
       */
      user.credentials.push({
        deviceName,
        credentialPublicKey: base64PublicKey,
        credentialID: base64CredentialID,
        counter,
        transports,
        registered: getNow(),
      } as StoredCredential);
    }

    await UserManager.saveUser(user);

    delete req.session.enrollment;

    // Respond with user info
    res.json(user.credentials);
  } catch (e) {
    delete req.session.enrollment;

    res.status(400).send({ status: false, error: e.message });
  }
});

/**
 * Respond with required information to call navigator.credential.get()
 * Input is passed via `req.body` with similar format as output
 * Output format:
 * ```{
     challenge: String,
     userVerification: ('required'|'preferred'|'discouraged'),
     allowCredentials: [{
       id: String,
       type: 'public-key',
       transports: [('ble'|'nfc'|'usb'|'internal'), ...]
     }, ...]
 * }```
 **/
router.post('/reauthRequest', authzAPI(SESSION_REQUIREMENT.AUTH), async (
  req: Request,
  res: Response
) => {
  // Unlikely exception
  if (!res.locals.user) throw 'Unauthorized.';

  try {
    const user = <User>res.locals.user;

    const credId = req.query.credId;
    const requestOptions = <PublicKeyCredentialRequestOptions>req.body;

    const userVerification = requestOptions.userVerification || 'required';

    const allowCredentials: PublicKeyCredentialDescriptor[] = [];
    for (let cred of user.credentials) {
      // When credId is not specified, or matches the one specified
      if (!credId || cred.credentialID == credId) {
        allowCredentials.push({
          id: base64url.toBuffer(cred.credentialID),
          type: 'public-key',
          transports: cred.transports
        });
      }
    }

    const options = generateAssertionOptions({
      timeout: WEBAUTHN_TIMEOUT,
      allowCredentials,
      userVerification,
    });

    req.session.auth = {
      username: user.username,
      expires_on: getNow() + WEBAUTHN_TIMEOUT,
      challenge: options.challenge
    };

    res.json(options);
  } catch (e) {
    res.status(400).json({ status: false, error: e });
  }
});

/**
 * Authenticate the user.
 * Input format:
 * ```{
     id: String,
     type: 'public-key',
     rawId: String,
     response: {
       clientDataJSON: String,
       authenticatorData: String,
       signature: String,
       userHandle: String
     }
 * }```
 **/
router.post('/reauthResponse', authzAPI(SESSION_REQUIREMENT.AUTH), async (
  req: Request,
  res: Response
) => {
  // Unlikely exception
  if (!res.locals.user) throw 'Unauthorized.';

  // Unlikely exception
  if (!process.env.HOSTNAME) throw 'HOSTNAME not configured as an environment variable.';
  if (!process.env.ORIGIN) throw 'ORIGIN not configured as an environment variable.';

  const user = <User>res.locals.user;
  const expectedChallenge = req.session.auth?.challenge || '';
  const expectedRPID = process.env.HOSTNAME;
  const expectedOrigin = process.env.ORIGIN;

  try {
    const claimedCred = <AssertionCredentialJSON>req.body;

    let storedCred = user.credentials.find((cred) => cred.credentialID === claimedCred.id);

    if (!storedCred) {
      throw 'Authenticating credential not found.';
    }

    const base64PublicKey = base64url.toBuffer(storedCred.credentialPublicKey);
    const base64CredentialID = base64url.toBuffer(storedCred.credentialID);
    const { counter, transports } = storedCred; 

    const authenticator: AuthenticatorDevice = {
      credentialPublicKey: base64PublicKey,
      credentialID: base64CredentialID,
      counter,
      transports
    }

    const verification = verifyAssertionResponse({
      credential: claimedCred,
      expectedChallenge,
      expectedOrigin,
      expectedRPID,
      authenticator,
    });

    const { verified, assertionInfo } = verification;

    if (!verified) {
      throw 'User verification failed.';
    }

    storedCred.counter = assertionInfo.newCounter;
    storedCred.last_used = getNow();

    await UserManager.saveUser(user);

    delete req.session.auth;
    req.session['user_id'] = user.id;
    req.session['last_signin'] = getNow();
    res.json(user.credentials);
  } catch (e) {
    delete req.session.auth?.challenge;
    res.status(400).json({ status: false, error: e });
  }
});

router.post('/usernamelessRequest', async (
  req: Request,
  res: Response
) => {
  try {
    const requestOptions = <PublicKeyCredentialRequestOptions>req.body;

    const userVerification = requestOptions.userVerification || 'required';

    const options = generateAssertionOptions({
      timeout: WEBAUTHN_TIMEOUT,
      allowCredentials: [],
      userVerification,
    });

    // Maybe we shouldn't use `auth` as the name for this
    // as it's already used for 2sv purposes. Or rename it to
    // `2sv` and keep this one as `auth`.
    req.session.auth = {
      expires_on: getNow() + WEBAUTHN_TIMEOUT,
      challenge: options.challenge
    };

    res.json(options);
  } catch (e) {
    res.status(400).json({ status: false, error: e });
  }
});

router.post('/usernamelessResponse', async (
  req: Request,
  res: Response
) => {
  // Unlikely exception
  if (!process.env.HOSTNAME) throw 'HOSTNAME not configured as an environment variable.';
  if (!process.env.ORIGIN) throw 'ORIGIN not configured as an environment variable.';

  const expectedChallenge = req.session.auth?.challenge || '';
  const expectedRPID = process.env.HOSTNAME;
  const expectedOrigin = process.env.ORIGIN;

  try {
    const claimedCred = <AssertionCredentialJSON>req.body;

    const user_id = claimedCred.response?.userHandle;
    if (!user_id) {
      throw 'User not found.';
    }

    const user = await UserManager.getUserById(user_id);

    let storedCred = user.credentials.find((cred) => cred.credentialID === claimedCred.id);

    if (!storedCred) {
      throw 'Authenticating credential not found.';
    }

    const base64PublicKey = base64url.toBuffer(storedCred.credentialPublicKey);
    const base64CredentialID = base64url.toBuffer(storedCred.credentialID);
    const { counter, transports } = storedCred; 

    const authenticator: AuthenticatorDevice = {
      credentialPublicKey: base64PublicKey,
      credentialID: base64CredentialID,
      counter,
      transports
    }

    const verification = verifyAssertionResponse({
      credential: claimedCred,
      expectedChallenge,
      expectedOrigin,
      expectedRPID,
      authenticator,
    });

    const { verified, assertionInfo } = verification;

    if (!verified) {
      throw 'User verification failed.';
    }

    storedCred.counter = assertionInfo.newCounter;
    storedCred.last_used = getNow();

    await UserManager.saveUser(user);

    delete req.session.auth;
    req.session['user_id'] = user.id;
    req.session['last_signin'] = getNow();
    res.json(user.credentials);
  } catch (e) {
    delete req.session.auth?.challenge;
    res.status(400).json({ status: false, error: e });
  }
});


export { router as webauthn };
