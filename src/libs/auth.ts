import express, { Request, Response } from 'express';
import { initializeApp } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import { cert } from 'firebase-admin/app';
import ServiceAccount from '../service-account.json';

// if (process.env.IS_LOCALHOST) {
//   initializeApp();
// } else {
  // @ts-ignore
  initializeApp({ credential: cert(ServiceAccount) });
// }

const auth = getAuth();
const router = express.Router();

router.post('/verify', async (req: Request, res: Response) => {
  const { id_token } = req.body;

  try {
    const result = await auth.verifyIdToken(<string>id_token);
    if (result) {
      console.log(result);
      req.session.user_id = result.user_id;
      req.session.name = result.name;
      res.json({
        status: true,
        message: 'Successfully signed in.'
      });
    } else {
      throw 'Verification failed.';
    }
  } catch (e) {
    console.error(e);
    res.status(400).json({
      status: false,
      message: 'Verification failed.'
    });
    return;
  }
});

router.post('/signout', (req: Request, res: Response) => {
  delete req.session.user_id;
  delete req.session.name;
  res.json({
    status: true,
    message: 'Successfully signed out.'
  });
});

export { router as auth };
