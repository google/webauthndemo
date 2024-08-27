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

import { config } from './config.mjs';
import express, { Request, Response } from 'express';
import {
  WebAuthnRegistrationObject,
  WebAuthnAuthenticationObject,
  AAGUIDs,
  AAGUID,
} from '../public/scripts/common';
import { createHash } from 'crypto';
import { getNow, csrfCheck, authzAPI } from './helper.mjs';
import { 
  getCredentials,
  removeCredential,
  storeCredential,
} from './credential.mjs';
import {
  generateAuthenticationOptions,
  generateRegistrationOptions,
  verifyAuthenticationResponse,
  verifyRegistrationResponse,
} from '@simplewebauthn/server';
import {isoBase64URL} from '@simplewebauthn/server/helpers';
import {
  AuthenticationResponseJSON,
  RegistrationResponseJSON,
  AuthenticatorSelectionCriteria,
  AuthenticatorDevice,
  AttestationConveyancePreference,
  PublicKeyCredentialParameters,
  PublicKeyCredentialUserEntityJSON,
} from '@simplewebauthn/types';

import aaguids from 'aaguid' with { type: 'json' };

const router = express.Router();

const RP_NAME = process.env.PROJECT_NAME || 'WebAuthn Demo';
const WEBAUTHN_TIMEOUT = 1000 * 60 * 5; // 5 minutes

export const getOrigin = (
  _origin: string,
  userAgent?: string
): string => {
  let origin = _origin;
  if (!userAgent) return origin;

  const appRe = /^[a-zA-z0-9_.]+/;
  const match = userAgent.match(appRe);
  if (match) {
    // Check if UserAgent comes from a supported Android app.
    if (process.env.ANDROID_PACKAGENAME && process.env.ANDROID_SHA256HASH) {
      const package_names = process.env.ANDROID_PACKAGENAME.split(",").map(name => name.trim());
      const hashes = process.env.ANDROID_SHA256HASH.split(",").map(hash => hash.trim());
      const appName = match[0];
      for (let i = 0; i < package_names.length; i++) {
        if (appName === package_names[i]) {
          // We recognize this app, so use the corresponding hash.
          const octArray = hashes[i].split(':').map((h) =>
            parseInt(h, 16),
          );
          // @ts-ignore
          const androidHash = isoBase64URL.fromBuffer(octArray);
          origin = `android:apk-key-hash:${androidHash}`;
          break;
        }
      }
    }
  }
  
  return origin;
}

router.get('/aaguids', (req: Request, res: Response): Response<AAGUID | AAGUIDs> => {
  if (Object.keys(aaguids).length === 0) {
    return res.json();
  }
  if (req.query.id) {
    const id = req.query.id as string;
    if (Object.keys(aaguids).indexOf(id) > -1) {
      return res.json((aaguids as AAGUIDs)[id] as AAGUID);
    }
    return res.json({
      name: 'Unknown',
    } as AAGUID);
  }
  return res.json(aaguids);
});

/**
 * Returns a list of credentials
 **/
router.post('/getCredentials', csrfCheck, authzAPI, async (
  req: Request,
  res: Response
): Promise<any> => {
  if (!res.locals.user) throw 'Unauthorized.';

  const user = res.locals.user;

  try {
    const credentials = await getCredentials(user.user_id);
    return res.json(credentials);
  } catch (error) {
    console.error(error);
    return res.status(401).json({
      status: false,
      error: 'Unauthorized'
    });
  }
});

/**
 * Removes a credential id attached to the user
 * Responds with empty JSON `{}`
 **/
router.post('/removeCredential', csrfCheck, authzAPI, async (
  req: Request,
  res: Response
): Promise<any> => {
  if (!res.locals.user) throw 'Unauthorized.';

  const { credId } = req.body;

  try {
    await removeCredential(credId);
    return res.json({
      status: true
    });
  } catch (error) {
    console.error(error);
    return res.status(400).json({
      status: false
    });
  }
});

router.post('/registerRequest', csrfCheck, authzAPI, async (
  req: Request,
  res: Response
): Promise<any> => {
  try {
    if (!res.locals.user) throw new Error('Unauthorized.');

    const googleUser = res.locals.user;
    const creationOptions = req.body as WebAuthnRegistrationObject || {};

    // const excludeCredentials: PublicKeyCredentialDescriptor[] = [];
    // if (creationOptions.credentialsToExclude) {
    //   const credentials = await getCredentials(googleUser.user_id);
    //   if (credentials.length > 0) {
    //     for (let cred of credentials) {
    //       if (creationOptions.credentialsToExclude.includes(`ID-${cred.credentialID}`)) {
    //         excludeCredentials.push({
    //           id: base64url.toBuffer(cred.credentialID),
    //           type: 'public-key',
    //           transports: cred.transports,
    //         });
    //       }
    //     }
    //   }
    // }
    const pubKeyCredParams: PublicKeyCredentialParameters[] = [];
    // const params = [-7, -35, -36, -257, -258, -259, -37, -38, -39, -8];
    const params = [-7, -257];
    for (let param of params) {
      pubKeyCredParams.push({ type: 'public-key', alg: param });
    }
    const authenticatorSelection: AuthenticatorSelectionCriteria = {};
    const aa = creationOptions.authenticatorSelection?.authenticatorAttachment;
    const rk = creationOptions.authenticatorSelection?.residentKey;
    const uv = creationOptions.authenticatorSelection?.userVerification;
    const cp = creationOptions.attestation; // attestationConveyancePreference
    let attestation: AttestationConveyancePreference = 'none';

    if (aa === 'platform' || aa === 'cross-platform') {
      authenticatorSelection.authenticatorAttachment = aa;
    }
    const enrollmentType = aa || 'undefined';
    if (rk === 'required' || rk === 'preferred' || rk === 'discouraged') {
      authenticatorSelection.residentKey = rk;
    }
    if (uv === 'required' || uv === 'preferred' || uv === 'discouraged') {
      authenticatorSelection.userVerification = uv;
    }
    if (cp === 'none' || cp === 'indirect' || cp === 'direct' || cp === 'enterprise') {
      attestation = cp;
    }

    const encoder = new TextEncoder();
    const name = creationOptions.user?.name || googleUser.name || 'Unnamed User';
    const displayName = creationOptions.user?.displayName || googleUser.displayName || 'Unnamed User';
    const data = encoder.encode(`${name}${displayName}`)
    const userId = createHash('sha256').update(data).digest();

    const user = {
      id: isoBase64URL.fromBuffer(Buffer.from(userId)),
      name,
      displayName
    } as PublicKeyCredentialUserEntityJSON

    // TODO: Validate
    const extensions = creationOptions.extensions;
    const timeout = creationOptions.customTimeout || WEBAUTHN_TIMEOUT;

    const options = await generateRegistrationOptions({
      rpName: RP_NAME,
      rpID: config.hostname,
      userID: userId,
      userName: user.name,
      userDisplayName: user.displayName,
      timeout,
      // Prompt users for additional information about the authenticator.
      attestationType: attestation,
      // Prevent users from re-registering existing authenticators
      // excludeCredentials,
      authenticatorSelection,
      extensions,
    });

    req.session.challenge = options.challenge;
    req.session.timeout = getNow() + WEBAUTHN_TIMEOUT;
    req.session.type = enrollmentType;

    return res.json(options);
  } catch (error: any) {
    console.error(error);
    return res.status(400).send({ status: false, error: error.message });
  }
});

router.post('/registerResponse', csrfCheck, authzAPI, async (
  req: Request,
  res: Response
): Promise<any> => {
  try {
    if (!res.locals.user) throw new Error('Unauthorized.');
    if (!req.session.challenge) throw new Error('No challenge found.');

    const user = res.locals.user;
    const credential = req.body as RegistrationResponseJSON;

    const expectedChallenge = req.session.challenge;
    const expectedRPID = config.hostname;
 
    let expectedOrigin = getOrigin(config.origin, req.get('User-Agent'));

    const verification = await verifyRegistrationResponse({
      response: credential,
      expectedChallenge,
      expectedOrigin,
      expectedRPID,
      // Since this is testing the client, verifying the UV flag here doesn't matter.
      requireUserVerification: false,
    });

    const { verified, registrationInfo } = verification;

    if (!verified || !registrationInfo) {
      throw new Error('User verification failed.');
    }

    const {
      aaguid,
      credentialPublicKey,
      credentialID,
      counter,
      credentialDeviceType,
      credentialBackedUp,
    } = registrationInfo;
    const base64PublicKey = isoBase64URL.fromBuffer(credentialPublicKey);
    const { response, clientExtensionResults } = credential;
    const transports = response.transports || [];

    await storeCredential({
      user_id: user.user_id,
      credentialID,
      credentialPublicKey: base64PublicKey,
      aaguid,
      counter,
      registered: getNow(),
      user_verifying: registrationInfo.userVerified,
      authenticatorAttachment: req.session.type || "undefined",
      credentialDeviceType,
      credentialBackedUp,
      browser: req.useragent?.browser,
      os: req.useragent?.os,
      platform: req.useragent?.platform,
      transports,
      clientExtensionResults,
    });

    delete req.session.challenge;
    delete req.session.timeout;
    delete req.session.type;

    // Respond with user info
    return res.json(credential);
  } catch (error: any) {
    console.error(error);

    delete req.session.challenge;
    delete req.session.timeout;
    delete req.session.type;

    return res.status(400).send({ status: false, error: error.message });
  }
});

router.post('/authRequest', csrfCheck, authzAPI, async (
  req: Request,
  res: Response
): Promise<any> => {
  if (!res.locals.user) throw new Error('Unauthorized.');

  try {
    // const user = res.locals.user;

    const requestOptions = req.body as WebAuthnAuthenticationObject;

    const userVerification = requestOptions.userVerification || 'preferred';
    const timeout = requestOptions.customTimeout || WEBAUTHN_TIMEOUT;
    // const allowCredentials: PublicKeyCredentialDescriptor[] = [];
    const extensions = requestOptions.extensions || {};
    const rpID = config.hostname;

    // // If `.allowCredentials` is not defined, leave `allowCredentials` an empty array.
    // if (requestOptions.allowCredentials) {
    //   const credentials = await getCredentials(user.user_id);
    //   for (let cred of credentials) {
    //     // Find the credential in the list of allowed credentials.
    //     const _cred = requestOptions.allowCredentials.find(_cred => {
    //       return _cred.id == cred.credentialID;
    //     });
    //     // If the credential is found, add it to the list of allowed credentials.
    //     if (_cred) {
    //       allowCredentials.push({
    //         id: base64url.toBuffer(_cred.id),
    //         type: 'public-key',
    //         transports: _cred.transports
    //       });
    //     }
    //   }
    // }

    const options = await generateAuthenticationOptions({
      timeout,
      // allowCredentials,
      userVerification,
      rpID,
      extensions,
    });

    req.session.challenge = options.challenge;
    req.session.timeout = getNow() + WEBAUTHN_TIMEOUT;

    return res.json(options);
  } catch (error: any) {
    console.error(error);

    return res.status(400).json({ status: false, error: error.message });
  }
});

router.post('/authResponse', csrfCheck, authzAPI, async (
  req: Request,
  res: Response
): Promise<any> => {
  if (!res.locals.user) throw new Error('Unauthorized.');

  const user = res.locals.user;
  const expectedChallenge = req.session.challenge || '';
  const expectedRPID = config.hostname;
  const expectedOrigin = getOrigin(config.origin, req.get('User-Agent'));

  try {
    const claimedCred = req.body as AuthenticationResponseJSON;

    const credentials = await getCredentials(user.user_id);
    let storedCred = credentials.find((cred) => cred.credentialID === claimedCred.id);

    if (!storedCred) {
      throw new Error('Authenticating credential not found.');
    }

    const credentialPublicKey = isoBase64URL.toBuffer(storedCred.credentialPublicKey);
    const { counter, transports } = storedCred;

    const authenticator: AuthenticatorDevice = {
      credentialPublicKey,
      credentialID: storedCred.credentialID,
      counter,
      transports
    }

    console.log('Claimed credential', claimedCred);
    console.log('Stored credential', storedCred);

    const verification = await verifyAuthenticationResponse({
      response: claimedCred,
      expectedChallenge,
      expectedOrigin,
      expectedRPID,
      authenticator,
      // Since this is testing the client, verifying the UV flag here doesn't matter.
      requireUserVerification: false,
    });

    const { verified, authenticationInfo } = verification;

    if (!verified) {
      throw new Error('User verification failed.');
    }

    storedCred.counter = authenticationInfo.newCounter;
    storedCred.last_used = getNow();

    delete req.session.challenge;
    delete req.session.timeout;
    return res.json(storedCred);
  } catch (error: any) {
    console.error(error);

    delete req.session.challenge;
    delete req.session.timeout;
    return res.status(400).json({ status: false, error: error.message });
  }
});

export { router as webauthn };
