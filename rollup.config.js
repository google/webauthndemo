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

import path from 'path';
import url from 'url';
import typescript from '@rollup/plugin-typescript';
import commonjs from '@rollup/plugin-commonjs';
import nodeResolve from '@rollup/plugin-node-resolve';
import json from '@rollup/plugin-json';
import builtins from 'rollup-plugin-node-builtins';
import globals from 'rollup-plugin-node-globals';
import copy from 'rollup-plugin-copy';
import scss from 'rollup-plugin-scss';
import css from 'rollup-plugin-import-css';
import sourcemaps from 'rollup-plugin-sourcemaps';

const __dirname = url.fileURLToPath(new URL('.', import.meta.url));

const serverSrc = path.join(__dirname, 'src');
const clientSrc = path.join(__dirname, 'src', 'public');
const dstRoot = path.join(__dirname, 'dist');
const clientDst = path.join(dstRoot, 'public');

export default () => {
  const sourcemap = process.env.NODE_ENV != 'production' ? 'inline' : false;
  const env = process.env.NODE_ENV != 'production' ? '.env.development' : '.env';

  const plugins = [
    typescript({
      sourceMap: true,
      inlineSources: true,
      tsconfig: path.join(clientSrc, 'tsconfig.json'),
    }),
    commonjs({ extensions: ['.js', '.ts', '.mts'] }),
    nodeResolve({
      browser: true,
      preferBuiltins: false
    }),
    builtins(),
    globals(),
    json(),
    sourcemaps(),
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
      copy({
        targets: [{
          src: 'firebase.json',
          dest: dstRoot,
        }, {
          src: path.join(clientSrc, '*.svg'),
          dest: clientDst,
        }, {
          src: path.join(clientSrc, 'manifest.json'),
          dest: clientDst,
        }, {
          src: path.join(serverSrc, 'templates', '*'),
          dest: path.join(dstRoot, 'templates'),
        }, {
          src: path.join(serverSrc, env),
          dest: dstRoot,
          rename: '.env'
        }]
      }),
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
        include: [
          path.join(clientSrc, 'styles', '*.css'),
          path.join(clientSrc, 'styles', '*.scss'),
          './node_modules/**/*.*'
        ],
        name: 'style.css',
        outputStyle: 'compressed',
      }),
      nodeResolve({
        browser: true,
        preferBuiltins: false
      }),
      css(),
    ]
  }];
};

