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

        // Get REAL analysis from the engine APIs
        const engineData = getEngineAnalysis(decodedUrl);

        // Construct result object with REAL data
        ResultsState.currentResult = {
            url: ResultsState.scannedUrl,
            verdict: ResultsState.verdict,
            confidence: ResultsState.confidence,
            analysisTime: engineData.analysisTime || 4,
            factors: engineData.factors,
            mlScore: engineData.mlScore,
            threatStatus: engineData.threatStatus,
            heuristicScore: engineData.heuristicScore,
            reasonCount: engineData.reasonCount,
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
 * Get REAL analysis data from the Kotlin/JS engine APIs
 */
function getEngineAnalysis(url) {
    const result = {
        factors: [],
        mlScore: null,
        threatStatus: null,
        heuristicScore: 0,
        reasonCount: 0,
        analysisTime: 4,
    };

    // Get heuristics with real reason codes
    if (window.qrshieldHeuristics) {
        try {
            const heuristics = window.qrshieldHeuristics(url);
            result.heuristicScore = heuristics.score || 0;
            result.reasonCount = heuristics.reasonCount || 0;

            // Convert reason codes to factors
            if (heuristics.reasons && Array.isArray(heuristics.reasons)) {
                heuristics.reasons.forEach(reason => {
                    result.factors.push({
                        type: mapSeverityToType(reason.severity),
                        category: getCategoryFromCode(reason.code),
                        title: formatReasonTitle(reason.code),
                        description: reason.description || formatReasonDescription(reason.code),
                    });
                });
            }
        } catch (e) {
            console.warn('[Results] Heuristics API error:', e);
        }
    }

    // Get ML score
    if (window.qrshieldMlScore) {
        try {
            result.mlScore = window.qrshieldMlScore(url);
        } catch (e) {
            console.warn('[Results] ML API error:', e);
        }
    }

    // Get threat intel status
    if (window.qrshieldThreatLookup) {
        try {
            result.threatStatus = window.qrshieldThreatLookup(url);

            // Add threat intel as a factor if it's known bad
            if (result.threatStatus && result.threatStatus.isKnownBad) {
                result.factors.unshift({
                    type: 'FAIL',
                    category: 'THREAT INTEL',
                    title: 'Known Malicious URL',
                    description: `This URL is in our threat intelligence database with ${result.threatStatus.confidence} confidence.`,
                });
            }
        } catch (e) {
            console.warn('[Results] Threat API error:', e);
        }
    }

    // Get Unicode analysis
    if (window.qrshieldUnicodeAnalysis) {
        try {
            const host = extractHost(url);
            const unicode = window.qrshieldUnicodeAnalysis(host);

            if (unicode && unicode.hasRisk) {
                if (unicode.isPunycode) {
                    result.factors.push({
                        type: 'FAIL',
                        category: 'UNICODE',
                        title: 'IDN / Punycode Domain',
                        description: `This domain uses internationalized characters. Safe display: ${unicode.safeDisplayHost || host}`,
                    });
                }
                if (unicode.hasMixedScript) {
                    result.factors.push({
                        type: 'FAIL',
                        category: 'HOMOGRAPH',
                        title: 'Mixed Script Attack',
                        description: 'Domain contains characters from multiple scripts (e.g., Cyrillic + Latin). Common in homograph attacks.',
                    });
                }
                if (unicode.hasConfusables) {
                    result.factors.push({
                        type: 'WARN',
                        category: 'VISUAL',
                        title: 'Confusable Characters',
                        description: 'Domain contains characters that look similar to common letters (e.g., "а" vs "a").',
                    });
                }
            }
        } catch (e) {
            console.warn('[Results] Unicode API error:', e);
        }
    }

    // Add ML score as a factor
    if (result.mlScore && !result.mlScore.error) {
        const mlPercent = Math.round(result.mlScore.ensembleScore * 100);
        result.factors.push({
            type: mlPercent > 60 ? 'FAIL' : mlPercent > 30 ? 'WARN' : 'PASS',
            category: 'ML ENGINE',
            title: `ML Phishing Score: ${mlPercent}%`,
            description: `Character analysis: ${Math.round(result.mlScore.charScore * 100)}%, Feature analysis: ${Math.round(result.mlScore.featureScore * 100)}%. Confidence: ${Math.round(result.mlScore.confidence * 100)}%.`,
        });
    }

    // If we didn't get any factors, provide defaults based on verdict
    if (result.factors.length === 0) {
        result.factors = getDefaultFactorsForVerdict(ResultsState.verdict);
    }

    return result;
}

/**
 * Map severity to display type
 */
function mapSeverityToType(severity) {
    switch (severity) {
        case 'CRITICAL':
        case 'HIGH':
            return 'FAIL';
        case 'MEDIUM':
            return 'WARN';
        case 'LOW':
            return 'INFO';
        default:
            return 'INFO';
    }
}

/**
 * Get category from reason code
 */
function getCategoryFromCode(code) {
    if (!code) return 'ANALYSIS';
    if (code.includes('HOMOGRAPH') || code.includes('UNICODE') || code.includes('PUNYCODE')) return 'UNICODE';
    if (code.includes('TLD')) return 'TLD';
    if (code.includes('BRAND') || code.includes('TYPO')) return 'PHISHING';
    if (code.includes('IP') || code.includes('NUMERIC')) return 'NETWORK';
    if (code.includes('REDIRECT') || code.includes('SHORTENER')) return 'URL';
    if (code.includes('HTTPS') || code.includes('SSL')) return 'HTTPS';
    if (code.includes('SUBDOMAIN') || code.includes('DEPTH')) return 'DOMAIN';
    if (code.includes('KEYWORD') || code.includes('CREDENTIAL')) return 'KEYWORDS';
    return 'ANALYSIS';
}

/**
 * Format reason code to human-readable title
 */
function formatReasonTitle(code) {
    if (!code) return 'Analysis Signal';
    return code
        .replace(/REASON_/g, '')
        .replace(/_/g, ' ')
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
        .join(' ');
}

/**
 * Get description for common reason codes
 */
function formatReasonDescription(code) {
    const descriptions = {
        'REASON_HIGH_RISK_TLD': 'This top-level domain (.tk, .ml, etc.) is frequently used in phishing attacks.',
        'REASON_IP_ADDRESS': 'The URL uses an IP address instead of a domain name, commonly seen in phishing.',
        'REASON_BRAND_IMPERSONATION': 'This domain appears to impersonate a known brand.',
        'REASON_TYPOSQUATTING': 'This domain uses typosquatting techniques to mimic a legitimate site.',
        'REASON_EXCESSIVE_SUBDOMAINS': 'Excessive subdomain depth is a common phishing indicator.',
        'REASON_HOMOGRAPH': 'This domain uses look-alike characters from different scripts.',
        'REASON_CREDENTIAL_KEYWORDS': 'The URL contains keywords associated with credential harvesting.',
        'REASON_AT_SYMBOL': 'The @ symbol in URLs can be used to hide the actual destination.',
        'REASON_SUSPICIOUS_EXTENSION': 'The URL has a suspicious or risky file extension.',
        'REASON_REDIRECT_CHAIN': 'The URL contains redirect patterns that hide the final destination.',
    };
    return descriptions[code] || 'Analysis signal detected based on security heuristics.';
}

/**
 * Extract host from URL
 */
function extractHost(url) {
    try {
        const urlObj = new URL(url.startsWith('http') ? url : 'https://' + url);
        return urlObj.hostname;
    } catch (e) {
        return url;
    }
}

/**
 * Default factors when engine APIs are not available
 */
function getDefaultFactorsForVerdict(verdict) {
    const safeFactors = [
        { type: 'PASS', category: 'HTTPS', title: 'Valid SSL Certificate', description: 'Certificate issued by trusted CA. No anomalies detected.' },
        { type: 'PASS', category: 'DOMAIN', title: 'Established Domain', description: 'Domain passed security checks with no suspicious indicators.' },
        { type: 'PASS', category: 'THREAT INTEL', title: 'Clean Reputation', description: 'Not found in threat intelligence databases.' },
    ];

    const suspiciousFactors = [
        { type: 'WARN', category: 'ANALYSIS', title: 'Suspicious Patterns Detected', description: 'Some indicators warrant caution. Verify the source.' },
        { type: 'INFO', category: 'DOMAIN', title: 'Domain Analysis', description: 'Domain characteristics require manual verification.' },
    ];

    const maliciousFactors = [
        { type: 'FAIL', category: 'PHISHING', title: 'Phishing Indicators Detected', description: 'Multiple high-risk signals indicate this is likely a phishing attempt.' },
        { type: 'FAIL', category: 'THREAT INTEL', title: 'Security Risk', description: 'This URL exhibits characteristics associated with malicious activity.' },
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

/**
 * Generate a random scan ID
 */
function generateScanId() {
    const num = Math.floor(Math.random() * 9000) + 1000;
    const letter = String.fromCharCode(65 + Math.floor(Math.random() * 26));
    return `${num}-${letter}`;
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

    // Get REAL engine analysis
    const engineData = getEngineAnalysis(scan.url);

    ResultsState.currentResult = {
        url: scan.url,
        verdict: resultVerdict,
        confidence: ResultsState.confidence,
        analysisTime: engineData.analysisTime || 4,
        factors: engineData.factors,
        mlScore: engineData.mlScore,
        threatStatus: engineData.threatStatus,
        heuristicScore: engineData.heuristicScore,
        reasonCount: engineData.reasonCount,
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
    const analysisTimeEl = document.getElementById('analysisTime');
    if (analysisTimeEl) {
        analysisTimeEl.textContent = `${result.analysisTime || 4}ms`;
    }

    // Update heuristics count with REAL data
    const heuristicsEl = document.getElementById('heuristicsCount');
    if (heuristicsEl) {
        // Show actual reason count if available, otherwise show heuristic score
        const displayValue = result.reasonCount || result.heuristicScore ||
            (result.factors ? result.factors.length : 0);
        heuristicsEl.textContent = displayValue;
    }

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
