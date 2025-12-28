/**
 * QR-SHIELD Page Transitions Controller v2.1
 * Fast, smooth page transitions without theme flashing
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

        // Mark page as loaded
        requestAnimationFrame(() => {
            document.body.classList.add('page-loaded');
        });

        // Detect font loading
        detectFontLoading();

        console.log('[Transitions v2.1] Initialized');
    }

    /**
     * Detect when fonts are loaded
     */
    function detectFontLoading() {
        if (document.fonts && document.fonts.ready) {
            document.fonts.ready.then(() => {
                document.body.classList.add('fonts-loaded');
            }).catch(() => {
                setTimeout(() => {
                    document.body.classList.add('fonts-loaded');
                }, 200);
            });
        } else {
            setTimeout(() => {
                document.body.classList.add('fonts-loaded');
            }, 200);
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
