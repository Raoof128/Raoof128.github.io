/**
 * QR-SHIELD Page Transitions Controller v2.2
 * Fast, smooth page transitions without theme/font flashing
 * Uses font preloading and font-display: block for FOUT prevention
 */

(function () {
    'use strict';

    // Configuration - Ultra-fast for snappy feel
    const TRANSITION_DURATION = 80; // ms - very fast
    const EXCLUDED_LINKS = ['#', 'javascript:', 'mailto:', 'tel:'];

    /**
     * Initialize page transitions
     */
    function init() {
        // Attach click handlers to all internal links
        document.addEventListener('click', handleLinkClick);

        // Handle popstate (back/forward buttons)
        window.addEventListener('popstate', handlePopState);

        // Detect when icon font is loaded to prevent text fallback on slow connections
        detectIconFontLoaded();

        // Mark page as loaded
        requestAnimationFrame(() => {
            document.body.classList.add('page-loaded');
        });

        console.log('[Transitions v2.2] Initialized');
    }

    /**
     * Detect when Material Symbols icon font is loaded
     * This prevents icon text names from showing on slow 3G connections
     */
    function detectIconFontLoaded() {
        if ('fonts' in document) {
            // Use Font Loading API (modern browsers)
            document.fonts.ready.then(() => {
                document.documentElement.classList.add('fonts-loaded');
            });

            // Also specifically check for Material Symbols
            document.fonts.load('24px "Material Symbols Outlined"').then(() => {
                document.documentElement.classList.add('fonts-loaded');
            }).catch(() => {
                // Fallback: add class anyway after timeout to ensure icons show
                setTimeout(() => {
                    document.documentElement.classList.add('fonts-loaded');
                }, 3000);
            });
        } else {
            // Fallback for older browsers: assume fonts loaded after 1 second
            setTimeout(() => {
                document.documentElement.classList.add('fonts-loaded');
            }, 1000);
        }
    }

    /**
     * Check if a link is internal
     */
    function isInternalLink(href) {
        if (!href) return false;

        for (const excluded of EXCLUDED_LINKS) {
            if (href.startsWith(excluded)) return false;
        }

        try {
            const linkUrl = new URL(href, window.location.origin);
            const currentUrl = new URL(window.location.href);

            if (linkUrl.origin !== currentUrl.origin) return false;
            if (href.endsWith('.html') || linkUrl.pathname.endsWith('.html')) {
                return true;
            }
            return false;
        } catch (e) {
            return href.endsWith('.html');
        }
    }

    /**
     * Handle click events on links
     */
    function handleLinkClick(event) {
        const link = event.target.closest('a');
        if (!link) return;

        const href = link.getAttribute('href');
        if (!isInternalLink(href)) return;
        if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;

        event.preventDefault();
        navigateWithTransition(href);
    }

    /**
     * Navigate with fast CSS transition (no View Transitions API to avoid theme flash)
     */
    function navigateWithTransition(url) {
        // Add exit animation class
        document.body.classList.add('page-exit');

        // Navigate after short delay
        setTimeout(() => {
            window.location.href = url;
        }, TRANSITION_DURATION);
    }

    /**
     * Handle browser back/forward
     */
    function handlePopState() {
        document.body.classList.remove('page-exit');
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Expose API
    window.QRShieldTransitions = {
        navigateWithTransition: navigateWithTransition
    };

})();
