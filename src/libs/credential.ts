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
import { AuthenticatorTransportFuture } from '@simplewebauthn/typescript-types/';
import { DevicePublicKeyAuthenticatorOutput } from '@simplewebauthn/server/./dist';
import { AttestationFormat, AttestationStatement } from '@simplewebauthn/server/dist/helpers/decodeAttestationObject';
import base64url from 'base64url';

const store = getFirestore();
store.settings({ ignoreUndefinedProperties: true });

export type user_id = string;
export type credential_id = string;

export interface StoredCredential {
  user_id: user_id
  // User visible identifier.
  credentialID: credential_id // roaming authenticator's credential id,
  credentialPublicKey: string // public key,
  counter: number // previous counter,
  aaguid?: string // AAGUID,
  registered?: number // registered epoc time,
  user_verifying: boolean // user verifying authenticator,
  authenticatorAttachment: "platform" | "cross-platform" | "undefined" // authenticator attachment,
  transports?: AuthenticatorTransportFuture[] // list of transports,
  browser?: string
  os?: string
  platform?: string
  last_used?: number // last used epoc time,
  clientExtensionResults?: any
  dpks?: DevicePublicKeyAuthenticatorOutput[] // Device Public Key,
}

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
  return <StoredCredential>doc.data();
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
    const base64Dpk = base64url.encode(item.dpk);
    removeDevicePublicKey(base64Dpk);
  });
  const ref = store.collection('credentials').doc(credential_id);
  return ref.delete();
}

export interface StoredDevicePublicKey {
  credentialID: credential_id
  aaguid: string;
  dpk: string;
  scope: string;
  nonce?: string;
  fmt?: AttestationFormat;
  attStmt?: EncodedDevicePublicKeyAttestationStatement;
  sig?: string;
}

export async function getDevicePublicKeys(
  credential_id: credential_id
): Promise<DevicePublicKeyAuthenticatorOutput[]> {
  const results: DevicePublicKeyAuthenticatorOutput[] = [];
  const refs = await store.collection('dpks')
    .where('credentialID', '==', credential_id)
    .get();
  refs.forEach(item => {
    const storedDevicePublicKey = <StoredDevicePublicKey>(item.data());
    const aaguid = base64url.toBuffer(storedDevicePublicKey.aaguid);
    const dpk = base64url.toBuffer(storedDevicePublicKey.dpk);
    const scope = base64url.toBuffer(storedDevicePublicKey.scope);
    const nonce = storedDevicePublicKey.nonce ? base64url.toBuffer(storedDevicePublicKey.nonce) : undefined;
    const attStmt = decodeDevicePublicKeyAttStmt(storedDevicePublicKey.attStmt);
    const sig = storedDevicePublicKey.sig ? base64url.toBuffer(storedDevicePublicKey.sig) : undefined;
    const devicePubKey: DevicePublicKeyAuthenticatorOutput = {
      aaguid,
      dpk,
      scope,
      nonce,
      fmt: storedDevicePublicKey.fmt,
      attStmt,
      sig,
    }
    results.push(devicePubKey)
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
  const base64Aaguid = base64url.encode(devicePubKey.aaguid);
  const base64Dpk = base64url.encode(devicePubKey.dpk);
  const base64Scope = base64url.encode(devicePubKey.scope);
  const base64Nonce = devicePubKey.nonce ? base64url.encode(devicePubKey.nonce) : undefined;
  const base64AttStmt = encodeDevicePublicKeyAttStmt(devicePubKey.attStmt);
  const base64Sig = devicePubKey.sig ? base64url.encode(devicePubKey.sig) : undefined;

  const _devicePubKey: StoredDevicePublicKey = {
    credentialID: base64CredentialID,
    aaguid: base64Aaguid,
    dpk: base64Dpk,
    scope: base64Scope,
    nonce: base64Nonce,
    fmt: devicePubKey.fmt,
    attStmt: base64AttStmt,
    sig: base64Sig
  }

  const ref = store.collection('dpks').doc(base64Dpk);
  return ref.set(_devicePubKey);
}

function encodeDevicePublicKeyAttStmt(
  attStmt?: AttestationStatement
): EncodedDevicePublicKeyAttestationStatement | undefined {
  if (!attStmt) return undefined;

  const base64Sig = attStmt.sig ? base64url.encode(attStmt.sig) : '';
  const base64X5c = [];
  if (attStmt.x5c && attStmt.x5c.length > 0) {
    for (const x of attStmt.x5c) {
      base64X5c.push(base64url.encode(x));
    }
  }
  const base64Response = attStmt.response ? base64url.encode(attStmt.response) : '';
  const base64CertInfo = attStmt.certInfo ? base64url.encode(attStmt.certInfo) : '';
  const base64PubArea = attStmt.pubArea ? base64url.encode(attStmt.pubArea) : '';

  const encodedAttStmt: EncodedDevicePublicKeyAttestationStatement = {
    sig: base64Sig,
    x5c: base64X5c,
    response: base64Response,
    alg: attStmt.alg,
    ver: attStmt.ver,
    certInfo: base64CertInfo,
    pubArea: base64PubArea
  }
  return encodedAttStmt;
}

function decodeDevicePublicKeyAttStmt(
  storedAttStmt?: EncodedDevicePublicKeyAttestationStatement
): AttestationStatement | undefined {
  if (!storedAttStmt) return undefined;

  const sig = storedAttStmt.sig ? base64url.toBuffer(storedAttStmt.sig) : undefined;
  const x5c = [];
  if (storedAttStmt.x5c && storedAttStmt.x5c.length > 0) {
    for (const x of storedAttStmt.x5c) {
      x5c.push(base64url.toBuffer(x));
    }
  }
  const response = storedAttStmt.response ? base64url.toBuffer(storedAttStmt.response) : undefined;
  const certInfo = storedAttStmt.certInfo ? base64url.toBuffer(storedAttStmt.certInfo) : undefined;
  const pubArea = storedAttStmt.pubArea ? base64url.toBuffer(storedAttStmt.pubArea) : undefined;

  const attStmt: AttestationStatement = {
    sig,
    x5c,
    response,
    alg: storedAttStmt.alg,
    ver: storedAttStmt.ver,
    certInfo,
    pubArea,
  }

  return attStmt;
}

type EncodedDevicePublicKeyAttestationStatement = {
  sig?: string;
  x5c?: string[];
  response?: string;
  alg?: number;
  ver?: string;
  certInfo?: string;
  pubArea?: string;
}
