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
    UNKNOWN: {
        title: 'NO DATA AVAILABLE',
        badge: 'No Data',
        badgeClass: 'unknown',
        icon: 'help_outline',
        description: 'No scan data available. Please scan a QR code or enter a URL to analyze.',
    },
};

// =============================================================================
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('[QR-SHIELD Threat] Initializing v' + ThreatConfig.version);

    // Cache DOM elements
    cacheElements();

    // Load threat data (may need to wait for QRShieldUI)
    setTimeout(() => {
        loadThreatData();

        // Render UI
        renderUI();

        // Render attack cards with real data
        renderAttackCards();

        // Show demo mode badge if using demo data
        renderDemoModeBadge();

        // Render scan history list
        renderScanHistory();

        window.qrshieldApplyTranslations?.(document.body);

        console.log('[QR-SHIELD Threat] Ready');
    }, 100);

    // Setup event listeners
    setupEventListeners();
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
 * Load threat data from URL params, localStorage, or scan history
 */
function loadThreatData() {
    // Try to get from URL params first
    const params = new URLSearchParams(window.location.search);
    const urlParam = params.get('url');
    const scoreParam = params.get('score');
    const verdictParam = params.get('verdict');
    const scanIdParam = params.get('scanId');

    // 1. Try URL parameters
    if (urlParam) {
        const score = parseInt(scoreParam) || 50;
        ThreatState.threatData = {
            url: decodeURIComponent(urlParam),
            score: score,
            verdict: verdictParam || mapScoreToVerdict(score),
            scanId: scanIdParam || generateScanId(),
            timestamp: Date.now(),
            attacks: generateAttacksFromUrl(decodeURIComponent(urlParam), verdictParam, []),
            isDemo: false,
        };
        return;
    }

    // 2. Try scanId from URL to look up in history
    if (scanIdParam && window.QRShieldUI) {
        const scan = window.QRShieldUI.getScanById(scanIdParam);
        if (scan) {
            ThreatState.threatData = {
                url: scan.url,
                score: scan.score || 50,
                verdict: mapVerdictToLevel(scan.verdict),
                scanId: scan.id,
                timestamp: scan.timestamp,
                attacks: generateAttacksFromUrl(scan.url, scan.verdict, scan.signals || []),
                signals: scan.signals || [],
                isDemo: false,
            };
            return;
        }
    }

    // 3. Try localStorage for last threat
    try {
        const stored = localStorage.getItem(ThreatConfig.storageKey);
        if (stored) {
            ThreatState.threatData = JSON.parse(stored);
            ThreatState.threatData.isDemo = false;
            return;
        }
    } catch (e) {
        console.error('[Threat] Failed to load threat data:', e);
    }

    // 4. Try to get the most recent scan from history
    if (window.QRShieldUI) {
        const history = window.QRShieldUI.getScanHistory();
        if (history && history.length > 0) {
            const recentScan = history[0];
            ThreatState.threatData = {
                url: recentScan.url,
                score: recentScan.score || 50,
                verdict: mapVerdictToLevel(recentScan.verdict),
                scanId: recentScan.id,
                timestamp: recentScan.timestamp,
                attacks: generateAttacksFromUrl(recentScan.url, recentScan.verdict, recentScan.signals || []),
                signals: recentScan.signals || [],
                isDemo: false,
            };
            return;
        }
    }

    // 5. No real data available - show EMPTY STATE (NOT fake demo)
    ThreatState.threatData = getEmptyStateData();
    console.log('[Threat] No scan data found, showing empty state (NO fake demo)');
}

/**
 * Get empty state data when no real scan is available
 * NEVER fabricates fake security outcomes - this is a non-negotiable rule
 */
function getEmptyStateData() {
    return {
        url: '',
        score: 0,
        verdict: 'UNKNOWN',
        scanId: 'no_data',
        timestamp: Date.now(),
        attacks: [],
        tags: [],
        isEmpty: true,
    };
}

/**
 * Legacy demo data function - DEPRECATED
 * Kept for reference but should never be called in production
 * @deprecated Use getEmptyStateData() instead
 */
function getDemoData() {
    console.warn('[Security] getDemoData() called - this should not happen in production');
    return getEmptyStateData();
}

/**
 * Legacy demo attacks function - DEPRECATED
 * @deprecated Demo attacks are no longer shown
 */
function getDemoAttacks() {
    console.warn('[Security] getDemoAttacks() called - this should not happen in production');
    return [];
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

    // Update the URL in meta section
    const sourceValue = document.querySelector('.meta-item .meta-value');
    if (sourceValue && data.url) {
        const domain = extractDomain(data.url);
        sourceValue.innerHTML = `
            <span class="material-symbols-outlined">link</span>
            ${domain}
        `;
    }
}

/**
 * Render attack cards dynamically based on real threat data
 */
function renderAttackCards() {
    const data = ThreatState.threatData;
    if (!data || !data.attacks || data.attacks.length === 0) return;

    const mainColumn = document.querySelector('.main-column');
    if (!mainColumn) return;

    // Find existing attack cards container or create one
    let attackContainer = document.getElementById('attackCardsContainer');
    if (!attackContainer) {
        attackContainer = document.createElement('div');
        attackContainer.id = 'attackCardsContainer';

        // Insert after section header
        const sectionHeader = mainColumn.querySelector('.section-header');
        if (sectionHeader && sectionHeader.nextSibling) {
            mainColumn.insertBefore(attackContainer, sectionHeader.nextSibling);
        } else {
            mainColumn.appendChild(attackContainer);
        }
    }

    // If we have real data (not demo), rebuild the attack cards
    if (!data.isDemo) {
        // Hide the hardcoded demo cards
        const demoCards = mainColumn.querySelectorAll('details.attack-card');
        demoCards.forEach(card => card.style.display = 'none');

        // Render real attack cards
        attackContainer.innerHTML = data.attacks.map((attack, index) => {
            const cardClass = getAttackCardClass(attack.type);
            const icon = getAttackIcon(attack.type);

            let content = '';

            if (attack.type === 'homograph' && attack.visual && attack.actual) {
                content = `
                    <div class="domain-comparison">
                        <div class="domain-item">
                            <span class="domain-label">${translateText('Visual Appearance')}</span>
                            <span class="domain-value safe">${attack.visual}</span>
                        </div>
                        <div class="domain-item">
                            <span class="domain-label">${translateText('Actual Domain')}</span>
                            <span class="domain-value danger">${attack.actual}</span>
                        </div>
                    </div>
                    ${attack.explanation ? `<p class="attack-explanation">${attack.explanation}</p>` : ''}
                `;
            } else if (attack.type === 'redirect' && attack.chain) {
                content = `
                    <ol class="redirect-timeline">
                        ${attack.chain.map(hop => `
                            <li class="timeline-item ${hop.status}">
                                <span class="timeline-dot${hop.status === 'danger' ? ' pulse' : ''}"></span>
                                <div class="timeline-content">
                                    <h5>${hop.label}</h5>
                                    <code${hop.status === 'danger' ? ' class="danger"' : ''}>${hop.url}</code>
                                </div>
                            </li>
                        `).join('')}
                    </ol>
                `;
            } else if (attack.type === 'obfuscation' && attack.code) {
                content = `
                    <div class="code-block">
                        <code>${escapeHtml(attack.code)}</code>
                    </div>
                `;
            } else if (attack.description) {
                content = `<p class="attack-explanation">${attack.description}</p>`;
            }

            return `
                <details class="attack-card ${cardClass}" ${index === 0 ? 'open' : ''}>
                    <summary class="attack-header">
                        <div class="attack-title">
                            <span class="attack-icon">
                                <span class="material-symbols-outlined">${icon}</span>
                            </span>
                            <div class="attack-info">
                                <h4>${attack.title}</h4>
                                <p>${attack.description}</p>
                            </div>
                        </div>
                        <span class="material-symbols-outlined expand-icon">expand_more</span>
                    </summary>
                    <div class="attack-content">
                        ${content}
                    </div>
                </details>
            `;
        }).join('');
    }
}

function getAttackCardClass(type) {
    switch (type) {
        case 'homograph': return 'blue';
        case 'redirect': return 'warning';
        case 'obfuscation': return 'primary';
        case 'suspicious_tld': return 'danger';
        case 'heuristic': return 'primary';
        case 'brand_impersonation': return 'danger';
        case 'phishing': return 'danger';
        case 'suspicious_keywords': return 'warning';
        default: return 'primary';
    }
}

function getAttackIcon(type) {
    switch (type) {
        case 'homograph': return 'abc';
        case 'redirect': return 'alt_route';
        case 'obfuscation': return 'javascript';
        case 'suspicious_tld': return 'domain';
        case 'heuristic': return 'psychology';
        case 'brand_impersonation': return 'storefront';
        case 'phishing': return 'phishing';
        case 'suspicious_keywords': return 'warning';
        default: return 'security';
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
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

/**
 * Map score to threat level
 */
function mapScoreToVerdict(score) {
    if (score >= 70) return 'HIGH';
    if (score >= 40) return 'MEDIUM';
    if (score >= 20) return 'LOW';
    return 'SAFE';
}

/**
 * Map engine verdict to threat level
 */
function mapVerdictToLevel(verdict) {
    switch (verdict) {
        case 'MALICIOUS': return 'HIGH';
        case 'SUSPICIOUS': return 'MEDIUM';
        case 'SAFE': return 'SAFE';
        case 'HIGH': return 'HIGH';
        case 'MEDIUM': return 'MEDIUM';
        case 'LOW': return 'LOW';
        default: return 'UNKNOWN'; // NEVER default to SAFE - security rule
    }
}

/**
 * Generate attack analysis from actual URL data and engine signals
 */
function generateAttacksFromUrl(url, verdict, signals = []) {
    const attacks = [];
    const domain = extractDomain(url);

    // If we have real signals from the engine, convert them to attack cards
    if (signals && signals.length > 0) {
        signals.forEach(signal => {
            // Parse the signal string to extract useful info
            const signalLower = signal.toLowerCase();

            if (signalLower.includes('punycode') || signalLower.includes('homograph') || signalLower.includes('idn')) {
                attacks.push({
                    type: 'homograph',
                    title: translateText('Homograph / IDN Attack'),
                    description: signal,
                    visual: domain.replace(/^xn--/, '').replace(/-[a-z0-9]+$/, ''),
                    actual: domain,
                    explanation: translateText('This domain uses international characters that may mimic legitimate domains.'),
                });
            } else if (signalLower.includes('shortener') || signalLower.includes('redirect') || signalLower.includes('bit.ly') || signalLower.includes('tracking')) {
                attacks.push({
                    type: 'redirect',
                    title: translateText('Redirect/Shortener Detected'),
                    description: signal,
                    chain: [
                        { label: translateText('Scanned URL'), url: url, status: 'warning' },
                    ],
                });
            } else if (signalLower.includes('suspicious') && signalLower.includes('tld')) {
                attacks.push({
                    type: 'suspicious_tld',
                    title: translateText('Suspicious TLD'),
                    description: signal,
                });
            } else if (signalLower.includes('brand') || signalLower.includes('impersonat')) {
                attacks.push({
                    type: 'brand_impersonation',
                    title: translateText('Brand Impersonation'),
                    description: signal,
                });
            } else if (signalLower.includes('entropy') || signalLower.includes('obfuscat') || signalLower.includes('encoded')) {
                attacks.push({
                    type: 'obfuscation',
                    title: translateText('Suspicious Encoding'),
                    description: signal,
                });
            } else if (signalLower.includes('phish') || signalLower.includes('credential')) {
                attacks.push({
                    type: 'phishing',
                    title: translateText('Phishing Indicators'),
                    description: signal,
                });
            } else if (signalLower.includes('keyword') || signalLower.includes('login') || signalLower.includes('verify')) {
                attacks.push({
                    type: 'suspicious_keywords',
                    title: translateText('Suspicious Keywords'),
                    description: signal,
                });
            } else {
                // Generic signal - still show it!
                attacks.push({
                    type: 'heuristic',
                    title: translateText('Security Signal'),
                    description: signal,
                });
            }
        });

        return attacks;
    }

    // Fallback: URL-based analysis if no engine signals available

    // Check for punycode/homograph indicators
    if (domain.startsWith('xn--') || /[^\x00-\x7F]/.test(domain)) {
        attacks.push({
            type: 'homograph',
            title: translateText('Homograph / IDN Attack'),
            description: translateText('Internationalized domain name detected.'),
            visual: domain.replace(/^xn--/, '').replace(/-[a-z0-9]+$/, ''),
            actual: domain,
            explanation: translateText('This domain uses international characters that may mimic legitimate domains.'),
        });
    }

    // Check for URL shorteners
    const shorteners = ['bit.ly', 't.co', 'goo.gl', 'tinyurl.com', 'ow.ly', 'is.gd', 'buff.ly'];
    if (shorteners.some(s => domain.includes(s))) {
        attacks.push({
            type: 'redirect',
            title: translateText('URL Shortener Detected'),
            description: translateText('URL uses a shortening service that hides the final destination.'),
            chain: [
                { label: translateText('Scanned URL'), url: url, status: 'warning' },
                { label: translateText('Hidden Destination'), url: '???', status: 'danger' },
            ],
        });
    }

    // Check for suspicious parameters
    try {
        const urlObj = new URL(url, 'https://example.com');
        const params = urlObj.search;
        if (params.length > 100 || /eval|script|exec|base64/i.test(params)) {
            attacks.push({
                type: 'obfuscation',
                title: translateText('Suspicious URL Parameters'),
                description: translateText('URL contains complex or potentially obfuscated parameters.'),
                code: params.substring(0, 200) + (params.length > 200 ? '...' : ''),
            });
        }
    } catch (e) {
        // Ignore URL parsing errors
    }

    // Check for suspicious TLDs
    const suspiciousTlds = ['.tk', '.ml', '.ga', '.cf', '.gq', '.xyz', '.top', '.work', '.date'];
    if (suspiciousTlds.some(tld => domain.endsWith(tld))) {
        attacks.push({
            type: 'suspicious_tld',
            title: translateText('Suspicious Domain'),
            description: formatText(translateText('Domain uses {tld} TLD commonly associated with malicious sites.'),
                { tld: domain.split('.').pop() }),
        });
    }

    // If high risk but no specific attacks found, add generic reason
    if (attacks.length === 0 && (verdict === 'HIGH' || verdict === 'MALICIOUS')) {
        attacks.push({
            type: 'heuristic',
            title: translateText('Heuristic Analysis'),
            description: translateText('URL flagged based on multiple risk factors detected by the analysis engine.'),
        });
    }

    return attacks;
}

/**
 * Render demo mode badge if showing demo data
 */
function renderDemoModeBadge() {
    const data = ThreatState.threatData;
    if (!data || !data.isDemo) return;

    // Create demo badge
    const existingBadge = document.getElementById('demoBadge');
    if (existingBadge) return; // Already exists

    const badge = document.createElement('div');
    badge.id = 'demoBadge';
    badge.style.cssText = `
        position: fixed;
        top: 80px;
        right: 16px;
        background: linear-gradient(135deg, #f59e0b, #d97706);
        color: white;
        padding: 8px 16px;
        border-radius: 8px;
        font-size: 12px;
        font-weight: 600;
        z-index: 1000;
        display: flex;
        align-items: center;
        gap: 6px;
        box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
    `;
    badge.innerHTML = `
        <span class="material-symbols-outlined" style="font-size: 16px;">science</span>
        <span>DEMO MODE - Scan a QR code to see real data</span>
    `;
    document.body.appendChild(badge);
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
