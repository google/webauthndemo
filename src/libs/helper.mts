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
): Promise<any> => {
  const { user_id, name, displayName, picture } = req.session;

  if (config.debug) {
    console.log('Session:', req.session);
  }
  if (!user_id) {
    // When a non-signed-in user is trying to access.
    return res.status(401).json({ error: 'User not signed in.' });
  }

  res.locals.user = {user_id, name, displayName, picture } as UserInfo;
  if (config.debug) {
    console.log('User:', res.locals.user);
  }
  return next();
};

export { csrfCheck, authzAPI, getNow };
