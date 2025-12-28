/**
 * QR-SHIELD Threat Analysis Page Controller
 * 
 * Handles threat display, attack breakdown interactions,
 * action buttons, and dynamic data population.
 * 
 * @author QR-SHIELD Team
 * @version 2.4.1
 */

// =============================================================================
// CONFIGURATION
// =============================================================================

const ThreatConfig = {
    version: '2.4.1',
    storageKey: 'qrshield_threat_data',
};

// =============================================================================
// STATE
// =============================================================================

const ThreatState = {
    threatData: null,
    isSidebarOpen: false,
};

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

// =============================================================================
// DOM ELEMENTS
// =============================================================================

const elements = {
    // Sidebar
    sidebar: null,
    menuToggle: null,

    // Hero
    threatHero: null,
    threatTitle: null,
    threatBadge: null,
    threatDescription: null,
    threatTags: null,
    confidenceScore: null,
    progressFill: null,

    // Meta
    scanId: null,
    scanIdLabel: null,
    scanTime: null,

    // Buttons
    blockBtn: null,
    exportReportBtn: null,
    notificationBtn: null,

    // Toast
    toast: null,
    toastMessage: null,
};

// =============================================================================
// THREAT LEVELS
// =============================================================================

const ThreatLevels = {
    HIGH: {
        title: 'HIGH RISK DETECTED',
        badge: 'Dangerous',
        badgeClass: 'danger',
        icon: 'gpp_maybe',
        description: 'The scanned QR code contains malicious indicators associated with phishing and credential harvesting. Do not proceed to the target URL.',
    },
    MEDIUM: {
        title: 'SUSPICIOUS ACTIVITY',
        badge: 'Warning',
        badgeClass: 'warning',
        icon: 'warning',
        description: 'The scanned QR code shows suspicious patterns that may indicate potential security risks. Proceed with caution.',
    },
    LOW: {
        title: 'MINOR CONCERNS',
        badge: 'Caution',
        badgeClass: 'caution',
        icon: 'info',
        description: 'Some minor security concerns were identified. The link appears mostly safe but review recommended.',
    },
    SAFE: {
        title: 'VERIFIED SAFE',
        badge: 'Safe',
        badgeClass: 'safe',
        icon: 'verified',
        description: 'No security threats were detected. The QR code leads to a verified safe destination.',
    },
};

// =============================================================================
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('[QR-SHIELD Threat] Initializing v' + ThreatConfig.version);

    // Cache DOM elements
    cacheElements();

    // Load threat data
    loadThreatData();

    // Setup event listeners
    setupEventListeners();

    // Render UI
    renderUI();

    // Render scan history list
    renderScanHistory();

    window.qrshieldApplyTranslations?.(document.body);

    console.log('[QR-SHIELD Threat] Ready');
});

/**
 * Cache frequently accessed DOM elements
 */
function cacheElements() {
    elements.sidebar = document.getElementById('sidebar');
    elements.menuToggle = document.getElementById('menuToggle');
    elements.threatHero = document.getElementById('threatHero');
    elements.threatTitle = document.getElementById('threatTitle');
    elements.threatBadge = document.getElementById('threatBadge');
    elements.threatDescription = document.getElementById('threatDescription');
    elements.threatTags = document.getElementById('threatTags');
    elements.confidenceScore = document.getElementById('confidenceScore');
    elements.progressFill = document.getElementById('progressFill');
    elements.scanId = document.getElementById('scanId');
    elements.scanIdLabel = document.getElementById('scanIdLabel');
    elements.scanTime = document.getElementById('scanTime');
    elements.blockBtn = document.getElementById('blockBtn');
    elements.exportReportBtn = document.getElementById('exportReportBtn');
    elements.notificationBtn = document.getElementById('notificationBtn');
    elements.toast = document.getElementById('toast');
    elements.toastMessage = document.getElementById('toastMessage');
}

/**
 * Setup all event listeners
 */
function setupEventListeners() {
    // Mobile menu toggle
    elements.menuToggle?.addEventListener('click', toggleSidebar);

    // Block button
    elements.blockBtn?.addEventListener('click', blockAndReport);

    // Export Report button
    elements.exportReportBtn?.addEventListener('click', exportThreatReport);

    // Notification button
    elements.notificationBtn?.addEventListener('click', () => {
        showToast('No new notifications', 'info');
    });

    // Clear history button
    const clearHistoryBtn = document.getElementById('clearHistoryBtn');
    if (clearHistoryBtn) {
        clearHistoryBtn.addEventListener('click', () => {
            if (window.QRShieldUI) {
                window.QRShieldUI.clearScanHistory();
                renderScanHistory();
                showToast('Scan history cleared', 'success');
            }
        });
    }

    // Keyboard shortcuts
    setupKeyboardShortcuts();
}

// =============================================================================
// DATA MANAGEMENT
// =============================================================================

/**
 * Load threat data from URL params or localStorage
 */
function loadThreatData() {
    // Try to get from URL params first
    const params = new URLSearchParams(window.location.search);
    const urlParam = params.get('url');
    const scoreParam = params.get('score');
    const verdictParam = params.get('verdict');

    if (urlParam) {
        const score = parseInt(scoreParam) || 98;
        ThreatState.threatData = {
            url: decodeURIComponent(urlParam),
            score: score,
            verdict: verdictParam || (score >= 70 ? 'HIGH' : score >= 40 ? 'MEDIUM' : 'LOW'),
            scanId: generateScanId(),
            timestamp: Date.now(),
            attacks: getDemoAttacks(),
        };
    } else {
        // Try localStorage
        try {
            const stored = localStorage.getItem(ThreatConfig.storageKey);
            if (stored) {
                ThreatState.threatData = JSON.parse(stored);
            }
        } catch (e) {
            console.error('[Threat] Failed to load threat data:', e);
        }
    }

    // If still no data, use demo data
    if (!ThreatState.threatData) {
        ThreatState.threatData = getDemoData();
    }
}

/**
 * Get demo threat data
 */
function getDemoData() {
    return {
        url: 'http://xn--secure-bankng-87b.com/login',
        score: 98.5,
        verdict: 'HIGH',
        scanId: 'scan_2023_10_24_af92',
        timestamp: Date.now(),
        attacks: getDemoAttacks(),
        tags: ['Phishing Attempt', 'Obfuscated Script', 'Homograph Attack'],
    };
}

/**
 * Get demo attack details
 */
function getDemoAttacks() {
    return [
        {
            type: 'homograph',
            title: 'Homograph / IDN Attack',
            description: 'Cyrillic characters mimicking Latin alphabet detected.',
            visual: 'secure-banking.com',
            actual: 'xn--secure-bankng-87b.com',
            explanation: "The domain uses the Cyrillic 'а' (U+0430) instead of Latin 'a' (U+0061).",
        },
        {
            type: 'redirect',
            title: 'Suspicious Redirect Chain',
            description: '3 hops detected involving known URL shorteners.',
            chain: [
                { label: 'QR Code Scan', url: 'http://bit.ly/3x891', status: 'start' },
                { label: 'Intermediate Hop', url: 'http://tracker-service-cloud.net/ref?id=99', status: 'warning' },
                { label: 'Final Destination', url: 'http://xn--secure-bankng-87b.com/login', status: 'danger' },
            ],
        },
        {
            type: 'obfuscation',
            title: 'Obfuscated JavaScript',
            description: 'High entropy string detected in URL parameters.',
            code: "<script>eval(function(p,a,c,k,e,d){e=function(c){return c};if(!''.replace(/^/,String)...",
        },
    ];
}

// =============================================================================
// UI RENDERING
// =============================================================================

/**
 * Render all UI components
 */
function renderUI() {
    const data = ThreatState.threatData;
    if (!data) return;

    const level = ThreatLevels[data.verdict] || ThreatLevels.HIGH;

    // Update hero section color class
    if (elements.threatHero) {
        // Remove all state classes
        elements.threatHero.classList.remove('safe', 'warning', 'caution', 'danger');

        // Add appropriate class based on verdict
        switch (data.verdict) {
            case 'SAFE':
                elements.threatHero.classList.add('safe');
                break;
            case 'MEDIUM':
                elements.threatHero.classList.add('warning');
                break;
            case 'LOW':
                elements.threatHero.classList.add('caution');
                break;
            case 'HIGH':
            default:
                // Default styling is danger (red), no class needed
                break;
        }
    }

    // Update title
    if (elements.threatTitle) {
        elements.threatTitle.textContent = translateText(level.title);
    }

    // Update badge
    if (elements.threatBadge) {
        elements.threatBadge.textContent = translateText(level.badge);
    }

    // Update description
    if (elements.threatDescription) {
        elements.threatDescription.textContent = translateText(level.description);
    }

    // Update score
    if (elements.confidenceScore) {
        elements.confidenceScore.textContent = `${data.score}%`;
    }

    // Update progress bar
    if (elements.progressFill) {
        elements.progressFill.style.width = `${data.score}%`;
    }

    // Update scan ID
    if (elements.scanId) {
        elements.scanId.textContent = formatText('Scan # {id}', { id: data.scanId.slice(-7).toUpperCase() });
    }

    if (elements.scanIdLabel) {
        elements.scanIdLabel.textContent = formatText('ID: {id}', { id: data.scanId });
    }

    // Update scan time
    if (elements.scanTime) {
        elements.scanTime.textContent = formatScanTime(data.timestamp);
    }
}

/**
 * Format scan timestamp
 */
function formatScanTime(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();

    let locale = 'en-AU';
    if (window.qrshieldGetLanguageCode) {
        locale = window.qrshieldGetLanguageCode();
    }

    const timeStr = date.toLocaleTimeString(locale, {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    });

    if (isToday) {
        return formatText('Today, {time}', { time: timeStr });
    }

    const dateStr = date.toLocaleDateString(locale, {
        month: 'short',
        day: 'numeric'
    });

    return formatText('{date}, {time}', { date: dateStr, time: timeStr });
}

/**
 * Render the scan history list
 */
function renderScanHistory() {
    const historyContainer = document.getElementById('historyList');
    if (!historyContainer) return;

    // Wait for QRShieldUI
    if (!window.QRShieldUI) {
        setTimeout(renderScanHistory, 100);
        return;
    }

    const history = window.QRShieldUI.getScanHistory();

    if (history.length === 0) {
        historyContainer.innerHTML = `
            <div class="history-empty">
                <span class="material-symbols-outlined">history</span>
                <p>No scans yet. Scan a QR code to see it here.</p>
            </div>
        `;
        window.qrshieldApplyTranslations?.(historyContainer);
        return;
    }

    // Render history items (show last 10)
    const recentHistory = history.slice(0, 10);
    historyContainer.innerHTML = recentHistory.map(scan => {
        const verdictClass = getVerdictClass(scan.verdict);
        const verdictLabel = getVerdictLabel(scan.verdict);
        const icon = getVerdictIcon(scan.verdict);

        return `
            <div class="history-item" data-scan-id="${scan.id}">
                <div class="history-icon ${verdictClass}">
                    <span class="material-symbols-outlined">${icon}</span>
                </div>
                <div class="history-content">
                    <div class="history-url">${truncateUrl(scan.url)}</div>
                    <div class="history-meta">
                        <span class="history-badge ${verdictClass}">${verdictLabel}</span>
                        <span>${formatScanTime(scan.timestamp)}</span>
                        ${scan.score ? `<span class="history-score">${scan.score}%</span>` : ''}
                    </div>
                </div>
                <span class="material-symbols-outlined" style="color: #64748b;">chevron_right</span>
            </div>
        `;
    }).join('');

    window.qrshieldApplyTranslations?.(historyContainer);

    // Add click handlers
    historyContainer.querySelectorAll('.history-item').forEach(item => {
        item.addEventListener('click', () => {
            const scanId = item.dataset.scanId;
            viewScanDetails(scanId);
        });
    });
}

function getVerdictClass(verdict) {
    switch (verdict) {
        case 'HIGH': return 'danger';
        case 'MEDIUM': return 'warning';
        case 'LOW':
        case 'SAFE': return 'safe';
        default: return 'safe';
    }
}

function getVerdictLabel(verdict) {
    switch (verdict) {
        case 'HIGH': return translateText('High Risk');
        case 'MEDIUM': return translateText('Warning');
        case 'LOW': return translateText('Low Risk');
        case 'SAFE': return translateText('Safe');
        default: return translateText('Unknown');
    }
}

function getVerdictIcon(verdict) {
    switch (verdict) {
        case 'HIGH': return 'gpp_bad';
        case 'MEDIUM': return 'warning';
        case 'LOW':
        case 'SAFE': return 'verified_user';
        default: return 'help';
    }
}

function truncateUrl(url) {
    if (!url) return translateText('Unknown URL');
    if (url.length > 50) {
        return url.substring(0, 47) + '...';
    }
    return url;
}

function viewScanDetails(scanId) {
    if (!window.QRShieldUI) return;

    const scan = window.QRShieldUI.getScanById(scanId);
    if (!scan) return;

    // Map verdict to results.html format
    let resultVerdict = 'UNKNOWN';
    switch (scan.verdict) {
        case 'SAFE':
            resultVerdict = 'SAFE';
            break;
        case 'LOW':
            resultVerdict = 'SAFE';
            break;
        case 'MEDIUM':
            resultVerdict = 'SUSPICIOUS';
            break;
        case 'HIGH':
            resultVerdict = 'MALICIOUS';
            break;
    }

    // Navigate to results.html with scan details
    const params = new URLSearchParams({
        url: scan.url,
        verdict: resultVerdict,
        score: scan.score || 50
    });

    window.location.href = `results.html?${params.toString()}`;
}

// =============================================================================
// ACTIONS
// =============================================================================

/**
 * Block and report the threat
 */
function blockAndReport() {
    const data = ThreatState.threatData;

    // Save to blocklist
    try {
        const blocklistKey = 'qrshield_blocklist';
        const blocklist = JSON.parse(localStorage.getItem(blocklistKey) || '[]');

        // Extract domain from URL
        const domain = extractDomain(data.url);

        if (!blocklist.includes(domain)) {
            blocklist.push(domain);
            localStorage.setItem(blocklistKey, JSON.stringify(blocklist));
        }
    } catch (e) {
        console.error('[Threat] Failed to add to blocklist:', e);
    }

    showToast('Threat blocked and reported', 'success');

    // Visual feedback on button
    if (elements.blockBtn) {
        elements.blockBtn.innerHTML = `
            <span class="material-symbols-outlined">check</span>
            Blocked
        `;
        elements.blockBtn.disabled = true;
        elements.blockBtn.style.opacity = '0.7';
    }
}

/**
 * Export threat report
 */
function exportThreatReport() {
    // Navigate to export page
    window.location.href = 'export.html';
}

// =============================================================================
// SIDEBAR CONTROLS
// =============================================================================

function toggleSidebar() {
    if (ThreatState.isSidebarOpen) {
        closeSidebar();
    } else {
        openSidebar();
    }
}

function openSidebar() {
    elements.sidebar?.classList.add('open');
    ThreatState.isSidebarOpen = true;
}

function closeSidebar() {
    elements.sidebar?.classList.remove('open');
    ThreatState.isSidebarOpen = false;
}

// =============================================================================
// KEYBOARD SHORTCUTS
// =============================================================================

function setupKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Escape - go back
        if (e.key === 'Escape') {
            if (ThreatState.isSidebarOpen) {
                closeSidebar();
            } else {
                window.history.back();
            }
        }

        // B - Block
        if (e.key === 'b' && !e.ctrlKey && !e.metaKey &&
            document.activeElement?.tagName !== 'INPUT') {
            blockAndReport();
        }

        // E - Export
        if (e.key === 'e' && !e.ctrlKey && !e.metaKey &&
            document.activeElement?.tagName !== 'INPUT') {
            exportThreatReport();
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

function generateScanId() {
    const date = new Date();
    const dateStr = date.toISOString().slice(0, 10).replace(/-/g, '_');
    const randomStr = Math.random().toString(36).substring(2, 6);
    return `scan_${dateStr}_${randomStr}`;
}

function extractDomain(url) {
    try {
        const urlObj = new URL(url);
        return urlObj.hostname;
    } catch (e) {
        // If URL parsing fails, try basic extraction
        const match = url.match(/^(?:https?:\/\/)?([^\/]+)/i);
        return match ? match[1] : url;
    }
}

// =============================================================================
// EXPORTS FOR TESTING
// =============================================================================

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        ThreatState,
        ThreatConfig,
        ThreatLevels,
        loadThreatData,
        blockAndReport,
        exportThreatReport,
    };
}
