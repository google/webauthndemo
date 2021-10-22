import './libs/helper';

declare module 'express-session' {
  interface Session {
    // User ID and the indicator that the user is signed in.
    user_id?: string,
    name?: string,
    // Timestamp of the recent successful sign-in time.
    timeout?: number,
    // Enrollment session for the second step.
    challenge?: string
  }
}
