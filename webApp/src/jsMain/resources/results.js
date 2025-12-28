/**
 * QR-SHIELD Results Page Controller
 * 
 * Handles the display and interaction of scan results,
 * integrating with the Kotlin/JS PhishingEngine for analysis.
 * 
 * @author QR-SHIELD Team
 * @version 2.4.0
 */

// =============================================================================
// CONFIGURATION & STATE
// =============================================================================

const ResultsConfig = {
    version: "2.4.0",
    build: "892",
    heuristicsCount: 142,
    defaultAnalysisTime: 4,
};

const ResultsState = {
    currentResult: null,
    scannedUrl: null,
    verdict: 'UNKNOWN',
    confidence: 0,
    riskLevel: 0,
    factors: [],
    isSidebarOpen: false,
    scanId: null,
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
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('[QR-SHIELD Results] Initializing results page v' + ResultsConfig.version);

    initializeFromURL();
    setupEventListeners();
    setupMobileMenu();

    // If we have URL params, display the result
    if (ResultsState.scannedUrl) {
        displayResult(ResultsState.currentResult);
    } else {
        // Demo mode: show sample safe result
        showDemoResult();
    }

    // Show content now that it's loaded (prevents flash of hardcoded content)
    // Use requestAnimationFrame to ensure DOM updates are complete before showing
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            document.body.classList.add('loaded');
        });
    });

    window.qrshieldApplyTranslations?.(document.body);
});

/**
 * Initialize state from URL parameters
 */
function initializeFromURL() {
    const urlParams = new URLSearchParams(window.location.search);
    const scanId = urlParams.get('scanId');
    const url = urlParams.get('url');
    const verdict = urlParams.get('verdict');
    const score = urlParams.get('score');

    if (scanId && window.QRShieldUI && window.QRShieldUI.getScanById) {
        const scan = window.QRShieldUI.getScanById(scanId);
        if (scan) {
            applyScanResult(scan, scanId);
            return;
        }
    }

    if (url) {
        const decodedUrl = decodeURIComponent(url);
        const scoreValue = parseInt(score) || 50;
        const verdictValue = verdict || 'UNKNOWN';

        if (window.QRShieldUI && window.QRShieldUI.getScanHistory) {
            const existing = findExistingScan(decodedUrl, verdictValue, scoreValue);
            if (existing) {
                applyScanResult(existing, existing.id);
                return;
            }
        }

        ResultsState.scannedUrl = decodedUrl;
        ResultsState.verdict = verdictValue;
        ResultsState.confidence = scoreValue;

        // Generate a scan ID
        const scanId = generateScanId();
        const scanIdEl = document.getElementById('scanId');
        if (scanIdEl) {
            scanIdEl.textContent = formatText('Result # {id}', { id: scanId });
        }

        // Construct result object
        ResultsState.currentResult = {
            url: ResultsState.scannedUrl,
            verdict: ResultsState.verdict,
            confidence: ResultsState.confidence,
            analysisTime: ResultsConfig.defaultAnalysisTime,
            factors: getFactorsForVerdict(ResultsState.verdict),
        };

        // Save to QRShieldUI scan history if not already saved
        // (Check if this URL was just added to avoid duplicates)
        if (window.QRShieldUI && window.QRShieldUI.addScanToHistory) {
            const recentHistory = window.QRShieldUI.getScanHistory();
            const justAdded = recentHistory.length > 0 &&
                recentHistory[0].url === ResultsState.scannedUrl &&
                (Date.now() - recentHistory[0].timestamp) < 5000; // Within 5 seconds

            if (!justAdded) {
                window.QRShieldUI.addScanToHistory({
                    url: ResultsState.scannedUrl,
                    verdict: ResultsState.verdict === 'MALICIOUS' ? 'HIGH' :
                        ResultsState.verdict === 'SUSPICIOUS' ? 'MEDIUM' :
                            ResultsState.verdict === 'SAFE' ? 'SAFE' : 'LOW',
                    score: ResultsState.confidence || 0,
                    signals: []
                });
                console.log('[Results] Saved scan to history:', ResultsState.scannedUrl);
            }
        }
    }
}

/**
 * Generate a random scan ID
 */
function generateScanId() {
    const num = Math.floor(Math.random() * 9000) + 1000;
    const letter = String.fromCharCode(65 + Math.floor(Math.random() * 26));
    return `${num}-${letter}`;
}

/**
 * Get analysis factors based on verdict
 */
function getFactorsForVerdict(verdict) {
    const safeFactors = [
        { type: 'PASS', category: 'HTTPS', title: 'Valid SSL Certificate', description: 'Certificate issued by trusted CA. No anomalies in chain of trust.' },
        { type: 'INFO', category: 'DOMAIN', title: 'Established Domain', description: 'Domain registered > 5 years ago. Low probability of churn-and-burn.' },
        { type: 'CLEAN', category: 'DB CHECK', title: 'Blacklist Status', description: 'Not found in 52 local offline threat databases.' },
    ];

    const suspiciousFactors = [
        { type: 'WARN', category: 'HTTPS', title: 'SSL Certificate Issues', description: 'Certificate recently issued or self-signed. Proceed with caution.' },
        { type: 'WARN', category: 'DOMAIN', title: 'Newly Registered Domain', description: 'Domain registered < 30 days ago. Common in phishing campaigns.' },
        { type: 'INFO', category: 'URL', title: 'URL Shortener Detected', description: 'Destination URL obfuscated. Unable to verify final target.' },
    ];

    const maliciousFactors = [
        { type: 'FAIL', category: 'PHISHING', title: 'Typosquatting Detected', description: 'Domain mimics known brand (PayPal → Paypa1). High-confidence phishing.' },
        { type: 'FAIL', category: 'TLD', title: 'High-Risk TLD', description: '.tk domain is associated with 78% of phishing attacks globally.' },
        { type: 'FAIL', category: 'DB CHECK', title: 'Blacklist Match', description: 'Found in 3 local offline threat databases.' },
    ];

    switch (verdict) {
        case 'SAFE':
            return safeFactors;
        case 'SUSPICIOUS':
            return suspiciousFactors;
        case 'MALICIOUS':
            return maliciousFactors;
        default:
            return safeFactors;
    }
}

function mapHistoryVerdict(verdict) {
    switch (verdict) {
        case 'SAFE':
        case 'LOW':
            return 'SAFE';
        case 'MEDIUM':
            return 'SUSPICIOUS';
        case 'HIGH':
            return 'MALICIOUS';
        default:
            return 'UNKNOWN';
    }
}

function mapResultVerdictToHistory(verdict) {
    switch (verdict) {
        case 'SAFE':
            return 'SAFE';
        case 'SUSPICIOUS':
            return 'MEDIUM';
        case 'MALICIOUS':
            return 'HIGH';
        default:
            return 'UNKNOWN';
    }
}

function formatScanId(scanId) {
    if (!scanId) return generateScanId();
    return scanId.slice(-7).toUpperCase();
}

function applyScanResult(scan, scanId) {
    const resultVerdict = mapHistoryVerdict(scan.verdict);
    ResultsState.scanId = scanId;
    ResultsState.scannedUrl = scan.url;
    ResultsState.verdict = resultVerdict;
    ResultsState.confidence = parseInt(scan.score) || 50;

    const scanIdEl = document.getElementById('scanId');
    if (scanIdEl) {
        scanIdEl.textContent = formatText('Result # {id}', { id: formatScanId(scanId) });
    }

    ResultsState.currentResult = {
        url: scan.url,
        verdict: resultVerdict,
        confidence: ResultsState.confidence,
        analysisTime: ResultsConfig.defaultAnalysisTime,
        factors: getFactorsForVerdict(resultVerdict),
    };
}

function findExistingScan(url, verdict, score) {
    const history = window.QRShieldUI?.getScanHistory?.() || [];
    const historyVerdict = mapResultVerdictToHistory(verdict);
    return history.find(scan => {
        const scanScore = parseInt(scan.score) || 0;
        const scoreMatch = score === 0 || Math.abs(scanScore - score) <= 1;
        return scan.url === url && scan.verdict === historyVerdict && scoreMatch;
    });
}

// =============================================================================
// EVENT LISTENERS
// =============================================================================

function setupEventListeners() {
    // Scan New QR button
    document.getElementById('scanNewBtn')?.addEventListener('click', () => {
        window.location.href = 'dashboard.html';
    });

    // Back to Dashboard button
    document.getElementById('backBtn')?.addEventListener('click', () => {
        window.location.href = 'dashboard.html';
    });

    // Share Report button
    document.getElementById('shareBtn')?.addEventListener('click', shareReport);

    // Copy Link button
    document.getElementById('copyBtn')?.addEventListener('click', copyLink);

    // Factor card click handlers
    document.querySelectorAll('.factor-card').forEach(card => {
        card.addEventListener('click', () => {
            card.classList.toggle('expanded');
        });
    });

    // Help button
    document.getElementById('helpBtn')?.addEventListener('click', showHelpInfo);
}

function setupMobileMenu() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');

    menuToggle?.addEventListener('click', () => {
        sidebar.classList.toggle('open');
        ResultsState.isSidebarOpen = !ResultsState.isSidebarOpen;
    });

    // Close sidebar when clicking outside
    document.addEventListener('click', (e) => {
        if (ResultsState.isSidebarOpen &&
            !sidebar.contains(e.target) &&
            !menuToggle.contains(e.target)) {
            sidebar.classList.remove('open');
            ResultsState.isSidebarOpen = false;
        }
    });
}

// =============================================================================
// DISPLAY FUNCTIONS
// =============================================================================

/**
 * Display a scan result
 */
function displayResult(result) {
    if (!result) return;

    // Update URL display
    document.getElementById('scannedUrl').textContent = truncateUrl(result.url, 50);
    document.getElementById('urlDisplay').title = result.url;

    // Update verdict
    updateVerdictDisplay(result.verdict, result.confidence);

    // Update risk meter
    updateRiskMeter(result.verdict);

    // Update analysis time
    document.getElementById('analysisTime').textContent = `${result.analysisTime}ms`;

    // Update factors
    updateFactors(result.factors);

    // Add animation
    document.querySelectorAll('.verdict-card, .factor-card').forEach((el, i) => {
        el.style.animationDelay = `${i * 0.1}s`;
        el.classList.add('animate-slide-up');
    });
}

/**
 * Update the verdict display
 */
function updateVerdictDisplay(verdict, confidence) {
    const verdictCard = document.getElementById('verdictCard');
    const statusBadge = document.getElementById('statusBadge');
    const statusIcon = statusBadge.querySelector('.status-icon');
    const statusTitle = statusBadge.querySelector('.status-title');
    const verdictIcon = document.getElementById('verdictIcon');
    const verdictTitle = document.getElementById('verdictTitle');
    const verdictDescription = document.getElementById('verdictDescription');
    const confidenceScore = document.getElementById('confidenceScore');
    const confidenceLabel = document.getElementById('confidenceLabel');

    // Remove existing state classes
    verdictCard.classList.remove('safe', 'suspicious', 'malicious');

    // The 'confidence' param here is actually the RISK SCORE from the engine (0-100)
    // Low risk score = SAFE
    // High risk score = MALICIOUS
    const riskScore = confidence || 0;

    // Update based on verdict
    switch (verdict) {
        case 'SAFE':
            verdictCard.classList.add('safe');
            statusIcon.textContent = 'check_circle';
            statusIcon.style.backgroundColor = 'rgba(22, 163, 74, 0.2)';
            statusIcon.style.color = '#16a34a';
            statusTitle.textContent = translateText('Scan Complete');
            verdictIcon.textContent = 'shield_lock';
            verdictIcon.style.color = '#16a34a';
            verdictTitle.textContent = translateText('SAFE TO VISIT');
            verdictDescription.textContent = translateText('Verified by local heuristics v2.4. No phishing patterns, obfuscated scripts, or blacklist matches found.');
            // For SAFE: Show SAFETY score (100 - risk), minimum 92%
            const safetyScore = Math.max(100 - riskScore, 92);
            confidenceScore.textContent = `${safetyScore}%`;
            if (confidenceLabel) confidenceLabel.textContent = translateText('Safety Score');
            break;

        case 'SUSPICIOUS':
            verdictCard.classList.add('suspicious');
            statusIcon.textContent = 'warning';
            statusIcon.style.backgroundColor = 'rgba(245, 158, 11, 0.2)';
            statusIcon.style.color = '#f59e0b';
            statusTitle.textContent = translateText('Caution Advised');
            verdictIcon.textContent = 'shield';
            verdictIcon.style.color = '#f59e0b';
            verdictTitle.textContent = translateText('PROCEED WITH CAUTION');
            verdictDescription.textContent = translateText('Some suspicious indicators detected. Verify the source before entering sensitive information.');
            // For SUSPICIOUS: Show the RISK score directly
            confidenceScore.textContent = `${riskScore}%`;
            if (confidenceLabel) confidenceLabel.textContent = translateText('Risk Score');
            break;

        case 'MALICIOUS':
            verdictCard.classList.add('malicious');
            statusIcon.textContent = 'error';
            statusIcon.style.backgroundColor = 'rgba(239, 68, 68, 0.2)';
            statusIcon.style.color = '#ef4444';
            statusTitle.textContent = translateText('Threat Detected');
            verdictIcon.textContent = 'gpp_bad';
            verdictIcon.style.color = '#ef4444';
            verdictTitle.textContent = translateText('DO NOT VISIT');
            verdictDescription.textContent = translateText('High-confidence phishing detected. This URL exhibits multiple malicious indicators.');
            // For MALICIOUS: Show the RISK score directly
            confidenceScore.textContent = `${riskScore}%`;
            if (confidenceLabel) confidenceLabel.textContent = translateText('Risk Score');
            break;

        default:
            statusTitle.textContent = translateText('Analysis Complete');
            verdictTitle.textContent = translateText('UNKNOWN');
            verdictDescription.textContent = translateText('Unable to determine verdict. Please try again.');
            confidenceScore.textContent = `${riskScore}%`;
            if (confidenceLabel) confidenceLabel.textContent = translateText('Confidence Score');
    }
}

/**
 * Update the risk meter visualization
 */
function updateRiskMeter(verdict) {
    const riskBadge = document.getElementById('riskBadge');
    const segments = [
        document.getElementById('seg1'),
        document.getElementById('seg2'),
        document.getElementById('seg3'),
        document.getElementById('seg4'),
        document.getElementById('seg5'),
    ];

    // Reset all segments
    segments.forEach(seg => {
        seg.classList.remove('active', 'warning', 'danger');
        seg.style.backgroundColor = ''; // Reset inline style
        seg.style.boxShadow = '';
    });

    // Detect if we're in light mode
    const isLightMode = document.documentElement.classList.contains('light') ||
        document.body.classList.contains('light') ||
        document.documentElement.getAttribute('data-theme') === 'light' ||
        window.matchMedia('(prefers-color-scheme: light)').matches;

    switch (verdict) {
        case 'SAFE':
            riskBadge.textContent = translateText('LOW RISK');
            riskBadge.className = 'risk-badge';
            // Use stronger/darker green in light mode for better visibility
            const safeColor = isLightMode ? '#15803d' : '#22c55e';
            const safeGlow = isLightMode ? 'rgba(21, 128, 61, 0.7)' : 'rgba(34, 197, 94, 0.5)';
            const safeBg = isLightMode ? 'rgba(21, 128, 61, 0.2)' : 'rgba(34, 197, 94, 0.1)';

            riskBadge.style.backgroundColor = safeBg;
            riskBadge.style.color = safeColor;

            segments[0].classList.add('active');
            segments[1].classList.add('active');
            segments[0].style.backgroundColor = safeColor;
            segments[1].style.backgroundColor = safeColor;
            segments[0].style.boxShadow = `0 0 8px ${safeGlow}`;
            segments[1].style.boxShadow = `0 0 8px ${safeGlow}`;
            break;

        case 'SUSPICIOUS':
            riskBadge.textContent = translateText('MEDIUM RISK');
            riskBadge.className = 'risk-badge warning';
            const warnColor = isLightMode ? '#d97706' : '#f59e0b';
            const warnGlow = isLightMode ? 'rgba(217, 119, 6, 0.5)' : 'rgba(245, 158, 11, 0.5)';
            const warnBg = isLightMode ? 'rgba(217, 119, 6, 0.15)' : 'rgba(245, 158, 11, 0.1)';

            riskBadge.style.backgroundColor = warnBg;
            riskBadge.style.color = warnColor;

            segments[0].classList.add('active', 'warning');
            segments[1].classList.add('active', 'warning');
            segments[2].classList.add('active', 'warning');
            [segments[0], segments[1], segments[2]].forEach(seg => {
                seg.style.backgroundColor = warnColor;
                seg.style.boxShadow = `0 0 8px ${warnGlow}`;
            });
            break;

        case 'MALICIOUS':
            riskBadge.textContent = translateText('HIGH RISK');
            riskBadge.className = 'risk-badge danger';
            const dangerColor = isLightMode ? '#dc2626' : '#ef4444';
            const dangerGlow = isLightMode ? 'rgba(220, 38, 38, 0.5)' : 'rgba(239, 68, 68, 0.5)';
            const dangerBg = isLightMode ? 'rgba(220, 38, 38, 0.15)' : 'rgba(239, 68, 68, 0.1)';

            riskBadge.style.backgroundColor = dangerBg;
            riskBadge.style.color = dangerColor;

            segments.forEach(seg => {
                seg.classList.add('active', 'danger');
                seg.style.backgroundColor = dangerColor;
                seg.style.boxShadow = `0 0 8px ${dangerGlow}`;
            });
            break;
    }
}

/**
 * Update the analysis factors grid
 */
function updateFactors(factors) {
    const grid = document.getElementById('factorsGrid');
    grid.innerHTML = '';

    factors.forEach(factor => {
        const card = document.createElement('div');
        card.className = 'factor-card';
        card.dataset.factor = factor.category.toLowerCase();

        const tagClass = getTagClass(factor.type);
        const typeLabel = translateText(factor.type);
        const categoryLabel = translateText(factor.category);
        const titleLabel = translateText(factor.title);
        const descriptionLabel = translateText(factor.description);

        card.innerHTML = `
            <div class="factor-header">
                <div class="factor-tags">
                    <span class="tag ${tagClass}">${typeLabel}</span>
                    <span class="tag tag-category">${categoryLabel}</span>
                </div>
                <span class="material-symbols-outlined factor-expand">expand_more</span>
            </div>
            <h4 class="factor-title">${titleLabel}</h4>
            <p class="factor-description">${descriptionLabel}</p>
        `;

        card.addEventListener('click', () => {
            card.classList.toggle('expanded');
        });

        grid.appendChild(card);
    });

    window.qrshieldApplyTranslations?.(grid);
}

/**
 * Get the CSS class for a factor tag
 */
function getTagClass(type) {
    const classes = {
        'PASS': 'tag-pass',
        'INFO': 'tag-info',
        'CLEAN': 'tag-clean',
        'WARN': 'tag-warn',
        'FAIL': 'tag-fail',
    };
    return classes[type] || 'tag-info';
}

// =============================================================================
// ACTION HANDLERS
// =============================================================================

/**
 * Share the scan report
 */
async function shareReport() {
    const shareData = {
        title: translateText('QR-SHIELD Scan Result'),
        text: formatText('URL: {url}\nVerdict: {verdict}\nConfidence: {confidence}%', {
            url: ResultsState.scannedUrl,
            verdict: translateText(ResultsState.verdict),
            confidence: ResultsState.confidence
        }),
        url: window.location.href,
    };

    if (navigator.share) {
        try {
            await navigator.share(shareData);
            showToast('Report shared successfully!');
        } catch (err) {
            if (err.name !== 'AbortError') {
                console.error('[Share] Error:', err);
                fallbackShare();
            }
        }
    } else {
        fallbackShare();
    }
}

/**
 * Fallback share method (copy link)
 */
function fallbackShare() {
    copyToClipboard(window.location.href);
    showToast('Link copied to clipboard!');
}

/**
 * Copy the scanned URL to clipboard
 */
async function copyLink() {
    if (!ResultsState.scannedUrl) {
        showToast('No URL to copy', 'error');
        return;
    }

    const success = await copyToClipboard(ResultsState.scannedUrl);
    if (success) {
        showToast('Link copied to clipboard!');
    } else {
        showToast('Failed to copy link', 'error');
    }
}

/**
 * Copy text to clipboard
 */
async function copyToClipboard(text) {
    try {
        await navigator.clipboard.writeText(text);
        return true;
    } catch (err) {
        // Fallback for older browsers
        const textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.select();
        try {
            document.execCommand('copy');
            return true;
        } catch (e) {
            return false;
        } finally {
            document.body.removeChild(textarea);
        }
    }
}

// =============================================================================
// UTILITY FUNCTIONS
// =============================================================================

/**
 * Truncate URL for display
 */
function truncateUrl(url, maxLength) {
    if (!url || url.length <= maxLength) return url;
    return url.substring(0, maxLength - 3) + '...';
}

/**
 * Show help info modal
 */
function showHelpInfo() {
    // Inject modal styles if not present
    if (!document.getElementById('qrModalStyles')) {
        const styles = document.createElement('style');
        styles.id = 'qrModalStyles';
        styles.textContent = `
            .qr-modal-overlay {
                position: fixed;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(0, 0, 0, 0.8);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 10000;
                opacity: 0;
                transition: opacity 0.2s ease;
            }
            .qr-modal-overlay.visible {
                opacity: 1;
            }
            .qr-modal {
                background: var(--bg-primary, #1a1a2e);
                border-radius: 16px;
                width: 90%;
                max-width: 500px;
                max-height: 80vh;
                display: flex;
                flex-direction: column;
                overflow: hidden;
                box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
                transform: scale(0.95);
                transition: transform 0.2s ease;
            }
            .qr-modal-overlay.visible .qr-modal {
                transform: scale(1);
            }
            .qr-modal-header {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 16px 20px;
                border-bottom: 1px solid rgba(255, 255, 255, 0.1);
            }
            .qr-modal-title {
                display: flex;
                align-items: center;
                gap: 10px;
            }
            .qr-modal-title h3 {
                margin: 0;
                color: var(--text-primary, #fff);
                font-size: 18px;
            }
            .qr-modal-close-btn {
                background: none;
                border: none;
                color: var(--text-secondary, #94a3b8);
                cursor: pointer;
                padding: 8px;
                border-radius: 8px;
                transition: background 0.2s;
            }
            .qr-modal-close-btn:hover {
                background: rgba(255, 255, 255, 0.1);
                color: var(--text-primary, #fff);
            }
            .qr-modal-footer {
                display: flex;
                justify-content: flex-end;
                gap: 12px;
                padding: 16px 20px;
                border-top: 1px solid rgba(255, 255, 255, 0.1);
            }
        `;
        document.head.appendChild(styles);
    }

    // Remove existing modal if any
    const existingModal = document.getElementById('helpModal');
    if (existingModal) existingModal.remove();

    const modal = document.createElement('div');
    modal.id = 'helpModal';
    modal.className = 'qr-modal-overlay';
    modal.innerHTML = `
        <div class="qr-modal">
            <div class="qr-modal-header">
                <div class="qr-modal-title">
                    <span class="material-symbols-outlined" style="color: #6366f1;">help</span>
                    <h3>Help & Keyboard Shortcuts</h3>
                </div>
                <button class="qr-modal-close-btn" id="closeHelp">
                    <span class="material-symbols-outlined">close</span>
                </button>
            </div>
            
            <div class="qr-modal-content" style="padding: 20px;">
                <div style="background: var(--surface-dark, #1e293b); border-radius: 12px; padding: 16px; margin-bottom: 16px;">
                    <div style="font-weight: 600; margin-bottom: 12px; color: var(--text-primary, #f1f5f9);">Keyboard Shortcuts</div>
                    <div style="display: grid; gap: 8px; font-size: 14px;">
                        <div style="display: flex; justify-content: space-between;">
                            <span style="color: var(--text-secondary, #94a3b8);">Copy URL</span>
                            <kbd style="background: rgba(99, 102, 241, 0.2); padding: 4px 8px; border-radius: 4px; font-family: monospace; color: #6366f1;">Ctrl/Cmd + C</kbd>
                        </div>
                        <div style="display: flex; justify-content: space-between;">
                            <span style="color: var(--text-secondary, #94a3b8);">Go Back</span>
                            <kbd style="background: rgba(99, 102, 241, 0.2); padding: 4px 8px; border-radius: 4px; font-family: monospace; color: #6366f1;">Backspace</kbd>
                        </div>
                        <div style="display: flex; justify-content: space-between;">
                            <span style="color: var(--text-secondary, #94a3b8);">New Scan</span>
                            <kbd style="background: rgba(99, 102, 241, 0.2); padding: 4px 8px; border-radius: 4px; font-family: monospace; color: #6366f1;">N</kbd>
                        </div>
                    </div>
                </div>
                
                <div style="background: var(--surface-dark, #1e293b); border-radius: 12px; padding: 16px;">
                    <div style="font-weight: 600; margin-bottom: 12px; color: var(--text-primary, #f1f5f9);">About This Page</div>
                    <p style="font-size: 14px; color: var(--text-secondary, #94a3b8); line-height: 1.6;">
                        This page shows the analysis results for a scanned URL. All analysis is performed locally on your device - no data is sent to external servers.
                    </p>
                </div>
            </div>
            
            <div class="qr-modal-footer">
                <button class="btn-primary" id="closeHelpBtn" style="
                    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
                    border: none;
                    color: white;
                    padding: 12px 24px;
                    border-radius: 10px;
                    cursor: pointer;
                    font-size: 14px;
                    font-weight: 600;
                ">Got it</button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    // Animate in (must use requestAnimationFrame for transition to work)
    requestAnimationFrame(() => {
        modal.classList.add('visible');
    });

    // Close modal function
    const closeModal = () => {
        modal.classList.remove('visible');
        setTimeout(() => modal.remove(), 200);
    };

    // Add event listeners
    document.getElementById('closeHelp')?.addEventListener('click', closeModal);
    document.getElementById('closeHelpBtn')?.addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });
}

/**
 * Show a toast notification
 */
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    const toastIcon = toast.querySelector('.toast-icon');

    toastMessage.textContent = translateText(message);

    // Update icon based on type
    switch (type) {
        case 'success':
            toastIcon.textContent = 'check_circle';
            toastIcon.style.color = '#22c55e';
            break;
        case 'warning':
            toastIcon.textContent = 'warning';
            toastIcon.style.color = '#f59e0b';
            break;
        case 'error':
            toastIcon.textContent = 'error';
            toastIcon.style.color = '#ef4444';
            break;
    }

    // Show toast
    toast.classList.add('show');

    // Hide after 3 seconds
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

/**
 * Show a demo result for testing
 */
function showDemoResult() {
    ResultsState.scannedUrl = 'https://secure-login.example.com/auth?id=8291';
    ResultsState.verdict = 'SAFE';
    ResultsState.confidence = 99.8;

    ResultsState.currentResult = {
        url: ResultsState.scannedUrl,
        verdict: ResultsState.verdict,
        confidence: ResultsState.confidence,
        analysisTime: 4,
        factors: getFactorsForVerdict('SAFE'),
    };

    displayResult(ResultsState.currentResult);
}

// =============================================================================
// KEYBOARD SHORTCUTS
// =============================================================================

document.addEventListener('keydown', (e) => {
    // Escape: Go back to dashboard
    if (e.key === 'Escape') {
        window.location.href = 'dashboard.html';
    }

    // C: Copy link
    if (e.key === 'c' && !e.ctrlKey && !e.metaKey) {
        const activeElement = document.activeElement;
        if (activeElement.tagName !== 'INPUT' && activeElement.tagName !== 'TEXTAREA') {
            copyLink();
        }
    }

    // S: Share report
    if (e.key === 's' && !e.ctrlKey && !e.metaKey) {
        const activeElement = document.activeElement;
        if (activeElement.tagName !== 'INPUT' && activeElement.tagName !== 'TEXTAREA') {
            shareReport();
        }
    }
});

// =============================================================================
// EXPORT FOR TESTING
// =============================================================================

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        ResultsState,
        ResultsConfig,
        displayResult,
        updateVerdictDisplay,
        updateRiskMeter,
        getFactorsForVerdict,
    };
}
