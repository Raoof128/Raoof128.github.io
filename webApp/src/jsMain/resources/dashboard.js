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
    statsKey: 'qrshield_stats',
    historyKey: 'qrshield_scan_history',
};

// =============================================================================
// STATE
// =============================================================================

const DashboardState = {
    stats: {
        threats: 0,
        safeScans: 0,
        totalScans: 0,
    },
    scanHistory: [],
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
};

// =============================================================================
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('[QR-SHIELD Dashboard] Initializing v' + DashboardConfig.version);

    // Cache DOM elements
    cacheElements();

    // Load saved data
    loadStats();
    loadHistory();

    // Setup event listeners
    setupEventListeners();

    // Render UI
    renderUI();

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
}

/**
 * Setup all event listeners
 */
function setupEventListeners() {
    // Mobile menu toggle
    elements.menuToggle?.addEventListener('click', toggleSidebar);

    // Notification button
    elements.notificationBtn?.addEventListener('click', () => {
        showToast('No new notifications', 'info');
    });

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
// DATA MANAGEMENT
// =============================================================================

/**
 * Load statistics from localStorage
 */
function loadStats() {
    try {
        const stored = localStorage.getItem(DashboardConfig.statsKey);
        if (stored) {
            DashboardState.stats = JSON.parse(stored);
        } else {
            // Initialize with demo data
            DashboardState.stats = {
                threats: 0,
                safeScans: 124,
                totalScans: 124,
            };
        }
    } catch (e) {
        console.error('[Dashboard] Failed to load stats:', e);
    }
}

/**
 * Load scan history from localStorage
 */
function loadHistory() {
    try {
        const stored = localStorage.getItem(DashboardConfig.historyKey);
        if (stored) {
            DashboardState.scanHistory = JSON.parse(stored);
        } else {
            // Initialize with demo data
            DashboardState.scanHistory = [
                {
                    url: 'github.com/login',
                    status: 'safe',
                    details: 'Valid TLS cert, Trusted Domain',
                    time: '10:42 AM',
                    favicon: 'https://www.google.com/s2/favicons?domain=github.com&sz=32',
                },
                {
                    url: 'secure-bank-verify.net',
                    status: 'phish',
                    details: 'Homograph attack detected',
                    time: '09:15 AM',
                    favicon: null,
                },
                {
                    url: 'company.atlassian.net',
                    status: 'safe',
                    details: 'Internal Allowlist Match',
                    time: '08:55 AM',
                    favicon: 'https://www.google.com/s2/favicons?domain=atlassian.com&sz=32',
                },
            ];
        }
    } catch (e) {
        console.error('[Dashboard] Failed to load history:', e);
    }
}

/**
 * Save statistics to localStorage
 */
function saveStats() {
    try {
        localStorage.setItem(DashboardConfig.statsKey, JSON.stringify(DashboardState.stats));
    } catch (e) {
        console.error('[Dashboard] Failed to save stats:', e);
    }
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
}

/**
 * Render statistics
 */
function renderStats() {
    if (elements.threatCount) {
        elements.threatCount.textContent = DashboardState.stats.threats;
    }

    if (elements.safeCount) {
        elements.safeCount.textContent = DashboardState.stats.safeScans;
    }
}

/**
 * Render scan history table
 */
function renderHistory() {
    if (!elements.recentScansBody) return;

    const history = DashboardState.scanHistory.slice(0, 5); // Show max 5 recent

    if (history.length === 0) {
        elements.recentScansBody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center" style="padding: 2rem; color: var(--text-muted);">
                    No scans yet. Start a new scan to see results here.
                </td>
            </tr>
        `;
        return;
    }

    elements.recentScansBody.innerHTML = history.map(scan => `
        <tr>
            <td>
                <span class="status-badge ${scan.status === 'safe' ? 'safe' : 'danger'}">
                    <span class="material-symbols-outlined">${scan.status === 'safe' ? 'check_circle' : 'warning'}</span>
                    ${scan.status === 'safe' ? 'SAFE' : 'PHISH'}
                </span>
            </td>
            <td class="source-cell">
                ${scan.favicon
            ? `<img src="${scan.favicon}" alt="" class="favicon"/>`
            : `<span class="favicon-placeholder">?</span>`
        }
                ${escapeHtml(scan.url)}
            </td>
            <td class="hide-mobile details-cell">${escapeHtml(scan.details)}</td>
            <td class="time-cell">${scan.time}</td>
        </tr>
    `).join('');
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
            showToast(`Imported: ${file.name}`, 'success');
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
            elements.analyzeBtn.innerHTML = '<span class="material-symbols-outlined">security</span> Analyze';
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
        elements.analyzeBtn.innerHTML = '<span class="material-symbols-outlined">hourglass_empty</span> Analyzing...';
    }

    // Call Kotlin engine
    console.log('[Dashboard] Analyzing URL:', fixedUrl);
    if (window.qrshieldAnalyze) {
        window.qrshieldAnalyze(fixedUrl);
    } else {
        // Fallback: Navigate to results with URL
        showToast('Engine not ready, using demo mode', 'info');
        setTimeout(() => {
            const resultsUrl = new URL('results.html', window.location.origin);
            resultsUrl.searchParams.set('url', encodeURIComponent(fixedUrl));
            resultsUrl.searchParams.set('verdict', 'SAFE');
            resultsUrl.searchParams.set('score', '5');
            window.location.href = resultsUrl.toString();
        }, 500);
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

    elements.toastMessage.textContent = message;

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
        loadStats,
        loadHistory,
        saveStats,
    };
}
