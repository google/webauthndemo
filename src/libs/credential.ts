/*
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

import { getFirestore } from 'firebase-admin/firestore';
// import { DevicePublicKeyAuthenticatorOutput } from '@simplewebauthn/server/dist';
import { DevicePublicKeyAuthenticatorOutput } from '../public/scripts/common';
import {
  user_id,
  credential_id,
  StoredCredential,
  StoredDevicePublicKey,
  EncodedDevicePublicKey
} from '../public/scripts/common';
import base64url from 'base64url';

const store = getFirestore();
store.settings({ ignoreUndefinedProperties: true });

export async function getCredentials(
  user_id: user_id
): Promise<StoredCredential[]> {
  const results: StoredCredential[] = [];
  const refs = await store.collection('credentials')
    .where('user_id', '==', user_id)
    .orderBy('registered', 'desc').get();
  refs.forEach(cred => results.push(<StoredCredential>cred.data()));
  for (let cred of results) {
    cred.dpks = await getDevicePublicKeys(cred.credentialID);
  }
  return results;
};

export async function getCredential(
  credential_id: credential_id
): Promise<StoredCredential> {
  const doc = await store.collection('credentials').doc(credential_id).get();
  const credential = <StoredCredential>doc.data();
  credential.dpks = await getDevicePublicKeys(credential_id);
  return credential;
}

export function storeCredential(
  credential: StoredCredential
): Promise<FirebaseFirestore.WriteResult> {
  const ref = store.collection('credentials').doc(credential.credentialID);
  return ref.set(credential);
}

export async function removeCredential(
  credential_id: credential_id
): Promise<FirebaseFirestore.WriteResult> {
  const getDpks = await getDevicePublicKeys(credential_id);
  getDpks.forEach(item => {
    removeDevicePublicKey(item.dpk);
  });
  const ref = store.collection('credentials').doc(credential_id);
  return ref.delete();
}

export async function getDevicePublicKeys(
  credential_id: credential_id
): Promise<StoredDevicePublicKey[]> {
  const results: StoredDevicePublicKey[] = [];
  const refs = await store.collection('dpks')
    .where('credentialID', '==', credential_id)
    .get();
  refs.forEach(item => {
    results.push(<StoredDevicePublicKey>item.data())
  });
  return results;
}

export function removeDevicePublicKey(
  dpk: string
): Promise<FirebaseFirestore.WriteResult> {
  const ref = store.collection('dpks').doc(dpk);
  return ref.delete();
}

export function storeDevicePublicKey(
  credentialID: Buffer,
  devicePubKey: DevicePublicKeyAuthenticatorOutput
): Promise<FirebaseFirestore.WriteResult> {
  const base64CredentialID = base64url.encode(credentialID);
  const encodedDevicePubKey = encodeDevicePublicKey(devicePubKey);

  const _devicePubKey: StoredDevicePublicKey = {
    credentialID: base64CredentialID,
    ...encodedDevicePubKey
  }

  const ref = store.collection('dpks').doc(encodedDevicePubKey.dpk);
  return ref.set(_devicePubKey);
}

function encodeDevicePublicKey(
  devicePubKey: DevicePublicKeyAuthenticatorOutput
): EncodedDevicePublicKey {
  const base64Aaguid = base64url.encode(devicePubKey.aaguid);
  const base64Dpk = base64url.encode(devicePubKey.dpk);
  const base64Nonce = devicePubKey.nonce ? base64url.encode(devicePubKey.nonce) : undefined;

  const encodedDevicePubKey: EncodedDevicePublicKey = {
    aaguid: base64Aaguid,
    dpk: base64Dpk,
    scope: devicePubKey.scope,
    nonce: base64Nonce,
    fmt: devicePubKey.fmt || 'none',
  };

  encodedDevicePubKey.attStmt = {};

  if (devicePubKey.fmt !== 'none' && devicePubKey.attStmt) {
    const { attStmt } = devicePubKey;
    encodedDevicePubKey.attStmt.sig = attStmt.sig ? base64url.encode(attStmt.sig) : '';
    encodedDevicePubKey.attStmt.x5c = [];
    if (attStmt.x5c && attStmt.x5c.length > 0) {
      for (const x of attStmt.x5c) {
        encodedDevicePubKey.attStmt.x5c.push(base64url.encode(x));
      }
    }
    encodedDevicePubKey.attStmt.response = attStmt.response ? base64url.encode(attStmt.response) : '';
    encodedDevicePubKey.attStmt.certInfo = attStmt.certInfo ? base64url.encode(attStmt.certInfo) : '';
    encodedDevicePubKey.attStmt.pubArea = attStmt.pubArea ? base64url.encode(attStmt.pubArea) : '';
    encodedDevicePubKey.attStmt.alg = attStmt.alg;
    encodedDevicePubKey.attStmt.ver = attStmt.ver;
  }

  return encodedDevicePubKey;
}

export function decodeDevicePublicKey(
  encodedDevicePubKey: EncodedDevicePublicKey
): DevicePublicKeyAuthenticatorOutput {
  const aaguid = base64url.toBuffer(encodedDevicePubKey.aaguid);
  const dpk = base64url.toBuffer(encodedDevicePubKey.dpk);
  const scope = encodedDevicePubKey.scope;
  const nonce = encodedDevicePubKey.nonce ? base64url.toBuffer(encodedDevicePubKey.nonce) : Buffer.from('', 'hex');
  const fmt = encodedDevicePubKey.fmt ? encodedDevicePubKey.fmt : 'none';

  const decodedDevicePubKey: DevicePublicKeyAuthenticatorOutput = {
    aaguid,
    dpk,
    scope,
    nonce,
    fmt,
    attStmt: {}
  }

  if (encodedDevicePubKey.fmt !== 'none' && encodedDevicePubKey.attStmt) {
    const { attStmt: encodedAttStmt } = encodedDevicePubKey;
    decodedDevicePubKey.attStmt.sig = encodedAttStmt.sig ? base64url.toBuffer(encodedAttStmt.sig) : undefined;
    decodedDevicePubKey.attStmt.x5c = [];
    if (encodedAttStmt.x5c && encodedAttStmt.x5c.length > 0) {
      for (const x of encodedAttStmt.x5c) {
        decodedDevicePubKey.attStmt.x5c.push(base64url.toBuffer(x));
      }
    }
    decodedDevicePubKey.attStmt.response = encodedAttStmt.response ? base64url.toBuffer(encodedAttStmt.response) : undefined;
    decodedDevicePubKey.attStmt.certInfo = encodedAttStmt.certInfo ? base64url.toBuffer(encodedAttStmt.certInfo) : undefined;
    decodedDevicePubKey.attStmt.pubArea = encodedAttStmt.pubArea ? base64url.toBuffer(encodedAttStmt.pubArea) : undefined;
    decodedDevicePubKey.attStmt.alg = encodedAttStmt.alg;
    decodedDevicePubKey.attStmt.ver = encodedAttStmt.ver;
  }
  return decodedDevicePubKey;
}
