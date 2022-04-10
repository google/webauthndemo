import { Request, Response, NextFunction } from 'express';
import { UserInfo } from '../public/scripts/common';

const getNow = () => new Date().getTime();

/**
 * Checks CSRF protection using custom header `X-Requested-With`
 * If cookie doesn't contain `username`, consider the user is not authenticated.
 **/
const csrfCheck = (
  req: Request,
  res: Response,
  next: NextFunction
) => {
  if (req.header("X-Requested-With") != "XMLHttpRequest") {
    res.status(400).json({ error: "invalid access." });
    return;
  }
  next();
};

/**
 * Middleware that checks authorization status required to access this API.
 * Rejects if insufficient.
 * @param {number} requirement The authorization requirement to access this API.
 * @returns 
 */
const authzAPI = async (
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> => {
  const { user_id, name, displayName, picture } = req.session;

  if (process.env.NODE_ENV !== 'production') {
    console.log('Session:', req.session);
  }
  if (!user_id) {
    // When a non-signed-in user is trying to access.
    res.status(401).json({ error: 'User not signed in.' });
    return;
  }

  res.locals.user = {user_id, name, displayName, picture } as UserInfo;
  if (process.env.NODE_ENV !== 'production') {
    console.log('User:', res.locals.user);
  }
  next();
};

export { csrfCheck, authzAPI, getNow };
