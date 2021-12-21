import {
  getFirestore,
} from 'firebase-admin/firestore';

const store = getFirestore();

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
  transports?: AuthenticatorTransport[] // list of transports,
  browser?: string
  os?: string
  platform?: string
  last_used?: number // last used epoc time,
  clientExtensionResults?: any
}

export async function getCredentials(
  user_id: user_id
): Promise<StoredCredential[]> {
  const results: StoredCredential[] = [];
  const refs = await store.collection('credentials')
    .where('user_id', '==', user_id)
    .orderBy('registered', 'desc').get();
  refs.forEach(cred => results.push(<StoredCredential>cred.data()));
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

export function removeCredential(
  credential_id: credential_id
): Promise<FirebaseFirestore.WriteResult> {
  const ref = store.collection('credentials').doc(credential_id);
  return ref.delete();
}
