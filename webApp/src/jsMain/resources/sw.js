/*
 * QR-SHIELD Service Worker
 * Enables offline functionality and PWA installation
 */

const CACHE_NAME = 'qr-shield-v2.14.0';
const DEV_HOSTS = new Set(['localhost', '127.0.0.1']);

function isDevHost() {
    try {
        const scopeUrl = new URL(self.registration.scope);
        return DEV_HOSTS.has(scopeUrl.hostname);
    } catch (e) {
        return false;
    }
}

// Complete list of all assets for offline functionality
const STATIC_ASSETS = [
    // Root
    './',
    './index.html',

    // Page Files - HTML, CSS, JS
    './dashboard.html',
    './dashboard.css',
    './dashboard.js',
    './scanner.html',
    './scanner.css',
    './scanner.js',
    './results.html',
    './results.css',
    './results.js',
    './threat.html',
    './threat.css',
    './threat.js',
    './export.html',
    './export.css',
    './export.js',
    './trust.html',
    './trust.css',
    './trust.js',
    './onboarding.html',
    './onboarding.css',
    './onboarding.js',
    './game.html',
    './game.css',
    './game.js',

    // Shared CSS/JS
    './theme.css',
    './theme.js',
    './transitions.css',
    './transitions.js',
    './shared-ui.css',
    './shared-ui.js',
    './shared-header.css',

    // Fonts
    './fonts.css',
    './fonts/inter-latin.woff2',
    './fonts/material-symbols.woff2',

    // Core JS
    './platform-bridge.js',
    './webApp.js',
    './jsQR.min.js',
    './visualizer.js',

    // Assets - ALL icons
    './assets/logo.svg',
    './assets/icon-512.png',
    './assets/icon-256.png',
    './assets/icon-128.png',
    './assets/favicon-32.png',
    './assets/favicon-16.png',
    './assets/shield-safe.svg',
    './assets/shield-warning.svg',
    './assets/shield-danger.svg',

    // PWA manifest
    './manifest.json'
];

// Install event - cache static assets with individual error handling
self.addEventListener('install', (event) => {
    console.log('[SW] Installing QR-SHIELD service worker v2.14.0 (100% Offline Mode)...');
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(async (cache) => {
                console.log('[SW] Caching', STATIC_ASSETS.length, 'static assets');

                // Cache each asset individually to avoid one failure breaking everything
                const results = await Promise.allSettled(
                    STATIC_ASSETS.map(async (url) => {
                        try {
                            const response = await fetch(url);
                            if (!response.ok) {
                                throw new Error(`HTTP ${response.status}`);
                            }
                            await cache.put(url, response);
                            return { url, status: 'cached' };
                        } catch (err) {
                            console.warn('[SW] Failed to cache:', url, err.message);
                            return { url, status: 'failed', error: err.message };
                        }
                    })
                );

                const cached = results.filter(r => r.value?.status === 'cached').length;
                const failed = results.filter(r => r.value?.status === 'failed').length;
                console.log(`[SW] Cached ${cached}/${STATIC_ASSETS.length} assets (${failed} failed)`);

                return Promise.resolve();
            })
            .then(() => {
                console.log('[SW] Installation complete, activating immediately');
                return self.skipWaiting();
            })
    );
});

// Activate event - clean up old caches
self.addEventListener('activate', (event) => {
    console.log('[SW] Activating service worker...');
    event.waitUntil(
        caches.keys().then((cacheNames) => {
            return Promise.all(
                cacheNames
                    .filter((name) => name !== CACHE_NAME)
                    .map((name) => {
                        console.log('[SW] Deleting old cache:', name);
                        return caches.delete(name);
                    })
            );
        }).then(() => self.clients.claim())
    );
});

// Fetch event - cache-first with network update (stale-while-revalidate)
// This ensures FULL offline functionality on all hosts including dev
self.addEventListener('fetch', (event) => {
    // Skip non-GET requests
    if (event.request.method !== 'GET') return;

    // Skip cross-origin requests
    if (!event.request.url.startsWith(self.location.origin)) {
        return;
    }

    event.respondWith(
        caches.match(event.request)
            .then((cachedResponse) => {
                // Start a network request in background (for updating cache)
                const networkFetch = fetch(event.request)
                    .then((response) => {
                        // Cache the new response
                        if (response && response.status === 200) {
                            const responseClone = response.clone();
                            caches.open(CACHE_NAME).then((cache) => {
                                cache.put(event.request, responseClone);
                            });
                        }
                        return response;
                    })
                    .catch(() => null);

                // Return cached response immediately if available (fast offline!)
                if (cachedResponse) {
                    return cachedResponse;
                }

                // No cache - wait for network
                return networkFetch.then((response) => {
                    if (response) {
                        return response;
                    }

                    // Network failed too - try page fallback for navigation
                    if (event.request.mode === 'navigate') {
                        const url = new URL(event.request.url);
                        const pageName = url.pathname.split('/').pop() || 'index.html';
                        return caches.match('./' + pageName)
                            .then(cached => cached || caches.match('./scanner.html'))
                            .then(cached => cached || caches.match('./index.html'));
                    }

                    return undefined;
                });
            })
    );
});

// Handle messages from the main thread
self.addEventListener('message', (event) => {
    if (event.data === 'skipWaiting') {
        self.skipWaiting();
    }
});
