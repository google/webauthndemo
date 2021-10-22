import { getFirestore } from 'firebase-admin/firestore';
import * as uid from 'uid-safe';

const db = getFirestore();

export type username = string;
export type password = string;
export type user_id = string;
export type device_id = string;
export type credential_id = string;
export type phone_number = string;

export interface User {
  id: user_id
  // User visible identifier.
  username: username
  password?: password
  name?: string
  picture_url?: string
  // email?: string
  email_verified?: boolean
  // `credentials` is an array that manages roaming authenticators.
  // This is used for "2FA with a security key".
  credentials: StoredCredential[]
  // A phone number that is already verified.
  verified_phone?: phone_number
  // A TOTP secret that is already verified.
  totp_secret?: string
  // The primary 2FA method of user's choice.
  tsv_method?: TSV_Method
}

export interface FIDUser {
  id: user_id
  subject: user_id
  issuer: string
  audience: string
  picture?: string
  email?: string
  issued_at?: number
  expires_on?: number
  associated_userid?: user_id
}

/**
 * Instead of using a credential ID directly, generate a random
 * unique ID per device and use it as the key to determine the
 * platform authenticator. This is used for "reauth".
 **/
// interface Device {
//   id: device_id // device id,
//   name: string // device name,
//   credentialId: credential_id // platform authenticator's credential id,
//   publicKey: string // public key,
//   registered: number // registered epoc time,
//   last_used: number // last used epoc time,
// }

/**
 * `credentials` is an array that manages roaming authenticators.
 * This is used for "2FA with security keys".
 **/
export interface StoredCredential extends Credential {
  deviceName?: string // authenticator name,
  credentialID: credential_id // roaming authenticator's credential id,
  credentialPublicKey: string // public key,
  counter: number // previous counter,
  aaguid?: string // AAGUID,
  registered?: number // registered epoc time,
  transports?: AuthenticatorTransport[] // list of transports,
  last_used?: number // last used epoc time,
}

type TSV_Method = 'none' | 'sms' | 'totp' | 'sk'

export class UserManager {
  public static async newUser(
    username: username,
    // email?: string,
    password?: password,
    credentials: StoredCredential[] = [],
    verified_phone?: phone_number,
    tsv_method: TSV_Method = 'none'
  ): Promise<User> {
    const id = await uid.default(32);
    const user: User = {
      username, id, password, credentials, verified_phone, tsv_method
    };
    await UserManager.saveUser(user);
    return UserManager.getUserById(id)
  }

  public static async saveUser(
    user: User
  ): Promise<FirebaseFirestore.WriteResult> {
    if (!user.id) throw 'User ID not available.';
    const docRef = db.collection('user').doc(user.id);
    return await docRef.set(user);
  }

  public static async getUserById(
    userId: user_id
  ): Promise<User> {
    const doc = await db.collection('user').doc(userId).get();
    if (doc.exists) {
      // @ts-ignore
      return <User>doc.data();
    } else {
      throw 'User ID not found.';
    }
  }

  public static async getUserByUsername(
    username: username
  ): Promise<User> {
    const users = await db.collection('user').where('username', '==', username).get();
    if (users.size > 0) {
      return <User>users.docs[0].data();
    } else {
      throw 'Username not found.';
    }
  }

  public static async getUserByEmail(
    email: string
  ): Promise<User> {
    const users = await db.collection('user').where('username', '==', email).get();
    if (users.size > 0) {
      return <User>users.docs[0].data();
    } else {
      throw 'Username not found.';
    }
  }

  public static async overwritePassword(
    user: User,
    newPassword: password
  ): Promise<User> {
    // TODO: Hash the password
    user.password =  newPassword;

    await UserManager.saveUser(user);
    return user;
  }

  public static async hashAndStorePassword(
    passwd: password
  ) {
    // const salt = bcrypt.genSaltSync(10);
    // const hashed = bcrypt.hashSync(passwd, salt);
    // const seed = await db.getById(this.id);
    // seed.password = hashed;
    // await db.save(this.id, seed);
  }

  public static verifyPassword(
    passwd: password,
    hashed: password
  ) {
    // return bcrypt.compareSync(passwd, hashed);
    return true
  }

  public static async signUp(
    username: username,
    password: password
  ) {
    let user = await UserManager.getUserByUsername(username);

    // If user entry is not created yet, create one
    if (!user) {
      user = await UserManager.newUser(
        username,
        password  // TODO: Hash password
      );
    } else {
      // TODO: Overwrite the password and save
      // TODO: Validate the password
      user.password  = password;
      UserManager.saveUser(user);
    }
    return user;
  }

  public static async signIn(
    username: username,
    password: password
  ): Promise<User | undefined> {
    try {
      let user = await UserManager.getUserByUsername(username);
      // If user is not found or the password doesn't match, fail.
      if (!user || user.password !== password) {
        throw 'Authentication failed';
      }
      return user;
    } catch (e) {
      console.error(e);
      return undefined;
    }
  }
}

// export class FIDManager {
//   public static async newUser(
//     subject: user_id,
//     issuer: string,
//     audience: string,
//     email: string = '',
//     picture: string = '',
//     issued_at?: number,
//     expires_on?: number,
//     associated_userid?: user_id,
//   ): Promise<FIDUser> {
//     const id = await uid.default(32);
//     const user: FIDUser = { id, subject, issuer, audience, picture,
//       email, issued_at, expires_on, associated_userid };
//     await FIDManager.saveUser(user);
//     return FIDManager.getUserBySubject(subject);
//   }

//   public static saveUser(
//     user: FIDUser
//   ): Promise<CommitResponse> {
//     if (!user.subject) throw '[FIDManager] Subject not available.';
//     return fiddb.save(user, user.id);
//   }

//   public static getUserByAssociatedUserId(
//     user_id: user_id
//   ): Promise<FIDUser[]> {
//     return fiddb.filter('associated_userid', '=', user_id);
//   }

//   public static async getUserBySubject(
//     subject: user_id
//   ): Promise<FIDUser> {
//     const users = await fiddb.filter('subject', '=', subject);
//     // TODO: What if there are zero or multiple users with the same email address?
//     return users[0];
//   }

//   public static async getUserByEmail(
//     email: string
//   ): Promise<FIDUser> {
//     const users = await fiddb.filter('email', '=', email);
//     return users[0];
//   }
// }
