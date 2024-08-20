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

import { store } from './config.mjs';
import {
  user_id,
  credential_id,
  StoredCredential,
} from '../public/scripts/common';

export async function getCredentials(
  user_id: user_id
): Promise<StoredCredential[]> {
  const results: StoredCredential[] = [];
  const refs = await store.collection('credentials')
    .where('user_id', '==', user_id)
    .orderBy('registered', 'desc').get();
  refs.forEach(cred => results.push(cred.data() as StoredCredential));
  return results;
};

export async function getCredential(
  credential_id: credential_id
): Promise<StoredCredential> {
  const doc = await store.collection('credentials').doc(credential_id).get();
  const credential = doc.data() as StoredCredential;
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
  const ref = store.collection('credentials').doc(credential_id);
  return ref.delete();
}
