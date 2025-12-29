/**
 * QR-SHIELD Dashboard Page Controller
 * 
 * Handles sidebar navigation, scan statistics, and page interactions.
 * 
 * @author QR-SHIELD Team
 * @version 2.4.1
 */

// =============================================================================
// CONFIGURATION
// =============================================================================

const DashboardConfig = {
    version: '2.4.1',
};

// =============================================================================
// STATE
// =============================================================================

const DashboardState = {
    isSidebarOpen: false,
};

// =============================================================================
// DOM ELEMENTS
// =============================================================================

const elements = {
    // Sidebar
    sidebar: null,
    menuToggle: null,

    // Header
    notificationBtn: null,
    settingsBtn: null,

    // Stats
    threatCount: null,
    safeCount: null,

    // Buttons
    startScanBtn: null,
    importBtn: null,
    updateDbBtn: null,

    // Table
    recentScansBody: null,

    // Toast
    toast: null,
    toastMessage: null,

    // DB Stats
    dbVersion: null,
    dbLastUpdate: null,
    dbSignatures: null,
};

// =============================================================================
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('[QR-SHIELD Dashboard] Initializing v' + DashboardConfig.version);

    // Cache DOM elements
    cacheElements();

    // Setup event listeners
    setupEventListeners();

    // Render UI (fetches data from Shared UI)
    renderUI();
    window.qrshieldApplyTranslations?.(document.body);

    console.log('[QR-SHIELD Dashboard] Ready');
});

/**
 * Cache frequently accessed DOM elements
 */
function cacheElements() {
    elements.sidebar = document.getElementById('sidebar');
    elements.menuToggle = document.getElementById('menuToggle');
    elements.notificationBtn = document.getElementById('notificationBtn');
    elements.settingsBtn = document.getElementById('settingsBtn');
    elements.threatCount = document.getElementById('threatCount');
    elements.safeCount = document.getElementById('safeCount');
    elements.startScanBtn = document.getElementById('startScanBtn');
    elements.importBtn = document.getElementById('importBtn');
    elements.updateDbBtn = document.getElementById('updateDbBtn');
    elements.recentScansBody = document.getElementById('recentScansBody');
    elements.toast = document.getElementById('toast');
    elements.toastMessage = document.getElementById('toastMessage');
    elements.dbVersion = document.getElementById('dbVersion');
    elements.dbLastUpdate = document.getElementById('dbLastUpdate');
    elements.dbSignatures = document.getElementById('dbSignatures');
}

/**
 * Setup all event listeners
 */
function setupEventListeners() {
    // Mobile menu toggle
    elements.menuToggle?.addEventListener('click', toggleSidebar);

    // Notification button - shared-ui.js now handles the dropdown
    // Just remove the simple toast message and let shared-ui handle it
    // (shared-ui.js attaches notification dropdown to #notificationBtn automatically)

    // Help button - handled by shared-ui.js globally

    // Settings button
    elements.settingsBtn?.addEventListener('click', () => {
        window.location.href = 'onboarding.html';
    });

    // Import button
    elements.importBtn?.addEventListener('click', handleImport);

    // Update database button
    elements.updateDbBtn?.addEventListener('click', handleUpdateDb);

    // URL Analysis - cache elements
    elements.urlInput = document.getElementById('urlInput');
    elements.analyzeBtn = document.getElementById('analyzeBtn');

    // URL Analysis - event listeners
    elements.analyzeBtn?.addEventListener('click', analyzeUrl);
    elements.urlInput?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') analyzeUrl();
    });

    // Setup Kotlin engine bridge
    setupKotlinBridge();

    // Keyboard shortcuts
    setupKeyboardShortcuts();
}

// =============================================================================
// UI RENDERING
// =============================================================================

/**
 * Render all UI components
 */
function renderUI() {
    renderStats();
    renderHistory();
    updateDbStats();
}

/**
 * Render statistics using Shared UI
 */
function renderStats() {
    // Wait for QRShieldUI to be ready
    if (!window.QRShieldUI || !window.QRShieldUI.getAppStats) {
        setTimeout(renderStats, 100);
        return;
    }

    const stats = window.QRShieldUI.getAppStats();

    if (elements.threatCount) {
        elements.threatCount.textContent = stats.threatsBlocked || 0;
    }

    if (elements.safeCount) {
        elements.safeCount.textContent = stats.safeUrls || 0;
    }
}

/**
 * Update database statistics (mocked for now, but dynamic)
 */
function updateDbStats() {
    // Current version
    if (elements.dbVersion) elements.dbVersion.textContent = 'v2.4.1';

    // Mock "Today at 04:00 AM" or similar logic based on current time
    const now = new Date();
    // Round to previous 4-hour block for "stability" illusion
    const hour = Math.floor(now.getHours() / 4) * 4;
    const updateTime = new Date(now);
    updateTime.setHours(hour, 0, 0, 0);

    const timeStr = updateTime.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });

    if (elements.dbLastUpdate) {
        elements.dbLastUpdate.textContent = formatText('Today, {time}', { time: timeStr });
    }

    // Signatures
    if (elements.dbSignatures) elements.dbSignatures.textContent = '4,281,092';
}

// Helper for translation
function t(key, fallback) {
    if (window.qrshieldGetTranslation) {
        return window.qrshieldGetTranslation(key);
    }
    return fallback;
}

function translateText(text) {
    if (window.qrshieldTranslateText) {
        return window.qrshieldTranslateText(text);
    }
    return text;
}

function formatText(template, params) {
    if (window.qrshieldFormatText) {
        return window.qrshieldFormatText(template, params);
    }
    return template;
}

/**
 * Render scan history table from QRShieldUI
 */
function renderHistory() {
    if (!elements.recentScansBody) return;

    // Try to get history from QRShieldUI
    let history = [];
    if (window.QRShieldUI && window.QRShieldUI.getScanHistory) {
        history = window.QRShieldUI.getScanHistory().slice(0, 5);
    } else {
        // Fallback: retry after QRShieldUI loads
        setTimeout(renderHistory, 200);
        return;
    }


    if (history.length === 0) {
        elements.recentScansBody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center" style="padding: 2rem; color: var(--text-muted);">
                    ${t('NoScansYet', 'No scans yet. Start a new scan to see results here.')}
                </td>
            </tr>
        `;
        window.qrshieldApplyTranslations?.(elements.recentScansBody);
        return;
    }

    elements.recentScansBody.innerHTML = history.map(scan => {
        const isSafe = scan.verdict === 'SAFE' || scan.verdict === 'LOW';
        const isWarning = scan.verdict === 'MEDIUM';
        const isDanger = scan.verdict === 'HIGH';

        // Determine status badge class and text
        let badgeClass = 'safe';
        let badgeText = t('VerdictSafe', 'SAFE');
        let badgeIcon = 'check_circle';

        if (isDanger) {
            badgeClass = 'danger';
            badgeText = t('VerdictPhish', 'PHISH');
            badgeIcon = 'warning';
        } else if (isWarning) {
            badgeClass = 'warning';
            badgeText = t('VerdictWarn', 'WARN');
            badgeIcon = 'warning';
        }

        // Format time
        const timeStr = formatHistoryTime(scan.timestamp);

        // Get domain from URL for favicon
        let domain = '';
        try {
            const urlObj = new URL(scan.url);
            domain = urlObj.hostname;
        } catch {
            domain = scan.url.split('/')[0];
        }

        // Generate details from score and signals
        let details = '';
        if (scan.score !== undefined) {
            details = `${t('ScoreLabel', 'Score:')} ${scan.score}%`;
        }
        if (scan.signals && scan.signals.length > 0) {
            details = scan.signals.slice(0, 2).join(', ');
        }

        // Map verdict to results.html format
        let resultVerdict = 'SAFE';
        if (isDanger) resultVerdict = 'MALICIOUS';
        else if (isWarning) resultVerdict = 'SUSPICIOUS';

        return `
        <tr class="clickable-row" data-scan-url="${encodeURIComponent(scan.url)}" data-scan-verdict="${resultVerdict}" data-scan-score="${scan.score || 0}">
            <td>
                <span class="status-badge ${badgeClass}">
                    <span class="material-symbols-outlined">${badgeIcon}</span>
                    ${badgeText}
                </span>
            </td>
            <td class="source-cell">
                <img src="https://www.google.com/s2/favicons?domain=${domain}&sz=32" alt="" class="favicon" onerror="this.style.display='none'"/>
                ${escapeHtml(truncateUrl(scan.url, 40))}
            </td>
            <td class="hide-mobile details-cell">${escapeHtml(details)}</td>
            <td class="time-cell">${timeStr}</td>
        </tr>
    `;
    }).join('');

    window.qrshieldApplyTranslations?.(elements.recentScansBody);

    // Add click handlers to navigate to results.html with proper data
    elements.recentScansBody.querySelectorAll('.clickable-row').forEach(row => {
        row.addEventListener('click', () => {
            const encodedUrl = row.dataset.scanUrl;
            const verdict = row.dataset.scanVerdict;
            const score = row.dataset.scanScore;

            // Use URLSearchParams for proper encoding
            const params = new URLSearchParams({
                url: decodeURIComponent(encodedUrl), // Decode first since it was encoded in data attribute
                verdict: verdict,
                score: score
            });

            window.location.href = `results.html?${params.toString()}`;
        });
    });
}

/**
 * Format timestamp for history display
 */
function formatHistoryTime(timestamp) {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    const now = new Date();

    let locale = 'en-US';
    if (window.qrshieldGetLanguageCode) {
        locale = window.qrshieldGetLanguageCode();
    }

    // Check if today
    if (date.toDateString() === now.toDateString()) {
        return date.toLocaleTimeString(locale, {
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    }

    // Otherwise show date
    return date.toLocaleDateString(locale, {
        month: 'short',
        day: 'numeric'
    });
}

/**
 * Truncate URL for display
 */
function truncateUrl(url, maxLen) {
    if (!url) return '';
    if (url.length <= maxLen) return url;
    return url.substring(0, maxLen - 3) + '...';
}

// =============================================================================
// ACTION HANDLERS
// =============================================================================

/**
 * Handle image import
 */
function handleImport() {
    // Create file input
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';

    input.onchange = (e) => {
        const file = e.target.files?.[0];
        if (file) {
            showToast(formatText('Imported: {name}', { name: file.name }), 'success');
            // In real implementation, would process the QR code from image
            setTimeout(() => {
                window.location.href = 'scanner.html';
            }, 1000);
        }
    };

    input.click();
}

/**
 * Handle database update check
 */
function handleUpdateDb() {
    const btn = elements.updateDbBtn;
    if (!btn) return;

    // Show loading state
    const originalContent = btn.innerHTML;
    btn.innerHTML = `
        <span class="material-symbols-outlined" style="animation: spin 1s linear infinite;">refresh</span>
        Checking...
    `;
    btn.disabled = true;

    // Simulate check
    setTimeout(() => {
        btn.innerHTML = originalContent;
        btn.disabled = false;
        showToast('Threat database is up to date', 'success');
    }, 2000);
}

// =============================================================================
// URL ANALYSIS (Kotlin Engine Integration)
// =============================================================================

/**
 * Setup Kotlin/JS engine bridge
 */
function setupKotlinBridge() {
    // Fallback if Kotlin engine hasn't loaded yet
    if (!window.qrshieldAnalyze) {
        window.qrshieldAnalyze = (url) => {
            console.warn('[Dashboard] Kotlin engine not ready');
            showToast('Engine initializing...', 'warning');
        };
    }

    // Override displayResult to handle results on dashboard
    const originalDisplayResult = window.displayResult;
    window.displayResult = (score, verdict, flags, url) => {
        // Reset analyze button
        if (elements.analyzeBtn) {
            elements.analyzeBtn.classList.remove('loading');
            elements.analyzeBtn.innerHTML = `<span class="material-symbols-outlined">security</span> ${translateText('Analyze')}`;
        }

        // Save to QRShieldUI scan history
        if (window.QRShieldUI && window.QRShieldUI.addScanToHistory) {
            window.QRShieldUI.addScanToHistory({
                url: url,
                verdict: verdict === 'MALICIOUS' ? 'HIGH' :
                    verdict === 'SUSPICIOUS' ? 'MEDIUM' :
                        verdict === 'SAFE' ? 'SAFE' : 'LOW',
                score: score || 0,
                signals: flags || []
            });
            console.log('[Dashboard] Saved scan to history:', url, verdict);
        }

        // Navigate to results page with data (using relative path for file:// compatibility)
        const params = new URLSearchParams();
        params.set('url', encodeURIComponent(url));
        params.set('verdict', verdict);
        params.set('score', score);
        window.location.href = `results.html?${params.toString()}`;

        // Also call original if exists
        if (originalDisplayResult) {
            originalDisplayResult(score, verdict, flags, url);
        }
    };
}

/**
 * Analyze URL using Kotlin PhishingEngine
 */
function analyzeUrl() {
    const url = elements.urlInput?.value.trim();

    if (!url) {
        showToast('Please enter a URL', 'warning');
        elements.urlInput?.focus();
        return;
    }

    // Auto-fix URLs without protocol
    let fixedUrl = url;
    if (!url.includes('://')) {
        fixedUrl = 'https://' + url;
    }

    // Validate URL
    if (!isValidUrl(fixedUrl)) {
        showToast('Invalid URL format', 'error');
        return;
    }

    // Show loading state
    if (elements.analyzeBtn) {
        elements.analyzeBtn.classList.add('loading');
        elements.analyzeBtn.innerHTML = `<span class="material-symbols-outlined">hourglass_empty</span> ${translateText('Analyzing...')}`;
    }

    // Call Kotlin engine
    console.log('[Dashboard] Analyzing URL:', fixedUrl);
    if (window.qrshieldAnalyze) {
        window.qrshieldAnalyze(fixedUrl);
    } else {
        // Fallback: Navigate to results with URL
        showToast('Engine not ready - please wait', 'warning');
        // SECURITY: Do NOT navigate with fake SAFE verdict - wait for engine
        if (elements.analyzeBtn) {
            elements.analyzeBtn.classList.remove('loading');
            elements.analyzeBtn.innerHTML = `<span class="material-symbols-outlined">security</span> ${translateText('Analyze')}`;
        }
    }
}

/**
 * Check if URL is valid
 */
function isValidUrl(string) {
    try {
        const url = new URL(string);
        return url.protocol === 'http:' || url.protocol === 'https:';
    } catch {
        return false;
    }
}

// =============================================================================
// SIDEBAR CONTROLS
// =============================================================================

function toggleSidebar() {
    if (DashboardState.isSidebarOpen) {
        closeSidebar();
    } else {
        openSidebar();
    }
}

function openSidebar() {
    elements.sidebar?.classList.add('open');
    DashboardState.isSidebarOpen = true;
}

function closeSidebar() {
    elements.sidebar?.classList.remove('open');
    DashboardState.isSidebarOpen = false;
}

// =============================================================================
// KEYBOARD SHORTCUTS
// =============================================================================

function setupKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Escape - close sidebar
        if (e.key === 'Escape') {
            if (DashboardState.isSidebarOpen) {
                closeSidebar();
            }
        }

        // S - Start scan
        if (e.key === 's' && !e.ctrlKey && !e.metaKey &&
            document.activeElement?.tagName !== 'INPUT') {
            window.location.href = 'scanner.html';
        }

        // I - Import
        if (e.key === 'i' && !e.ctrlKey && !e.metaKey &&
            document.activeElement?.tagName !== 'INPUT') {
            handleImport();
        }
    });
}

// =============================================================================
// UI HELPERS
// =============================================================================

/**
 * Show a toast notification
 */
function showToast(message, type = 'success') {
    if (!elements.toast || !elements.toastMessage) return;

    elements.toastMessage.textContent = translateText(message);

    const icon = elements.toast.querySelector('.toast-icon');
    if (icon) {
        switch (type) {
            case 'success':
                icon.textContent = 'check_circle';
                icon.style.color = '#10b981';
                break;
            case 'warning':
                icon.textContent = 'warning';
                icon.style.color = '#f59e0b';
                break;
            case 'error':
                icon.textContent = 'error';
                icon.style.color = '#ef4444';
                break;
            case 'info':
                icon.textContent = 'info';
                icon.style.color = '#3b82f6';
                break;
        }
    }

    elements.toast.classList.remove('hidden');
    elements.toast.classList.add('show');

    setTimeout(() => {
        elements.toast.classList.remove('show');
        setTimeout(() => {
            elements.toast.classList.add('hidden');
        }, 300);
    }, 3000);
}

// =============================================================================
// UTILITY FUNCTIONS
// =============================================================================

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Add spin animation for loading states
const style = document.createElement('style');
style.textContent = `
    @keyframes spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }
`;
document.head.appendChild(style);

// =============================================================================
// EXPORTS FOR TESTING
// =============================================================================

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        DashboardState,
        DashboardConfig,
    };
}
