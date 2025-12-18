/**
 * QR-SHIELD Page Transitions Controller
 * Handles smooth page exit animations and navigation
 */

(function () {
    'use strict';

    // Configuration
    const TRANSITION_DURATION = 200; // ms to wait before navigating
    const EXCLUDED_LINKS = ['#', 'javascript:', 'mailto:', 'tel:'];

    /**
     * Initialize page transitions
     */
    function init() {
        // Don't run on pages loaded via back/forward navigation
        if (performance.getEntriesByType &&
            performance.getEntriesByType('navigation').length > 0 &&
            performance.getEntriesByType('navigation')[0].type === 'back_forward') {
            return;
        }

        // Attach click handlers to all internal links
        document.addEventListener('click', handleLinkClick);

        // Handle popstate (back/forward buttons)
        window.addEventListener('popstate', handlePopState);

        // Mark page as loaded for entrance animations
        document.body.classList.add('page-loaded');

        console.log('[Transitions] Page transitions initialized');
    }

    /**
     * Check if a link is internal and should have transitions
     */
    function isInternalLink(href) {
        if (!href) return false;

        // Check for excluded prefixes
        for (const excluded of EXCLUDED_LINKS) {
            if (href.startsWith(excluded)) return false;
        }

        // Check if it's a relative URL or same-origin
        try {
            const linkUrl = new URL(href, window.location.origin);
            const currentUrl = new URL(window.location.href);

            // Same origin check
            if (linkUrl.origin !== currentUrl.origin) return false;

            // Check for HTML files (internal navigation)
            if (href.endsWith('.html') || linkUrl.pathname.endsWith('.html')) {
                return true;
            }

            return false;
        } catch (e) {
            // If URL parsing fails, check if it ends with .html
            return href.endsWith('.html');
        }
    }

    /**
     * Handle click events on links
     */
    function handleLinkClick(event) {
        // Find the closest anchor element
        const link = event.target.closest('a');
        if (!link) return;

        const href = link.getAttribute('href');

        // Check if this is an internal link that should transition
        if (!isInternalLink(href)) return;

        // Don't interfere with modified clicks
        if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;

        // Prevent default navigation
        event.preventDefault();

        // Navigate with transition
        navigateWithTransition(href);
    }

    /**
     * Navigate to a URL with a smooth exit transition
     */
    function navigateWithTransition(url) {
        // Add exit animation class
        document.body.classList.add('page-exit');

        // Wait for animation, then navigate
        setTimeout(() => {
            window.location.href = url;
        }, TRANSITION_DURATION);
    }

    /**
     * Handle browser back/forward navigation
     */
    function handlePopState() {
        // Reload without transition for back/forward
        document.body.classList.remove('page-exit');
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Expose for external use if needed
    window.QRShieldTransitions = {
        navigateWithTransition: navigateWithTransition
    };

})();
