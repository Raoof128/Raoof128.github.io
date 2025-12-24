// Webpack configuration to handle Skiko module resolution for wasmJs
// The common module has Compose dependencies that transitively depend on Skiko,
// but the webApp doesn't actually use Compose UI (it uses HTML/CSS).
// This configuration ignores the skiko module since we don't need it.

config.resolve = config.resolve || {};
config.resolve.alias = config.resolve.alias || {};

// Create an alias that points to an empty module
config.resolve.alias['./skiko.mjs'] = false;
config.resolve.fallback = config.resolve.fallback || {};
config.resolve.fallback['./skiko.mjs'] = false;

// Alternatively, ignore the module entirely
config.externals = config.externals || [];
if (!Array.isArray(config.externals)) {
    config.externals = [config.externals];
}
config.externals.push({
    './skiko.mjs': 'commonjs ./skiko.mjs'
});
