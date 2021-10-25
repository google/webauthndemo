import {
  getFirestore,
} from 'firebase-admin/firestore';

const db = getFirestore();

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
  transports?: AuthenticatorTransport[] // list of transports,
  last_used?: number // last used epoc time,
  clientExtensionResults?: any
}

export async function getCredentials(
  user_id: user_id
): Promise<StoredCredential[]> {
  const results: StoredCredential[] = [];
  const creds = await db.collection('credentials').where('user_id', '==', user_id).get();
  creds.forEach(cred => results.push(<StoredCredential>cred.data()));
  return results;
};

export async function getCredential(
  credential_id: credential_id
): Promise<StoredCredential> {
  const credRef = await db.collection('credentials').doc(credential_id).get();
  return <StoredCredential>credRef.data();
}

export function storeCredential(
  credential: StoredCredential
): Promise<FirebaseFirestore.WriteResult> {
  const credRef = db.collection('credentials').doc(credential.credentialID);
  return credRef.set(credential);
}

export function removeCredential(
  credential_id: credential_id
): Promise<FirebaseFirestore.WriteResult> {
  const credRef = db.collection('credentials').doc(credential_id);
  return credRef.delete();
}
