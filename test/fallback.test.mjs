/**
 * Copyright 2026 Google LLC
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

import test from 'node:test';
import assert from 'node:assert/strict';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import express from 'express';
import { engine } from 'express-handlebars';

import {
  FALLBACK_CSP,
  MAX_FALLBACK_PARAM_LENGTH,
  MAX_FALLBACK_QUERY_PARAMS,
  applyFallbackSecurityHeaders,
  buildFallbackViewModel,
  normalizeFallbackQuery,
  sanitizeAuthenticationExtensions,
} from '../dist/libs/fallback.mjs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const templatesDir = path.join(__dirname, '..', 'dist', 'templates');

test('normalizeFallbackQuery handles empty, single, repeated, and forbidden keys', () => {
  assert.deepEqual(normalizeFallbackQuery(undefined), {
    params: [],
    totalCount: 0,
    omittedCount: 0,
  });

  const query = Object.assign(Object.create(null), {
    sessionId: 'abc-123',
    tag: ['one', 'two'],
    __proto__: 'ignored',
    constructor: 'ignored',
    prototype: 'ignored',
  });
  const result = normalizeFallbackQuery(query);
  assert.equal(result.totalCount, 3);
  assert.equal(result.omittedCount, 0);
  assert.deepEqual(
    result.params.map((p) => [p.key, p.value, p.truncated]),
    [
      ['sessionId', 'abc-123', false],
      ['tag', 'one', false],
      ['tag', 'two', false],
    ]
  );
});

test('normalizeFallbackQuery truncates oversized values and caps parameter count', () => {
  const longVal = 'x'.repeat(MAX_FALLBACK_PARAM_LENGTH + 50);
  const longResult = normalizeFallbackQuery({ token: longVal });
  assert.equal(longResult.params.length, 1);
  assert.equal(longResult.params[0].truncated, true);
  assert.equal(longResult.params[0].value.length, MAX_FALLBACK_PARAM_LENGTH + 3);

  const many = {};
  for (let i = 0; i < MAX_FALLBACK_QUERY_PARAMS + 7; i++) {
    many[`k${i}`] = `v${i}`;
  }
  const capped = normalizeFallbackQuery(many);
  assert.equal(capped.params.length, MAX_FALLBACK_QUERY_PARAMS);
  assert.equal(capped.totalCount, MAX_FALLBACK_QUERY_PARAMS + 7);
  assert.equal(capped.omittedCount, 7);
});

test('buildFallbackViewModel uses configured origin and filters unknown useragent fields', () => {
  const vm = buildFallbackViewModel(
    {
      path: '/fallback',
      query: { sessionId: 'demo' },
      session: {},
      useragent: { browser: 'unknown', os: 'Android', platform: 'unknown' },
    },
    'https://try-webauthn.appspot.com/',
    'try-webauthn.appspot.com'
  );

  assert.equal(vm.origin, 'https://try-webauthn.appspot.com');
  assert.equal(vm.fallbackUrl, 'https://try-webauthn.appspot.com/fallback');
  assert.equal(vm.hostname, 'try-webauthn.appspot.com');
  assert.equal(vm.isSignedIn, false);
  assert.equal(vm.clientEnvironment, 'Android');
  assert.equal(vm.hasQueryParams, true);
  assert.equal(vm.queryParamCount, 1);
});

test('sanitizeAuthenticationExtensions handles crossDeviceFallbackUrl cleanly', () => {
  assert.deepEqual(sanitizeAuthenticationExtensions(undefined), {});
  assert.deepEqual(sanitizeAuthenticationExtensions(null), {});
  assert.deepEqual(sanitizeAuthenticationExtensions([]), {});

  // Omits empty or non-string values
  assert.deepEqual(
    sanitizeAuthenticationExtensions({
      crossDeviceFallbackUrl: '   ',
      txAuthSimple: '',
    }),
    {}
  );
  assert.deepEqual(
    sanitizeAuthenticationExtensions({
      crossDeviceFallbackUrl: 12345,
    }),
    {}
  );

  // Preserves valid and intentionally invalid URLs (for client SecurityError testing)
  assert.deepEqual(
    sanitizeAuthenticationExtensions({
      crossDeviceFallbackUrl: '  https://try-webauthn.appspot.com/fallback?sessionId=42  ',
      txAuthSimple: ' Approve login ',
      appid: 'https://try-webauthn.appspot.com',
    }),
    {
      crossDeviceFallbackUrl: 'https://try-webauthn.appspot.com/fallback?sessionId=42',
      txAuthSimple: 'Approve login',
      appid: 'https://try-webauthn.appspot.com',
    }
  );

  assert.deepEqual(
    sanitizeAuthenticationExtensions({
      crossDeviceFallbackUrl: 'http://evil.example/fallback',
    }),
    {
      crossDeviceFallbackUrl: 'http://evil.example/fallback',
    }
  );

  // Strips prototype-polluting keys
  const malicious = JSON.parse('{"__proto__":{"polluted":true},"constructor":"bad","prototype":"bad","crossDeviceFallbackUrl":"https://try-webauthn.appspot.com/fallback"}');
  const sanitized = sanitizeAuthenticationExtensions(malicious);
  assert.equal(Object.prototype.hasOwnProperty.call(sanitized, '__proto__'), false);
  assert.equal(Object.prototype.hasOwnProperty.call(sanitized, 'constructor'), false);
  assert.equal(Object.prototype.hasOwnProperty.call(sanitized, 'prototype'), false);
  assert.equal(sanitized.crossDeviceFallbackUrl, 'https://try-webauthn.appspot.com/fallback');
});

test('GET /fallback renders unauthenticated HTML with security headers and HTML escaping', async () => {
  const app = express();
  app.set('view engine', 'html');
  app.engine('html', engine({ extname: 'html' }));
  app.set('views', templatesDir);

  app.get('/fallback', (req, res) => {
    applyFallbackSecurityHeaders(res);
    const viewModel = buildFallbackViewModel(
      req,
      'https://try-webauthn.appspot.com',
      'try-webauthn.appspot.com'
    );
    return res.render('fallback.html', {
      layout: false,
      ...viewModel,
    });
  });

  const server = await new Promise((resolve) => {
    const s = app.listen(0, '127.0.0.1', () => resolve(s));
  });
  const { port } = server.address();
  const baseUrl = `http://127.0.0.1:${port}`;

  try {
    // 1. Plain /fallback
    const res1 = await fetch(`${baseUrl}/fallback`);
    assert.equal(res1.status, 200);
    assert.equal(res1.headers.get('cache-control'), 'no-store');
    assert.equal(res1.headers.get('referrer-policy'), 'no-referrer');
    assert.equal(res1.headers.get('x-content-type-options'), 'nosniff');
    assert.equal(res1.headers.get('x-frame-options'), 'DENY');
    assert.equal(res1.headers.get('content-security-policy'), FALLBACK_CSP);

    const body1 = await res1.text();
    assert.match(body1, /Cross-Device Fallback Authentication/);
    assert.match(body1, /No query parameters were included/);
    assert.doesNotMatch(body1, /\/scripts\/main\.js/);
    assert.doesNotMatch(body1, /unpkg\.com/);

    // 2. Trailing slash /fallback/
    const res2 = await fetch(`${baseUrl}/fallback/`);
    assert.equal(res2.status, 200);

    // 3. XSS escaping & repeated query params
    const xssUrl =
      `${baseUrl}/fallback?sessionId=sess_42&xss=` +
      encodeURIComponent('<script>alert(1)</script>') +
      '&dup=a&dup=b';
    const res3 = await fetch(xssUrl);
    assert.equal(res3.status, 200);
    const body3 = await res3.text();
    assert.match(body3, /sess_42/);
    assert.doesNotMatch(body3, /<script>alert\(1\)<\/script>/);
    assert.match(body3, /&lt;script&gt;alert\(1\)&lt;\/script&gt;/);
    assert.match(body3, /<code>dup<\/code>/);
  } finally {
    await new Promise((resolve) => server.close(resolve));
  }
});
