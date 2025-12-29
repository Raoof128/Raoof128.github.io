// Webpack configuration to bundle @js-joda/core locally for offline support
// This ensures the WASM loader doesn't fail with bare import errors in browser
// and works completely offline without CDN dependencies.

const path = require('path');

config.resolve = config.resolve || {};
config.resolve.alias = config.resolve.alias || {};

// Point @js-joda/core to the node_modules copy that Gradle downloads
// __dirname during webpack run is inside build/wasm/packages/QRShield-webApp
// So we go up to build/wasm/ then into node_modules
const jodaPath = path.resolve(__dirname, '../../node_modules/@js-joda/core/dist/js-joda.esm.js');

config.resolve.alias['@js-joda/core'] = jodaPath;

console.log('[webpack] @js-joda/core aliased to:', jodaPath);
