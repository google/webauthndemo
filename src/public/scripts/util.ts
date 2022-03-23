import { Snackbar } from '@material/mwc-snackbar';
import { LinearProgress } from '@material/mwc-linear-progress';
import { html, render } from 'lit';

const $: any = document.querySelector.bind(document);

const snackbar = $('#snackbar') as Snackbar;

function showSnackbar(message: string): void {
  snackbar.labelText = message;
  snackbar.show();
};

class Loading {
  private progress: LinearProgress

  constructor() {
    this.progress = $('#progress') as LinearProgress;
  }
  start() {
    this.progress.indeterminate = true;    
  }
  stop() {
    this.progress.indeterminate = false;    
  }
}

const loading = new Loading();

const _fetch = async (
  path: string,
  payload: any = ''
): Promise<any> => {
  const headers: any = {
    'X-Requested-With': 'XMLHttpRequest',
  };
  if (payload && !(payload instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(payload);
  }
  const res = await fetch(path, {
    method: 'POST',
    credentials: 'same-origin',
    headers: headers,
    body: payload,
  });
  if (res.status === 200) {
    // Server authentication succeeded
    return res.json();
  } else {
    // Server authentication failed
    const result = await res.json();
    throw new Error(result.error);
  }
};

export { html, render, $, showSnackbar, loading, _fetch };
