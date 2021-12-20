import path from 'path';
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

const serverSrc = path.join(__dirname, 'src');
const clientSrc = path.join(__dirname, 'src', 'public');
const dstRoot = path.join(__dirname, 'dist');
const clientDst = path.join(dstRoot, 'public');

export default () => {
  const sourcemap = process.env.NODE_ENV != 'production' ? 'inline' : false;
  const env = process.env.NODE_ENV != 'production' ? '.env.development' : '.env.production';

  const plugins = [
    typescript({
      sourceMap: true,
      inlineSources: true,
      tsconfig: path.join(clientSrc, 'tsconfig.json'),
    }),
    commonjs({ extensions: ['.js', '.ts'] }),
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
          src: path.join(clientSrc, 'favicon.*'),
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
    plugins: [
      scss({
        include: [
          path.join(clientSrc, 'styles', '*.css'),
          path.join(clientSrc, 'styles', '*.scss'),
          './node_modules/**/*.*'
        ],
        output: path.join(clientDst, 'styles', 'style.css'),
      }),
      nodeResolve({
        browser: true,
        preferBuiltins: false
      }),
      css(),
    ]
  }];
};

