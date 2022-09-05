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

import { PublicKeyCredentialCreationOptionsJSON, PublicKeyCredentialRequestOptionsJSON }
  from '@simplewebauthn/typescript-types';
import { AuthenticatorTransportFuture } from '@simplewebauthn/typescript-types';

export interface UserInfo {
  user_id: string
  name: string
  displayName: string
  picture: string
}

export interface WebAuthnRegistrationObject extends
  Omit<PublicKeyCredentialCreationOptionsJSON, 'rp' | 'pubKeyCredParams' | 'challenge' | 'excludeCredentials'> {
  credentialsToExclude?: string[]
  customTimeout?: number
  abortTimeout?: number
}

export interface WebAuthnAuthenticationObject extends Omit<PublicKeyCredentialRequestOptionsJSON, 'challenge'> {
  customTimeout?: number
  abortTimeout?: number
}

type AttestationFormat =
  | 'fido-u2f'
  | 'packed'
  | 'android-safetynet'
  | 'android-key'
  | 'tpm'
  | 'apple'
  | 'none';

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
  dpks?: StoredDevicePublicKey[] // Device Public Key,
}

export type EncodedDevicePublicKey = {
  aaguid: string;
  dpk: string;
  scope: string;
  nonce?: string;
  fmt?: AttestationFormat;
  attStmt?: {
    sig?: string;
    x5c?: string[];
    response?: string;
    alg?: number;
    ver?: string;
    certInfo?: string;
    pubArea?: string;
  };
  sig?: string;
}

export interface StoredDevicePublicKey extends EncodedDevicePublicKey {
  credentialID: credential_id
}
