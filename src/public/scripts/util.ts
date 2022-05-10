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
