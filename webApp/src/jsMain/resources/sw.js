/*
 * QR-SHIELD Service Worker
 * Enables offline functionality and PWA installation
 */

const CACHE_NAME = 'qr-shield-v2.1.0';
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
    './transitions.css',
    './transitions.js',
    './styles.css',
    './app.js',
    './webApp.js',
    './assets/logo.svg',
    './manifest.json'
];

// Install event - cache static assets
self.addEventListener('install', (event) => {
    console.log('[SW] Installing QR-SHIELD service worker...');
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
                            return caches.match('./dashboard.html');
                        }
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
