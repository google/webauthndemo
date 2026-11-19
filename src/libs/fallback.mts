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

import type { Request, Response } from 'express';
import type { ExtendedAuthenticationExtensionsClientInputs } from '../public/scripts/common';

export const MAX_FALLBACK_QUERY_PARAMS = 50;
export const MAX_FALLBACK_PARAM_LENGTH = 512;

export const FALLBACK_CSP =
  "default-src 'none'; style-src 'self'; img-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'";

export interface FallbackQueryParam {
  key: string;
  value: string;
  truncated: boolean;
}

export interface NormalizedFallbackQuery {
  params: FallbackQueryParam[];
  totalCount: number;
  omittedCount: number;
}

export interface FallbackViewModel {
  origin: string;
  hostname: string;
  path: string;
  fallbackUrl: string;
  timestamp: string;
  queryParams: FallbackQueryParam[];
  queryParamCount: number;
  hasQueryParams: boolean;
  queryParamsTruncated: boolean;
  omittedQueryParamCount: number;
  isSignedIn: boolean;
  clientEnvironment?: string;
}

const FORBIDDEN_KEYS = new Set(['__proto__', 'constructor', 'prototype']);

function truncateValue(raw: string): { text: string; truncated: boolean } {
  if (raw.length <= MAX_FALLBACK_PARAM_LENGTH) {
    return { text: raw, truncated: false };
  }
  return {
    text: `${raw.slice(0, MAX_FALLBACK_PARAM_LENGTH)}...`,
    truncated: true,
  };
}

function stringifyQueryValue(value: unknown): string {
  if (typeof value === 'string') {
    return value;
  }
  if (value === null || value === undefined) {
    return '';
  }
  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
}

/**
 * Normalizes Express `req.query` into a bounded, display-safe list of
 * key/value pairs for rendering on the `/fallback` page.
 */
export function normalizeFallbackQuery(
  query: Record<string, unknown> | undefined
): NormalizedFallbackQuery {
  if (!query || typeof query !== 'object') {
    return { params: [], totalCount: 0, omittedCount: 0 };
  }

  const params: FallbackQueryParam[] = [];
  let totalCount = 0;

  for (const [rawKey, rawValue] of Object.entries(query)) {
    if (FORBIDDEN_KEYS.has(rawKey)) {
      continue;
    }
    const values = Array.isArray(rawValue) ? rawValue : [rawValue];
    for (const item of values) {
      totalCount += 1;
      if (params.length >= MAX_FALLBACK_QUERY_PARAMS) {
        continue;
      }
      const keyResult = truncateValue(rawKey);
      const valResult = truncateValue(stringifyQueryValue(item));
      params.push({
        key: keyResult.text,
        value: valResult.text,
        truncated: keyResult.truncated || valResult.truncated,
      });
    }
  }

  return {
    params,
    totalCount,
    omittedCount: Math.max(0, totalCount - params.length),
  };
}

/**
 * Sets privacy and security headers recommended for cross-device fallback
 * landing pages (per RFC 10027 / W3C WebAuthn PR #2380).
 */
export function applyFallbackSecurityHeaders(res: Response): void {
  res.setHeader('Cache-Control', 'no-store');
  res.setHeader('Pragma', 'no-cache');
  res.setHeader('Referrer-Policy', 'no-referrer');
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'DENY');
  res.setHeader('Content-Security-Policy', FALLBACK_CSP);
}

/**
 * Builds the Handlebars view model for `GET /fallback`.
 * Uses configured origin rather than the `Host` header to avoid host-header injection.
 */
export function buildFallbackViewModel(
  req: Pick<Request, 'path' | 'query' | 'session' | 'useragent'>,
  origin: string,
  hostname: string
): FallbackViewModel {
  const normalizedPath = req.path || '/fallback';
  const normalizedOrigin = origin.replace(/\/+$/, '');
  const { params, totalCount, omittedCount } = normalizeFallbackQuery(
    req.query as Record<string, unknown>
  );

  const browser =
    req.useragent?.browser && req.useragent.browser !== 'unknown'
      ? req.useragent.browser
      : undefined;
  const os =
    req.useragent?.os && req.useragent.os !== 'unknown'
      ? req.useragent.os
      : undefined;
  const platform =
    req.useragent?.platform && req.useragent.platform !== 'unknown'
      ? req.useragent.platform
      : undefined;

  const clientParts = [browser, os, platform].filter(
    (part): part is string => Boolean(part)
  );
  const clientEnvironment =
    clientParts.length > 0 ? clientParts.join(' / ') : undefined;

  return {
    origin: normalizedOrigin,
    hostname,
    path: normalizedPath,
    fallbackUrl: `${normalizedOrigin}${normalizedPath}`,
    timestamp: new Date().toISOString(),
    queryParams: params,
    queryParamCount: totalCount,
    hasQueryParams: params.length > 0,
    queryParamsTruncated: omittedCount > 0,
    omittedQueryParamCount: omittedCount,
    isSignedIn: Boolean(req.session?.user_id),
    clientEnvironment,
  };
}

/**
 * Sanitizes authentication `extensions` received by `POST /webauthn/authRequest`.
 *
 * - Strips prototype-polluting keys (`__proto__`, `constructor`, `prototype`).
 * - Trims `crossDeviceFallbackUrl` and omits it when empty or non-string so the
 *   browser does not receive an empty string that triggers URL parsing errors.
 * - Intentionally does NOT enforce HTTPS / same-site RP ID restrictions here:
 *   this demo serves as a test harness so clients can verify `SecurityError`
 *   validation behavior on invalid URLs per W3C WebAuthn PR #2380.
 */
export function sanitizeAuthenticationExtensions(
  raw: unknown
): ExtendedAuthenticationExtensionsClientInputs {
  const sanitized: ExtendedAuthenticationExtensionsClientInputs = {};
  if (!raw || typeof raw !== 'object' || Array.isArray(raw)) {
    return sanitized;
  }

  for (const [key, value] of Object.entries(raw)) {
    if (FORBIDDEN_KEYS.has(key) || value === undefined) {
      continue;
    }
    if (key === 'crossDeviceFallbackUrl') {
      if (typeof value === 'string') {
        const trimmed = value.trim();
        if (trimmed.length > 0) {
          sanitized.crossDeviceFallbackUrl = trimmed;
        }
      }
      continue;
    }
    if (key === 'txAuthSimple') {
      if (typeof value === 'string') {
        const trimmed = value.trim();
        if (trimmed.length > 0) {
          sanitized.txAuthSimple = trimmed;
        }
      }
      continue;
    }
    (sanitized as Record<string, unknown>)[key] = value;
  }

  return sanitized;
}
