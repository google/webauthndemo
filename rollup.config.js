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

import fs from 'fs';
import path from 'path';
import url from 'url';
import typescript from '@rollup/plugin-typescript';
import commonjs from '@rollup/plugin-commonjs';
import nodeResolve from '@rollup/plugin-node-resolve';
import json from '@rollup/plugin-json';
import scss from 'rollup-plugin-scss';

const __dirname = url.fileURLToPath(new URL('.', import.meta.url));

const serverSrc = path.join(__dirname, 'src');
const clientSrc = path.join(__dirname, 'src', 'public');
const dstRoot = path.join(__dirname, 'dist');
const clientDst = path.join(dstRoot, 'public');

function copyStaticAssets(envFile) {
  return {
    name: 'copy-static-assets',
    writeBundle() {
      fs.mkdirSync(clientDst, { recursive: true });
      fs.cpSync(
        path.join(__dirname, 'firebase.json'),
        path.join(dstRoot, 'firebase.json')
      );
      for (const entry of fs.readdirSync(clientSrc)) {
        if (entry.endsWith('.svg')) {
          fs.cpSync(
            path.join(clientSrc, entry),
            path.join(clientDst, entry)
          );
        }
      }
      fs.cpSync(
        path.join(serverSrc, 'templates'),
        path.join(dstRoot, 'templates'),
        { recursive: true }
      );
      const envSrc = path.join(serverSrc, envFile);
      if (fs.existsSync(envSrc)) {
        fs.cpSync(envSrc, path.join(dstRoot, '.env'));
      }
    }
  };
}

export default () => {
  const sourcemap = process.env.NODE_ENV != 'production' ? 'inline' : false;
  const env = process.env.NODE_ENV != 'production' ? '.env.development' : '.env';

  const plugins = [
    typescript({
      sourceMap: true,
      inlineSources: true,
      tsconfig: path.join(clientSrc, 'tsconfig.json'),
      compilerOptions: {
        outDir: path.join(clientDst, 'scripts'),
      }
    }),
    commonjs({ extensions: ['.js', '.ts', '.mts'] }),
    nodeResolve({
      browser: true,
      preferBuiltins: false
    }),
    json(),
  ];

  const files = [ 'components' ];
  const config = files.map(fileName => {
    return {
      input: path.join(clientSrc, 'scripts', `${fileName}.ts`),
      output: {
        file: path.join(clientDst, 'scripts', `${fileName}.js`),
        format: 'es',
        sourcemap,
      },
      plugins
    };
  });
  return [ ...config, {
    input: path.join(clientSrc, 'scripts', 'main.ts'),
    output: {
      file: path.join(clientDst, 'scripts', 'main.js'),
      format: 'es',
      sourcemap,
    },
    plugins: [
      ...plugins,
      copyStaticAssets(env),
    ]
  }, {
    input: path.join(clientSrc, 'styles', 'style.js'),
    output: {
      file: path.join(clientDst, 'styles', 'style.js'),
      format: 'esm',
      assetFileNames: '[name][extname]',
    },
    plugins: [
      scss({
        includePaths: [
          path.join(__dirname, 'node_modules'),
          path.join(__dirname, '..', '..', 'node_modules'),
        ],
        name: 'style.css',
        outputStyle: 'compressed',
        quietDeps: true,
        silenceDeprecations: ['legacy-js-api'],
      }),
      nodeResolve({
        browser: true,
        preferBuiltins: false
      }),
    ]
  }];
};

