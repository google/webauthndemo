import '@material/mwc-top-app-bar-fixed';
import '@material/mwc-button';
import '@material/mwc-checkbox';
import '@material/mwc-dialog';
import '@material/mwc-drawer';
import '@material/mwc-linear-progress';
import '@material/mwc-list';
import '@material/mwc-radio';
import '@material/mwc-snackbar';
import '@material/mwc-select';
import '@material/mwc-switch';
import '@material/mwc-formfield';
import '@material/mwc-icon-button';
import '@material/mwc-textfield';
import { Drawer } from '@material/mwc-drawer';
import { Snackbar } from '@material/mwc-snackbar';
import { LinearProgress } from '@material/mwc-linear-progress';
import { html, render } from 'lit';

const $ = document.querySelector.bind(document);

const menu = $('#menu');
const drawer = $('#drawer') as Drawer;
const snackbar = $('#snackbar') as Snackbar;

if (menu && drawer) {
  menu.addEventListener('click', () => {
    drawer.open = !drawer.open;
  });
}

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

const _fetch = async (path, payload = '') => {
  const headers = {
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
    throw result.error;
  }
};

export { html, render, $, showSnackbar, loading, _fetch };
