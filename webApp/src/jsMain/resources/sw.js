/**
 * Mehr Guard Service Worker v2.16.0
 * 100% Offline-First PWA with Cache-First Strategy
 * 
 * Based on Google's Workbox best practices (2024)
 * - Uses cache.addAll with absolute URLs for reliable caching
 * - Implements cache-first with network fallback
 * - Includes extensive debug logging
 */

const CACHE_VERSION = 'v2.17.0';
const CACHE_NAME = `mehr-guard-${CACHE_VERSION}`;

// Debug mode - logs all cache operations
const DEBUG = true;

function log(...args) {
    if (DEBUG) console.log('[SW]', ...args);
}

function warn(...args) {
    console.warn('[SW]', ...args);
}

function error(...args) {
    console.error('[SW]', ...args);
}

// All files to precache (relative to service worker location)
const PRECACHE_ASSETS = [
    // Pages (HTML)
    './',
    './index.html',
    './dashboard.html',
    './scanner.html',
    './results.html',
    './threat.html',
    './export.html',
    './trust.html',
    './onboarding.html',
    './game.html',

    // Stylesheets (CSS)
    './dashboard.css',
    './scanner.css',
    './results.css',
    './threat.css',
    './export.css',
    './trust.css',
    './onboarding.css',
    './game.css',
    './theme.css',
    './transitions.css',
    './shared-ui.css',
    './shared-header.css',
    './fonts.css',

    // Scripts (JS)
    './dashboard.js',
    './scanner.js',
    './results.js',
    './threat.js',
    './export.js',
    './trust.js',
    './onboarding.js',
    './game.js',
    './theme.js',
    './transitions.js',
    './shared-ui.js',
    './platform-bridge.js',
    './webApp.js',
    './jsQR.min.js',
    './visualizer.js',

    // Fonts
    './fonts/inter-latin.woff2',
    './fonts/material-symbols.woff2',

    // Assets
    './assets/logo.svg',
    './assets/icon-512.png',
    './assets/icon-256.png',
    './assets/icon-128.png',
    './assets/favicon-32.png',
    './assets/favicon-16.png',
    './assets/shield-safe.svg',
    './assets/shield-warning.svg',
    './assets/shield-danger.svg',

    // PWA Manifest
    './manifest.json'
];

/**
 * Convert relative URL to absolute URL based on service worker scope
 */
function toAbsoluteUrl(relativeUrl) {
    // Use service worker location as base (not scope, which might be different)
    return new URL(relativeUrl, self.location.href).href;
}

/**
 * INSTALL EVENT - Precache all static assets
 * Uses individual fetches to handle failures gracefully
 */
self.addEventListener('install', (event) => {
    log(`Installing ${CACHE_NAME}...`);
    log('Service worker location:', self.location.href);

    event.waitUntil(
        caches.open(CACHE_NAME).then(async (cache) => {
            log(`Opened cache: ${CACHE_NAME}`);
            log(`Precaching ${PRECACHE_ASSETS.length} assets...`);

            let successCount = 0;
            let failedAssets = [];

            // Cache each asset individually for better error handling
            for (const asset of PRECACHE_ASSETS) {
                try {
                    const absoluteUrl = toAbsoluteUrl(asset);

                    // Fetch with cache: 'reload' to ensure fresh copy
                    const response = await fetch(absoluteUrl, { cache: 'reload' });

                    if (!response.ok) {
                        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                    }

                    // Cache with the absolute URL
                    await cache.put(absoluteUrl, response.clone());

                    // Also cache with the relative URL for matching flexibility
                    await cache.put(asset, response.clone());

                    successCount++;
                    log(`✓ Cached: ${asset}`);
                } catch (err) {
                    failedAssets.push({ asset, error: err.message });
                    warn(`✗ Failed to cache: ${asset} - ${err.message}`);
                }
            }

            log(`=================================`);
            log(`INSTALL COMPLETE`);
            log(`Cached: ${successCount}/${PRECACHE_ASSETS.length}`);

            if (failedAssets.length > 0) {
                warn(`Failed assets (${failedAssets.length}):`, failedAssets);
            }
            log(`=================================`);

            return Promise.resolve();
        }).then(() => {
            log('Calling skipWaiting()...');
            return self.skipWaiting();
        }).catch((err) => {
            error('Install failed:', err);
            throw err;
        })
    );
});

/**
 * ACTIVATE EVENT - Clean up old caches and claim clients
 */
self.addEventListener('activate', (event) => {
    log('Activating...');

    event.waitUntil(
        caches.keys().then((cacheNames) => {
            return Promise.all(
                cacheNames
                    .filter((name) => name.startsWith('mehr-guard-') && name !== CACHE_NAME)
                    .map((name) => {
                        log(`Deleting old cache: ${name}`);
                        return caches.delete(name);
                    })
            );
        }).then(() => {
            log('Claiming clients...');
            return self.clients.claim();
        }).then(() => {
            log('=================================');
            log('SERVICE WORKER ACTIVE');
            log(`Cache: ${CACHE_NAME}`);
            log('Ready for offline mode!');
            log('=================================');
        })
    );
});

/**
 * FETCH EVENT - Cache-first with network fallback
 * This ensures offline mode works reliably
 */
self.addEventListener('fetch', (event) => {
    const request = event.request;

    // Skip non-GET requests
    if (request.method !== 'GET') {
        return;
    }

    // Skip cross-origin requests (external APIs, etc.)
    const requestUrl = new URL(request.url);
    if (requestUrl.origin !== self.location.origin) {
        log(`Skipping cross-origin: ${request.url}`);
        return;
    }

    event.respondWith(handleFetch(request));
});

/**
 * Handle fetch with cache-first strategy
 * Strips query strings for cache matching to handle versioned URLs like file.css?v=2
 */
async function handleFetch(request) {
    const url = request.url;

    // Create a URL without query string for cache matching
    const urlWithoutQuery = new URL(url);
    urlWithoutQuery.search = '';
    const cleanUrl = urlWithoutQuery.href;

    try {
        // Try to find in cache first (try both with and without query string)
        let cachedResponse = await caches.match(request);

        // If not found, try without query string
        if (!cachedResponse && url !== cleanUrl) {
            cachedResponse = await caches.match(cleanUrl);
            if (cachedResponse) {
                log(`✓ Cache hit (without query): ${cleanUrl}`);
            }
        }

        if (cachedResponse) {
            log(`✓ Cache hit: ${url}`);

            // Update cache in background (stale-while-revalidate)
            updateCacheInBackground(request);

            return cachedResponse;
        }

        log(`○ Cache miss: ${url}`);

        // Not in cache - try network
        try {
            const networkResponse = await fetch(request);

            if (networkResponse.ok) {
                log(`↓ Network fetch: ${url}`);

                // Cache the response for next time
                const cache = await caches.open(CACHE_NAME);
                await cache.put(request, networkResponse.clone());

                return networkResponse;
            } else {
                warn(`Network error ${networkResponse.status}: ${url}`);
            }
        } catch (networkError) {
            warn(`Network failed: ${url} - ${networkError.message}`);
        }

        // Network failed - try fallback for navigation requests
        if (request.mode === 'navigate') {
            log(`→ Attempting navigation fallback for: ${url}`);

            // Try to find the specific page
            const pageName = new URL(url).pathname.split('/').pop() || 'index.html';

            // Try relative path first
            let fallback = await caches.match(`./${pageName}`);

            // Try absolute URL
            if (!fallback) {
                fallback = await caches.match(toAbsoluteUrl(`./${pageName}`));
            }

            // Fallback to dashboard
            if (!fallback) {
                fallback = await caches.match('./dashboard.html') ||
                    await caches.match(toAbsoluteUrl('./dashboard.html'));
            }

            // Fallback to index
            if (!fallback) {
                fallback = await caches.match('./index.html') ||
                    await caches.match(toAbsoluteUrl('./index.html'));
            }

            if (fallback) {
                log(`→ Serving fallback page: ${pageName}`);
                return fallback;
            }
        }

        // No cache, no network, no fallback - return error response
        error(`Failed to serve: ${url}`);
        return new Response('Offline - Resource not available', {
            status: 503,
            statusText: 'Service Unavailable',
            headers: { 'Content-Type': 'text/plain' }
        });

    } catch (err) {
        error(`Fetch handler error: ${url}`, err);
        return new Response('Error', { status: 500 });
    }
}

/**
 * Update cache in background without blocking response
 */
function updateCacheInBackground(request) {
    fetch(request)
        .then((response) => {
            if (response.ok) {
                caches.open(CACHE_NAME).then((cache) => {
                    cache.put(request, response);
                    log(`↻ Background update: ${request.url}`);
                });
            }
        })
        .catch(() => {
            // Ignore network errors during background update
        });
}

/**
 * MESSAGE EVENT - Handle skip waiting messages
 */
self.addEventListener('message', (event) => {
    log('Received message:', event.data);

    if (event.data === 'skipWaiting' || event.data?.type === 'SKIP_WAITING') {
        log('Skipping waiting...');
        self.skipWaiting();
    }
});

// Log when script is loaded
log('=================================');
log(`Mehr Guard Service Worker ${CACHE_VERSION}`);
log('Script loaded successfully');
log('=================================');
