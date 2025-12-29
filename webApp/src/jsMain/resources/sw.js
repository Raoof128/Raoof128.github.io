/*
 * QR-SHIELD Service Worker
 * Enables offline functionality and PWA installation
 */

const CACHE_NAME = 'qr-shield-v2.10.0';
const DEV_HOSTS = new Set(['localhost', '127.0.0.1']);

function isDevHost() {
    try {
        const scopeUrl = new URL(self.registration.scope);
        return DEV_HOSTS.has(scopeUrl.hostname);
    } catch (e) {
        return false;
    }
}
const STATIC_ASSETS = [
    './',
    './index.html',
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
    './theme.css',
    './theme.js',
    './transitions.css',
    './transitions.js',
    './shared-ui.css',
    './shared-ui.js',
    './shared-header.css',
    './fonts.css',
    './fonts/inter-latin.woff2',
    './fonts/material-symbols.woff2',
    './platform-bridge.js',
    './webApp.js',
    './jsQR.min.js',
    './visualizer.js',
    './assets/logo.svg',
    './assets/icon-512.png',
    './assets/icon-256.png',
    './assets/icon-128.png',
    './assets/favicon-32.png',
    './assets/favicon-16.png',
    './assets/shield-safe.svg',
    './assets/shield-warning.svg',
    './assets/shield-danger.svg',
    './manifest.json'
];

// Install event - cache static assets
self.addEventListener('install', (event) => {
    console.log('[SW] Installing QR-SHIELD service worker...');
    if (isDevHost()) {
        self.skipWaiting();
        return;
    }
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then((cache) => {
                console.log('[SW] Caching static assets');
                return cache.addAll(STATIC_ASSETS);
            })
            .then(() => self.skipWaiting())
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

// Fetch event - serve from cache, fall back to network
self.addEventListener('fetch', (event) => {
    // Skip non-GET requests
    if (event.request.method !== 'GET') return;

    // Skip cross-origin requests (like fonts, CDN)
    if (!event.request.url.startsWith(self.location.origin)) {
        return;
    }

    if (isDevHost()) {
        event.respondWith(fetch(event.request));
        return;
    }

    event.respondWith(
        caches.match(event.request)
            .then((cachedResponse) => {
                if (cachedResponse) {
                    // Return cache hit, but also fetch update in background
                    fetch(event.request).then((response) => {
                        if (response && response.status === 200) {
                            caches.open(CACHE_NAME).then((cache) => {
                                cache.put(event.request, response);
                            });
                        }
                    }).catch(() => { });

                    return cachedResponse;
                }

                // Not in cache - fetch from network
                return fetch(event.request)
                    .then((response) => {
                        // Cache successful responses
                        if (response && response.status === 200) {
                            const responseClone = response.clone();
                            caches.open(CACHE_NAME).then((cache) => {
                                cache.put(event.request, responseClone);
                            });
                        }
                        return response;
                    })
                    .catch(() => {
                        // Offline fallback for navigation requests
                        if (event.request.mode === 'navigate') {
                            // Try to match the specific page first
                            const url = new URL(event.request.url);
                            const pageName = url.pathname.split('/').pop() || 'index.html';

                            return caches.match('./' + pageName)
                                .then(cached => cached || caches.match('./scanner.html'))
                                .then(cached => cached || caches.match('./index.html'));
                        }
                        // For other requests, return nothing (let browser handle)
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
