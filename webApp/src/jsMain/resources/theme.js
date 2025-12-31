/*
 * Mehr Guard Theme System
 * JavaScript for handling theme switching and persistence
 */

(function () {
    'use strict';

    const THEME_KEY = 'mehrguard_theme';
    const THEMES = {
        LIGHT: 'light',
        DARK: 'dark',
        AUTO: 'auto'
    };
    const NORMALIZE_RE = /\s+/g;

    function normalizeKey(text) {
        return text ? text.replace(NORMALIZE_RE, ' ').trim() : '';
    }

    function translateText(text) {
        const normalized = normalizeKey(text);
        if (!normalized) return text;
        if (window.mehrguardTranslateText) {
            return window.mehrguardTranslateText(normalized);
        }
        if (window.mehrguardGetTranslation) {
            return window.mehrguardGetTranslation(normalized);
        }
        return normalized;
    }

    function formatText(template, params = {}) {
        let translated = translateText(template);
        Object.keys(params).forEach((key) => {
            translated = translated.replaceAll(`{${key}}`, params[key]);
        });
        return translated;
    }

    /**
     * Get the current system preferred theme
     */
    function getSystemTheme() {
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
            return THEMES.LIGHT;
        }
        return THEMES.DARK;
    }

    /**
     * Get the saved theme preference or default to auto
     */
    function getSavedTheme() {
        try {
            return localStorage.getItem(THEME_KEY) || THEMES.AUTO;
        } catch (e) {
            return THEMES.AUTO;
        }
    }

    /**
     * Save theme preference to localStorage
     */
    function saveTheme(theme) {
        try {
            localStorage.setItem(THEME_KEY, theme);
        } catch (e) {
            console.warn('Could not save theme preference:', e);
        }
    }

    /**
     * Get the effective theme (resolving 'auto' to actual theme)
     */
    function getEffectiveTheme() {
        const saved = getSavedTheme();
        if (saved === THEMES.AUTO) {
            return getSystemTheme();
        }
        return saved;
    }

    /**
     * Apply theme to the document
     */
    function applyTheme(theme) {
        const effectiveTheme = theme === THEMES.AUTO ? getSystemTheme() : theme;

        // Update data-theme attribute
        document.documentElement.setAttribute('data-theme', effectiveTheme);

        // Update class for legacy support
        document.documentElement.classList.remove('light', 'dark');
        document.documentElement.classList.add(effectiveTheme);

        document.body.classList.remove('light', 'dark');
        document.body.classList.add(effectiveTheme);

        // Update meta theme-color for mobile browsers
        const metaThemeColor = document.querySelector('meta[name="theme-color"]');
        if (metaThemeColor) {
            metaThemeColor.setAttribute('content', effectiveTheme === THEMES.LIGHT ? '#f8fafc' : '#0f1115');
        }

        // Dispatch custom event for other scripts to listen
        window.dispatchEvent(new CustomEvent('themechange', {
            detail: { theme: effectiveTheme, preference: theme }
        }));

        // Update theme toggle buttons
        updateThemeToggleIcons(effectiveTheme);
    }

    /**
     * Update theme toggle button icons
     */
    function updateThemeToggleIcons(effectiveTheme) {
        const toggleBtns = document.querySelectorAll('.theme-toggle');
        toggleBtns.forEach(btn => {
            const lightIcon = btn.querySelector('.icon-light');
            const darkIcon = btn.querySelector('.icon-dark');

            if (lightIcon && darkIcon) {
                if (effectiveTheme === THEMES.LIGHT) {
                    lightIcon.style.display = 'none';
                    darkIcon.style.display = 'block';
                } else {
                    lightIcon.style.display = 'block';
                    darkIcon.style.display = 'none';
                }
            }
        });
    }

    /**
     * Toggle between light and dark themes
     */
    function toggleTheme() {
        const current = getEffectiveTheme();
        const newTheme = current === THEMES.LIGHT ? THEMES.DARK : THEMES.LIGHT;
        saveTheme(newTheme);
        applyTheme(newTheme);
        return newTheme;
    }

    /**
     * Set a specific theme
     */
    function setTheme(theme) {
        if (!Object.values(THEMES).includes(theme)) {
            console.warn('Invalid theme:', theme);
            return;
        }
        saveTheme(theme);
        applyTheme(theme);
    }

    /**
     * Initialize theme toggle buttons
     */
    function initThemeToggle() {
        // Create toggle button HTML if not exists
        const toggleBtns = document.querySelectorAll('.theme-toggle');

        toggleBtns.forEach(btn => {
            // Add icons if not already present
            if (!btn.querySelector('.icon-light') && !btn.querySelector('.icon-dark')) {
                btn.innerHTML = `
                    <span class="material-symbols-outlined icon-light">light_mode</span>
                    <span class="material-symbols-outlined icon-dark">dark_mode</span>
                `;
            }

            btn.addEventListener('click', () => {
                const newTheme = toggleTheme();

                // Show toast feedback if available
                if (window.MehrGuardUI && window.MehrGuardUI.showToast) {
                    const modeLabel = translateText(newTheme === THEMES.LIGHT ? 'Light' : 'Dark');
                    window.MehrGuardUI.showToast(
                        formatText('Switched to {mode} mode', { mode: modeLabel }),
                        'info'
                    );
                }
            });
        });
    }

    /**
     * Add theme toggle button to header if not exists
     */
    function injectThemeToggle() {
        // Look for common header locations
        const headerRight = document.querySelector('.header-right, .header-actions, .top-header .flex:last-child');

        if (headerRight && !headerRight.querySelector('.theme-toggle')) {
            const toggleBtn = document.createElement('button');
            toggleBtn.className = 'theme-toggle';
            toggleBtn.setAttribute('aria-label', translateText('Toggle theme'));
            toggleBtn.setAttribute('title', translateText('Toggle light/dark mode'));
            toggleBtn.innerHTML = `
                <span class="material-symbols-outlined icon-light">light_mode</span>
                <span class="material-symbols-outlined icon-dark">dark_mode</span>
            `;

            // Insert before first child or notification button
            const notificationBtn = headerRight.querySelector('.notification-btn, [aria-label*="notification"]');
            if (notificationBtn) {
                headerRight.insertBefore(toggleBtn, notificationBtn);
            } else {
                headerRight.insertBefore(toggleBtn, headerRight.firstChild);
            }
        }
    }

    /**
     * Listen for system theme changes
     */
    function listenForSystemThemeChanges() {
        if (window.matchMedia) {
            const mediaQuery = window.matchMedia('(prefers-color-scheme: light)');

            const handleChange = (e) => {
                const saved = getSavedTheme();
                if (saved === THEMES.AUTO) {
                    applyTheme(THEMES.AUTO);
                }
            };

            // Modern browsers
            if (mediaQuery.addEventListener) {
                mediaQuery.addEventListener('change', handleChange);
            } else {
                // Legacy Safari
                mediaQuery.addListener(handleChange);
            }
        }
    }

    /**
     * Initialize the theme system
     */
    function init() {
        // Apply saved theme immediately
        applyTheme(getSavedTheme());

        // Set up toggle buttons when DOM is ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => {
                injectThemeToggle();
                initThemeToggle();
            });
        } else {
            injectThemeToggle();
            initThemeToggle();
        }

        // Listen for system theme changes
        listenForSystemThemeChanges();
    }

    // Expose API globally
    window.MehrGuardTheme = {
        toggle: toggleTheme,
        set: setTheme,
        get: getEffectiveTheme,
        getSaved: getSavedTheme,
        THEMES: THEMES,
        init: init
    };

    // Auto-initialize
    init();

})();
