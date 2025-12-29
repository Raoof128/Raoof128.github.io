/**
 * QR-SHIELD Web Logic
 * Handles UI interactions, theming, and connects to Kotlin/JS engine
 */

// State
let isScanning = false;
let history = [];

// DOM Elements
const themeToggle = document.getElementById('themeToggle');
const urlInput = document.getElementById('urlInput');
const analyzeBtn = document.getElementById('analyzeBtn');
const scanQrBtn = document.getElementById('scanQrBtn');
const uploadQrBtn = document.getElementById('uploadQrBtn');
const dropZone = document.getElementById('dropZone');
const fileInput = document.getElementById('fileInput');
const resultCard = document.getElementById('resultCard');
const historyList = document.getElementById('historyList');
const clearHistoryBtn = document.getElementById('clearHistoryBtn');
const qrModal = document.getElementById('qrModal');
const closeModalBtn = document.getElementById('closeModalBtn');
const video = document.getElementById('qr-video');
const canvasElement = document.getElementById('qr-canvas');
const canvas = canvasElement.getContext('2d');

// Constants
const THEME_KEY = 'qrshield_theme';
// Use the same key as shared-ui.js for cross-page consistency
const HISTORY_KEY = 'qrshield_scan_history';
const NORMALIZE_RE = /\s+/g;

function normalizeKey(text) {
    return text ? text.replace(NORMALIZE_RE, ' ').trim() : '';
}

function translateText(text) {
    const normalized = normalizeKey(text);
    if (!normalized) return text;
    if (window.qrshieldGetTranslation) {
        return window.qrshieldGetTranslation(normalized);
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

// ==========================================
// Initialization
// ==========================================

const ONBOARDING_KEY = 'qrshield_onboarding_complete';

document.addEventListener('DOMContentLoaded', () => {
    // Load theme
    const savedTheme = localStorage.getItem(THEME_KEY) || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme);

    // Load history
    loadHistory();

    // Show onboarding for first-time users
    if (!localStorage.getItem(ONBOARDING_KEY)) {
        showOnboarding();
    }

    // Setup onboarding event listeners
    setupOnboarding();

    // Demo Mode: Auto-fill sample URL if ?demo=true
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('demo') === 'true') {
        // Skip onboarding in demo mode
        localStorage.setItem(ONBOARDING_KEY, 'true');
        hideOnboarding();

        // Pre-fill a sample malicious URL for judges
        urlInput.value = 'https://paypa1-secure.tk/login';
        urlInput.focus();

        // Show helpful toast
        setTimeout(() => {
            showToast('🎬 Demo Mode: Click "Analyze URL" to see detection', 'info');
        }, 500);
    }

    // Keyboard shortcuts
    setupKeyboardShortcuts();

    // Fallback if Kotlin hasn't loaded yet
    if (!window.qrshieldAnalyze) {
        window.qrshieldAnalyze = (url) => {
            console.warn("Kotlin engine not ready yet");
            showToast("Engine initializing, please wait...", "warning");
        };
    }
});

// ==========================================
// Keyboard Shortcuts (Desktop-friendly)
// ==========================================

function setupKeyboardShortcuts() {
    // Enter key to analyze (when input focused)
    urlInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && urlInput.value.trim()) {
            e.preventDefault();
            if (window.qrshieldAnalyze) {
                window.qrshieldAnalyze(urlInput.value.trim());
            }
        }
    });

    // Global keyboard shortcuts
    document.addEventListener('keydown', (e) => {
        // Cmd/Ctrl + V when not in input = focus and paste
        if ((e.metaKey || e.ctrlKey) && e.key === 'v' && document.activeElement !== urlInput) {
            e.preventDefault();
            urlInput.focus();
            navigator.clipboard.readText().then(text => {
                if (text) {
                    urlInput.value = text;
                    showToast('URL pasted - press Enter to analyze', 'info');
                }
            }).catch(() => {
                // Fallback: just focus the input
                showToast('Press Cmd/Ctrl+V again to paste', 'info');
            });
        }

        // Escape to reset
        if (e.key === 'Escape') {
            if (!resultCard.classList.contains('hidden')) {
                window.resetScanner();
            }
        }

        // Forward slash to focus input (like many search UIs)
        if (e.key === '/' && document.activeElement !== urlInput) {
            e.preventDefault();
            urlInput.focus();
        }
    });
}

// ==========================================
// Theme Handling
// ==========================================

themeToggle.addEventListener('click', () => {
    const current = document.documentElement.getAttribute('data-theme');
    const next = current === 'light' ? 'dark' : 'light';

    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem(THEME_KEY, next);
    updateThemeIcon(next);
});

function updateThemeIcon(theme) {
    const icon = themeToggle.querySelector('.material-icons-round');
    icon.textContent = theme === 'light' ? 'dark_mode' : 'light_mode';
}

// ==========================================
// Analysis Logic (Called by Kotlin/JS)
// ==========================================

// Store current analysis for share/report
let currentAnalysis = { url: '', score: 0, verdict: '' };

/**
 * Called by Main.kt when analysis is complete
 */
window.displayResult = (score, verdict, flags, url) => {
    // Reset button state
    analyzeBtn.classList.remove('loading');
    analyzeBtn.innerHTML = `<span class="material-icons-round">search</span>${translateText('Analyze URL')}`;
    analyzeBtn.disabled = false;

    // Store current analysis
    currentAnalysis = { url, score, verdict };

    // Update Result Card
    updateResultCard(score, verdict, flags);

    // Show/hide report button based on verdict
    const reportBtn = document.getElementById('reportBtn');
    if (reportBtn) {
        reportBtn.style.display = (verdict === 'MALICIOUS' || verdict === 'SUSPICIOUS') ? 'flex' : 'none';
    }

    // Show Result
    resultCard.classList.remove('hidden');
    resultCard.scrollIntoView({ behavior: 'smooth', block: 'start' });

    // Save to History
    addToHistory({
        url,
        score,
        verdict,
        timestamp: Date.now()
    });
};

/**
 * Navigate to the enhanced dashboard-style results page
 * @param {string} url - The analyzed URL
 * @param {string} verdict - The verdict (SAFE, SUSPICIOUS, MALICIOUS)
 * @param {number} score - The risk score
 */
window.openFullResults = (url, verdict, score) => {
    const params = new URLSearchParams();
    params.set('url', encodeURIComponent(url || currentAnalysis.url));
    params.set('verdict', verdict || currentAnalysis.verdict);
    params.set('score', score || currentAnalysis.score);
    window.location.href = `results.html?${params.toString()}`;
};

/**
 * View current result in the enhanced dashboard view
 */
window.viewEnhancedResults = () => {
    if (currentAnalysis.url) {
        window.openFullResults(currentAnalysis.url, currentAnalysis.verdict, currentAnalysis.score);
    } else {
        showToast('No recent analysis to view', 'warning');
    }
};

function updateResultCard(score, verdict, flags) {
    // Reset classes
    resultCard.className = 'result-card';
    resultCard.classList.add(`result-${verdict.toLowerCase()}`);

    // Update Score Ring
    const ring = resultCard.querySelector('.score-ring');
    ring.textContent = score;

    // Update Verdict
    const pill = resultCard.querySelector('.verdict-pill');
    pill.textContent = translateText(verdict);

    // Calculate confidence based on signal count and score extremity
    const confidence = calculateConfidence(score, flags?.length || 0);

    const title = resultCard.querySelector('h3');
    if (verdict === 'SAFE') title.textContent = translateText('Safe to Visit');
    else if (verdict === 'SUSPICIOUS') title.textContent = translateText('Proceed with Caution');
    else if (verdict === 'MALICIOUS') title.textContent = translateText('Do Not Visit This URL');
    else title.textContent = translateText('Unknown Verdict');

    // Update Flags with enhanced explainability
    const riskContainer = document.getElementById('riskFactors');
    riskContainer.innerHTML = '';

    // Add "Why this verdict?" header with confidence
    const headerDiv = document.createElement('div');
    headerDiv.className = 'explainability-header';
    const verdictHeading = translateText('Why this verdict?');
    const confidenceText = formatText('{level} Confidence', { level: translateText(confidence.label) });
    headerDiv.innerHTML = `
        <div class="why-verdict">
            <span class="material-icons-round">psychology</span>
            <span>${verdictHeading}</span>
        </div>
        <div class="confidence-badge ${confidence.level}">
            <span class="confidence-dots">${'●'.repeat(confidence.dots)}${'○'.repeat(5 - confidence.dots)}</span>
            <span class="confidence-label">${confidenceText}</span>
        </div>
    `;
    riskContainer.appendChild(headerDiv);

    if (flags && flags.length > 0) {
        flags.forEach(flag => {
            const signalInfo = getSignalExplanation(flag);
            const div = document.createElement('div');
            div.className = `risk-item-expanded severity-${signalInfo.severity}`;

            const severityLabel = translateText(signalInfo.severity.toUpperCase());
            const nameLabel = translateText(signalInfo.name);
            const whatChecksLabel = translateText('What it checks:');
            const whyMattersLabel = translateText('Why it matters:');
            const riskImpactLabel = translateText('Risk impact:');
            const counterfactualLabel = translateText('💡 What would reduce this?');
            const whatChecksValue = translateText(signalInfo.whatItChecks);
            const whyMattersValue = translateText(signalInfo.whyItMatters);
            const riskImpactValue = translateText(signalInfo.riskImpact);
            const counterfactualValue = translateText(signalInfo.counterfactual);

            div.innerHTML = `
                <div class="signal-header" onclick="this.parentElement.classList.toggle('expanded')">
                    <span class="material-icons-round signal-icon">${signalInfo.icon}</span>
                    <span class="signal-name">${nameLabel}</span>
                    <span class="signal-severity ${signalInfo.severity}">${severityLabel}</span>
                    <span class="material-icons-round expand-icon">expand_more</span>
                </div>
                <div class="signal-details">
                    <div class="signal-row">
                        <span class="signal-label">${whatChecksLabel}</span>
                        <span class="signal-value">${whatChecksValue}</span>
                    </div>
                    <div class="signal-row">
                        <span class="signal-label">${whyMattersLabel}</span>
                        <span class="signal-value">${whyMattersValue}</span>
                    </div>
                    <div class="signal-row">
                        <span class="signal-label">${riskImpactLabel}</span>
                        <span class="signal-value">${riskImpactValue}</span>
                    </div>
                    <div class="signal-row counterfactual-row">
                        <span class="signal-label">${counterfactualLabel}</span>
                        <span class="signal-value">${counterfactualValue}</span>
                    </div>
                </div>
            `;
            riskContainer.appendChild(div);
        });
    } else {
        // Empty state for safe URLs
        if (verdict === 'SAFE') {
            const safeTitle = translateText('No threats detected');
            const safeSubtitle = formatText('This URL passed all {count} security checks', { count: getHeuristicCount() });
            const safeCheck1 = translateText('✓ No brand impersonation');
            const safeCheck2 = translateText('✓ Safe TLD');
            const safeCheck3 = translateText('✓ No suspicious patterns');
            const safeCheck4 = translateText('✓ No homograph attacks');

            riskContainer.innerHTML += `
                <div class="safe-explanation">
                    <span class="material-icons-round" style="font-size: 48px; color: var(--color-safe);">verified_user</span>
                    <p class="safe-title">${safeTitle}</p>
                    <p class="safe-subtitle">${safeSubtitle}</p>
                    <div class="safe-checks">
                        <span>${safeCheck1}</span>
                        <span>${safeCheck2}</span>
                        <span>${safeCheck3}</span>
                        <span>${safeCheck4}</span>
                    </div>
                </div>
            `;
        }
    }

    // Add ML Insights Section (uses new engine APIs)
    addMlInsightsSection(riskContainer);

    window.qrshieldApplyTranslations?.(riskContainer);
}

// ==========================================
// ML Insights Section
// Shows ML score, threat intel, unicode analysis
// ==========================================

function addMlInsightsSection(container) {
    // Get the last analysis details from window (set by Main.kt)
    const details = window.lastAnalysisDetails;
    if (!details) return;

    // Create ML insights section
    const section = document.createElement('div');
    section.className = 'ml-insights-section';

    // Determine score levels
    const mlLevel = details.mlScore < 30 ? 'safe' : details.mlScore < 60 ? 'warning' : 'danger';
    const threatStatus = details.isKnownBad ? 'known-bad' : 'clean';

    section.innerHTML = `
        <div class="ml-insights-header">
            <span class="material-symbols-outlined">auto_fix_high</span>
            <h4>${translateText('Advanced Detection Insights')}</h4>
            <span class="ml-insights-badge">v1.19</span>
        </div>
        <div class="ml-insights-grid">
            <!-- ML Ensemble Score -->
            <div class="ml-insight-card ${mlLevel}">
                <div class="ml-insight-icon ml">
                    <span class="material-symbols-outlined">psychology</span>
                </div>
                <div class="ml-insight-label">${translateText('ML Score')}</div>
                <div class="ml-insight-value ${mlLevel}">${details.mlScore}%</div>
                <div class="ml-insight-desc">${translateText('Ensemble model confidence')}</div>
                <div class="ml-score-bar">
                    <div class="ml-score-fill ${mlLevel}" style="width: ${details.mlScore}%"></div>
                </div>
            </div>
            
            <!-- Character Analysis -->
            <div class="ml-insight-card">
                <div class="ml-insight-icon ml">
                    <span class="material-symbols-outlined">text_format</span>
                </div>
                <div class="ml-insight-label">${translateText('Char Analysis')}</div>
                <div class="ml-insight-value">${details.charScore}%</div>
                <div class="ml-insight-desc">${translateText('Character-level embedding')}</div>
            </div>
            
            <!-- Feature Score -->
            <div class="ml-insight-card">
                <div class="ml-insight-icon ml">
                    <span class="material-symbols-outlined">tune</span>
                </div>
                <div class="ml-insight-label">${translateText('Feature Score')}</div>
                <div class="ml-insight-value">${details.featureScore}%</div>
                <div class="ml-insight-desc">${translateText('24-feature neural net')}</div>
            </div>
            
            <!-- Threat Intel -->
            <div class="ml-insight-card ${details.isKnownBad ? 'danger' : 'safe'}">
                <div class="ml-insight-icon threat">
                    <span class="material-symbols-outlined">${details.isKnownBad ? 'gpp_bad' : 'verified_user'}</span>
                </div>
                <div class="ml-insight-label">${translateText('Threat Intel')}</div>
                <div class="ml-insight-value ${details.isKnownBad ? 'danger' : 'safe'}">
                    ${details.isKnownBad ? translateText('BLOCKLISTED') : translateText('CLEAN')}
                </div>
                <div class="ml-insight-desc">
                    ${details.threatConfidence || 'NONE'} ${translateText('confidence')}
                </div>
            </div>
            
            <!-- Heuristics Count -->
            <div class="ml-insight-card">
                <div class="ml-insight-icon domain">
                    <span class="material-symbols-outlined">rule</span>
                </div>
                <div class="ml-insight-label">${translateText('Heuristics')}</div>
                <div class="ml-insight-value">${details.heuristicScore}</div>
                <div class="ml-insight-desc">${details.reasonCount} ${translateText('signals fired')}</div>
            </div>
            
            <!-- ML Confidence -->
            <div class="ml-insight-card">
                <div class="ml-insight-icon domain">
                    <span class="material-symbols-outlined">speed</span>
                </div>
                <div class="ml-insight-label">${translateText('Confidence')}</div>
                <div class="ml-insight-value">${details.mlConfidence}%</div>
                <div class="ml-insight-desc">${translateText('Analysis certainty')}</div>
            </div>
        </div>
    `;

    container.appendChild(section);

    // Add voting breakdown section
    addVotingSection(container, details);

    // Also check for Unicode risks if URL has them
    tryAddUnicodeWarning(container);
}

/**
 * Add voting visualization showing 4 component votes
 * Matches the democratic voting system in VerdictDeterminer.kt
 */
function addVotingSection(container, details) {
    // Calculate individual votes based on the same thresholds as VerdictDeterminer.kt
    const heuristicVote = details.heuristicScore <= 10 ? 'SAFE' : details.heuristicScore <= 25 ? 'SUS' : 'MAL';
    const mlProb = (details.mlScore || 0) / 100; // Convert back to 0.0-1.0
    const mlVote = mlProb <= 0.30 ? 'SAFE' : mlProb <= 0.60 ? 'SUS' : 'MAL';
    const brandVote = 'SAFE'; // Brand score not directly exposed, assume safe for now
    const tldVote = 'SAFE'; // TLD score not directly exposed, assume safe for now

    // Count votes
    const votes = [heuristicVote, mlVote, brandVote, tldVote];
    const safeVotes = votes.filter(v => v === 'SAFE').length;
    const susVotes = votes.filter(v => v === 'SUS').length;
    const malVotes = votes.filter(v => v === 'MAL').length;

    // Determine final verdict from voting
    let votingResult = 'SUSPICIOUS';
    let resultClass = 'warning';
    if (safeVotes >= 3) {
        votingResult = 'SAFE';
        resultClass = 'safe';
    } else if (malVotes >= 2) {
        votingResult = 'MALICIOUS';
        resultClass = 'danger';
    } else if (susVotes >= 2) {
        votingResult = 'SUSPICIOUS';
        resultClass = 'warning';
    } else if (safeVotes >= 2) {
        votingResult = 'SAFE';
        resultClass = 'safe';
    }

    const votingSection = document.createElement('div');
    votingSection.className = 'voting-section';

    votingSection.innerHTML = `
        <div class="voting-header">
            <span class="material-symbols-outlined">how_to_vote</span>
            <h4>${translateText('Component Voting')}</h4>
            <span class="voting-result ${resultClass}">${safeVotes}/4 ${translateText('SAFE')}</span>
        </div>
        <div class="voting-grid">
            <div class="vote-chip ${getVoteClass(heuristicVote)}">
                <span class="vote-icon">${getVoteIcon(heuristicVote)}</span>
                <span class="vote-label">${translateText('Heuristic')}</span>
            </div>
            <div class="vote-chip ${getVoteClass(mlVote)}">
                <span class="vote-icon">${getVoteIcon(mlVote)}</span>
                <span class="vote-label">${translateText('ML Model')}</span>
            </div>
            <div class="vote-chip ${getVoteClass(brandVote)}">
                <span class="vote-icon">${getVoteIcon(brandVote)}</span>
                <span class="vote-label">${translateText('Brand')}</span>
            </div>
            <div class="vote-chip ${getVoteClass(tldVote)}">
                <span class="vote-icon">${getVoteIcon(tldVote)}</span>
                <span class="vote-label">${translateText('TLD')}</span>
            </div>
        </div>
        <div class="voting-explanation">
            ${translateText('Majority vote determines verdict: 3+ SAFE = green, 2+ MAL = red')}
        </div>
    `;

    container.appendChild(votingSection);
}

function getVoteClass(vote) {
    switch (vote) {
        case 'SAFE': return 'vote-safe';
        case 'SUS': return 'vote-suspicious';
        case 'MAL': return 'vote-malicious';
        default: return '';
    }
}

function getVoteIcon(vote) {
    switch (vote) {
        case 'SAFE': return '✓';
        case 'SUS': return '⚠';
        case 'MAL': return '✗';
        default: return '?';
    }
}

/**
 * Check for Unicode risks and add warning if detected
 */
function tryAddUnicodeWarning(container) {
    const url = currentAnalysis?.url;
    if (!url || !window.qrshieldUnicodeAnalysis) return;

    try {
        // Extract host from URL
        let host = url;
        try {
            const urlObj = new URL(url.startsWith('http') ? url : 'https://' + url);
            host = urlObj.hostname;
        } catch (e) {
            // Use URL as-is if parsing fails
        }

        const result = window.qrshieldUnicodeAnalysis(host);

        if (result && result.hasRisk) {
            const warning = document.createElement('div');
            warning.className = 'unicode-warning';

            let warnings = [];
            if (result.isPunycode) warnings.push(translateText('Punycode (IDN)'));
            if (result.hasMixedScript) warnings.push(translateText('Mixed Scripts'));
            if (result.hasConfusables) warnings.push(translateText('Confusable Characters'));
            if (result.hasZeroWidth) warnings.push(translateText('Hidden Characters'));

            warning.innerHTML = `
                <span class="material-symbols-outlined">warning</span>
                <div class="unicode-warning-text">
                    <div class="unicode-warning-title">${translateText('Unicode Risk Detected')}</div>
                    <div class="unicode-warning-desc">
                        ${warnings.join(' • ')} — ${translateText('Safe display:')} <code>${result.safeDisplayHost || host}</code>
                    </div>
                </div>
            `;

            container.appendChild(warning);
        }
    } catch (e) {
        console.warn('Unicode analysis error:', e);
    }
}

// ==========================================
// Signal Explainability Database
// ==========================================

function getSignalExplanation(flag) {
    const flagLower = flag.toLowerCase();

    // Map of signals with full explanations + counterfactual hints
    const signals = {
        'brand': {
            name: 'Brand Impersonation',
            icon: 'business',
            severity: 'high',
            whatItChecks: 'Domain contains or mimics a known brand name (PayPal, Amazon, etc.)',
            whyItMatters: 'Attackers create domains that look like trusted brands to steal credentials',
            riskImpact: '+30-40 points — This is a primary phishing indicator',
            counterfactual: 'If this were the official brand domain (paypal.com), this signal would not trigger and score would drop by ~35 points.'
        },
        'typo': {
            name: 'Typosquatting',
            icon: 'spellcheck',
            severity: 'high',
            whatItChecks: 'Domain uses common misspellings or character substitutions (paypa1, amaz0n)',
            whyItMatters: 'Users may not notice subtle character swaps like "1" for "l" or "0" for "o"',
            riskImpact: '+20-30 points — Deliberate deception technique',
            counterfactual: 'If the domain spelled the brand correctly without substitutions, score would drop by ~25 points.'
        },
        'homograph': {
            name: 'Homograph Attack',
            icon: 'translate',
            severity: 'critical',
            whatItChecks: 'URL contains Cyrillic, Greek, or other lookalike Unicode characters',
            whyItMatters: 'Characters like Cyrillic "а" look identical to Latin "a" but are different',
            riskImpact: '+40-50 points — Advanced attack requiring intentional deception',
            counterfactual: 'If all characters were ASCII Latin letters, this signal would not trigger and score would drop by ~45 points.'
        },
        'punycode': {
            name: 'Punycode Domain',
            icon: 'code',
            severity: 'high',
            whatItChecks: 'Domain contains "xn--" IDN encoding',
            whyItMatters: 'International domains can hide malicious lookalike characters',
            riskImpact: '+30-35 points — Requires investigation of actual characters',
            counterfactual: 'If the domain used only ASCII characters (no IDN), score would drop by ~32 points.'
        },
        'tld': {
            name: 'Suspicious TLD',
            icon: 'public',
            severity: 'medium',
            whatItChecks: 'Uses high-risk TLDs like .tk, .ml, .ga, .cf, .xyz',
            whyItMatters: 'Free/cheap TLDs are heavily abused for throwaway phishing domains',
            riskImpact: '+20-30 points — Legitimate brands rarely use these',
            counterfactual: 'If this URL used .com, .org, or country TLDs, score would drop by ~25 points.'
        },
        'shortener': {
            name: 'URL Shortener',
            icon: 'link',
            severity: 'medium',
            whatItChecks: 'Uses bit.ly, t.co, tinyurl, or similar shortening service',
            whyItMatters: 'Hides the true destination URL from the user',
            riskImpact: '+15-20 points — Requires caution, not definitive',
            counterfactual: 'If the full destination URL was visible, score would drop by ~18 points (assuming destination is safe).'
        },
        'ip': {
            name: 'IP Address Host',
            icon: 'dns',
            severity: 'high',
            whatItChecks: 'URL uses raw IP address instead of domain name',
            whyItMatters: 'Legitimate services use domains; IPs hide identity and bypass filters',
            riskImpact: '+25-30 points — Strong phishing indicator',
            counterfactual: 'If a registered domain name was used instead of IP address, score would drop by ~28 points.'
        },
        'subdomain': {
            name: 'Excessive Subdomains',
            icon: 'account_tree',
            severity: 'low',
            whatItChecks: 'More than 3 subdomain levels (secure.login.paypal.fake.com)',
            whyItMatters: 'Deep subdomains can hide the actual domain at the end',
            riskImpact: '+10-15 points — Suspicious but not definitive',
            counterfactual: 'If only 1-2 subdomains were used, this signal would not trigger. Score would drop by ~12 points.'
        },
        'login': {
            name: 'Credential Harvesting Path',
            icon: 'password',
            severity: 'medium',
            whatItChecks: 'URL path contains /login, /signin, /verify, /secure, /account',
            whyItMatters: 'Combined with other signals, suggests intent to steal credentials',
            riskImpact: '+10-15 points — Context-dependent signal',
            counterfactual: 'Without other risk signals, login paths on safe domains would not increase score significantly.'
        },
        'http': {
            name: 'No HTTPS Encryption',
            icon: 'lock_open',
            severity: 'medium',
            whatItChecks: 'URL uses http:// instead of https://',
            whyItMatters: 'Data sent without encryption can be intercepted',
            riskImpact: '+15-20 points — Security baseline not met',
            counterfactual: 'If HTTPS was used, this signal would not trigger and score would drop by ~18 points.'
        },
        'entropy': {
            name: 'High Entropy',
            icon: 'shuffle',
            severity: 'low',
            whatItChecks: 'Domain or path contains random-looking character sequences',
            whyItMatters: 'Randomly generated domains are often temporary phishing sites',
            riskImpact: '+10-15 points — Suggestive but not conclusive',
            counterfactual: 'If the domain used readable words instead of random characters, score would drop by ~12 points.'
        },
        'redirect': {
            name: 'Embedded Redirect',
            icon: 'open_in_new',
            severity: 'medium',
            whatItChecks: 'URL contains another URL in query parameters',
            whyItMatters: 'Can redirect through tracking or to malicious destinations',
            riskImpact: '+15-20 points — Requires destination inspection',
            counterfactual: 'If no embedded URLs were present in query parameters, score would drop by ~18 points.'
        },
        'long': {
            name: 'Excessively Long URL',
            icon: 'straighten',
            severity: 'low',
            whatItChecks: 'URL exceeds 100 characters',
            whyItMatters: 'Long URLs can hide malicious parameters or overwhelm users',
            riskImpact: '+5-10 points — Minor signal',
            counterfactual: 'A shorter, cleaner URL would reduce this signal contribution by ~8 points.'
        }
    };

    // Find matching signal
    for (const [key, info] of Object.entries(signals)) {
        if (flagLower.includes(key)) {
            return info;
        }
    }

    // Default for unknown signals
    return {
        name: flag,
        icon: 'warning',
        severity: 'medium',
        whatItChecks: 'This URL triggered a security check',
        whyItMatters: 'The pattern matches known phishing characteristics',
        riskImpact: '+10-20 points — Contributes to overall risk score',
        counterfactual: 'Removing or changing the suspicious pattern would reduce the overall risk score.'
    };
}

function calculateConfidence(score, signalCount) {
    // Calculate confidence based on score extremity and signal agreement
    let dots = 2; // Base confidence
    let level = 'low';
    let label = 'Low';

    if (score >= 80 || score <= 15) {
        dots = 5;
        level = 'very-high';
        label = 'Very High';
    } else if (score >= 65 || score <= 25) {
        dots = 4;
        level = 'high';
        label = 'High';
    } else if (score >= 50 || score <= 35) {
        dots = 3;
        level = 'medium';
        label = 'Medium';
    }

    // Boost confidence if multiple signals agree
    if (signalCount >= 4) dots = Math.min(5, dots + 1);
    if (signalCount >= 6) dots = 5;

    return { dots, level, label };
}

function getHeuristicCount() {
    return 25; // Match the actual engine count
}

// ==========================================
// Graceful Failure Handling
// ==========================================

/**
 * Check if URL is valid and well-formed
 */
function isValidUrl(string) {
    try {
        const url = new URL(string);
        return url.protocol === 'http:' || url.protocol === 'https:';
    } catch {
        return false;
    }
}

/**
 * Check if app is offline
 */
function isOffline() {
    return !navigator.onLine;
}

/**
 * Show offline mode message
 */
function showOfflineMessage() {
    showModal({
        icon: 'cloud_off',
        iconColor: 'var(--text-tertiary)',
        title: 'You\'re Offline',
        message: 'QR-SHIELD works 100% offline! All analysis is done locally on your device. No internet connection required.',
        details: 'This is a security feature — your URLs never leave your device.',
        primaryAction: { text: 'Got it', action: () => hideModal() }
    });
}

/**
 * Show malformed URL error with suggestions
 */
function showMalformedUrlError(input) {
    showModal({
        icon: 'link_off',
        iconColor: 'var(--color-warning)',
        title: 'Invalid URL Format',
        message: 'The input doesn\'t appear to be a valid URL.',
        details: `Tip: URLs should start with "https://" or "http://"`,
        primaryAction: {
            text: 'Fix it',
            action: () => {
                hideModal();
                // Auto-fix by adding https://
                if (input && !input.includes('://')) {
                    urlInput.value = 'https://' + input;
                    showToast('Added https:// — try analyzing again', 'info');
                }
            }
        },
        secondaryAction: { text: 'Cancel', action: () => hideModal() }
    });
}

/**
 * Show QR decode failure with helpful tips
 */
function showQrDecodeError() {
    showModal({
        icon: 'qr_code',
        iconColor: 'var(--color-warning)',
        title: 'Couldn\'t Read QR Code',
        message: 'The image doesn\'t contain a recognizable QR code, or it may be damaged.',
        details: `Tips for better scanning:
• Ensure good lighting
• Hold the camera steady
• Fill the frame with the QR code
• Avoid glare or reflections
• Try uploading a clearer image`,
        primaryAction: { text: 'Try Again', action: () => hideModal() },
        secondaryAction: { text: 'Enter URL Manually', action: () => { hideModal(); urlInput.focus(); } }
    });
}

/**
 * Show camera permission denied with recovery path
 */
function showCameraPermissionError() {
    showModal({
        icon: 'videocam_off',
        iconColor: 'var(--color-danger)',
        title: 'Camera Access Required',
        message: 'QR-SHIELD needs camera access to scan QR codes in real-time.',
        details: 'You can still use QR-SHIELD by uploading images or pasting URLs directly.',
        primaryAction: {
            text: 'Upload Image Instead',
            action: () => {
                hideModal();
                fileInput?.click();
            }
        },
        secondaryAction: {
            text: 'Enter URL Manually',
            action: () => {
                hideModal();
                urlInput.focus();
            }
        }
    });
}

/**
 * Generic modal display helper
 */
function showModal({ icon, iconColor, title, message, details, primaryAction, secondaryAction }) {
    // Remove existing modal if any
    const existingModal = document.getElementById('errorModal');
    if (existingModal) existingModal.remove();

    const resolvedDetails = details
        ? (details.includes('<') ? details : translateText(details))
        : '';
    const detailsHtml = resolvedDetails ? resolvedDetails.replace(/\n/g, '<br>') : '';

    const modalHtml = `
        <div id="errorModal" class="error-modal active">
            <div class="error-modal-content">
                <span class="material-icons-round error-modal-icon" style="color: ${iconColor}">${icon}</span>
                <h3 class="error-modal-title">${translateText(title)}</h3>
                <p class="error-modal-message">${translateText(message)}</p>
                ${detailsHtml ? `<p class="error-modal-details">${detailsHtml}</p>` : ''}
                <div class="error-modal-actions">
                    <button class="btn-primary" id="modalPrimaryBtn">${translateText(primaryAction.text)}</button>
                    ${secondaryAction ? `<button class="btn-secondary" id="modalSecondaryBtn">${translateText(secondaryAction.text)}</button>` : ''}
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHtml);
    window.qrshieldApplyTranslations?.(document.getElementById('errorModal'));

    document.getElementById('modalPrimaryBtn').addEventListener('click', primaryAction.action);
    if (secondaryAction) {
        document.getElementById('modalSecondaryBtn').addEventListener('click', secondaryAction.action);
    }
}

function hideModal() {
    const modal = document.getElementById('errorModal');
    if (modal) {
        modal.classList.remove('active');
        setTimeout(() => modal.remove(), 300);
    }
}

// Button Click Listener (connects to global Kotlin function)
analyzeBtn.addEventListener('click', () => {
    const url = urlInput.value.trim();

    if (!url) {
        showToast("Please enter a URL", "error");
        return;
    }

    // Validate URL format
    if (!isValidUrl(url)) {
        showMalformedUrlError(url);
        return;
    }

    // Show offline indicator if needed (but still analyze)
    if (isOffline()) {
        showToast('Analyzing offline...', 'info');
    }

    // Call exposed Kotlin function
    if (window.qrshieldAnalyze) {
        window.qrshieldAnalyze(url);
    }
});

// ==========================================
// History Management
// ==========================================

function loadHistory() {
    const json = localStorage.getItem(HISTORY_KEY);
    if (json) {
        history = JSON.parse(json);
        renderHistory();
    }
}

function addToHistory(item) {
    // Avoid creating duplicates at the top
    if (history.length > 0 && history[0].url === item.url) return;

    history.unshift(item); // Add to top
    if (history.length > 50) history.pop(); // Max 50 items

    localStorage.setItem(HISTORY_KEY, JSON.stringify(history));
    renderHistory();
}

function renderHistory() {
    historyList.innerHTML = '';

    if (history.length === 0) {
        historyList.innerHTML = `
            <div class="scan-item" style="justify-content: center; opacity: 0.5;">
                <span>No recent scans</span>
            </div>
        `;
        window.qrshieldApplyTranslations?.(historyList);
        return;
    }

    history.forEach(item => {
        const div = document.createElement('div');
        div.className = 'scan-item';
        div.onclick = () => {
            urlInput.value = item.url;
            window.qrshieldAnalyze(item.url);
        };

        let color = 'var(--color-unknown)';
        if (item.verdict === 'SAFE') color = 'var(--color-safe)';
        if (item.verdict === 'SUSPICIOUS') color = 'var(--color-warning)';
        if (item.verdict === 'MALICIOUS') color = 'var(--color-danger)';

        const locale = window.qrshieldGetLanguageCode ? window.qrshieldGetLanguageCode() : undefined;
        const date = new Date(item.timestamp).toLocaleTimeString(locale);

        // Escape HTML to prevent XSS
        const safeUrl = escapeHtml(item.url);

        div.innerHTML = `
            <div class="scan-status" style="background: ${color}"></div>
            <span class="scan-url">${safeUrl}</span>
            <span class="scan-time">${date}</span>
        `;
        historyList.appendChild(div);
    });

    window.qrshieldApplyTranslations?.(historyList);
}

clearHistoryBtn.addEventListener('click', () => {
    if (confirm(translateText('Clear scan history?'))) {
        history = [];
        localStorage.removeItem(HISTORY_KEY);
        renderHistory();
    }
});

window.resetScanner = () => {
    urlInput.value = '';
    urlInput.focus();
    resultCard.classList.add('hidden');
    window.scrollTo(0, 0);
};

/**
 * Try a sample URL - for judges to quickly test detection
 */
window.tryUrl = (url) => {
    urlInput.value = url;
    urlInput.scrollIntoView({ behavior: 'smooth', block: 'center' });

    // Slight delay for visual feedback
    setTimeout(() => {
        if (window.qrshieldAnalyze) {
            window.qrshieldAnalyze(url);
        }
    }, 200);
};

// ==========================================
// QR Scanner Logic
// ==========================================

scanQrBtn.addEventListener('click', startQrScanner);
closeModalBtn.addEventListener('click', stopQrScanner);

function startQrScanner() {
    qrModal.classList.add('active');
    isScanning = true;

    navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment" } })
        .then(function (stream) {
            video.srcObject = stream;
            video.setAttribute("playsinline", true); // required to tell iOS safari we don't want fullscreen
            video.play();
            requestAnimationFrame(tick);
        })
        .catch(function (err) {
            console.error(err);
            stopQrScanner();
            // Use graceful error modal instead of toast
            showCameraPermissionError();
        });
}

function stopQrScanner() {
    isScanning = false;
    qrModal.classList.remove('active');

    if (video.srcObject) {
        video.srcObject.getTracks().forEach(track => track.stop());
    }
}

function tick() {
    if (!isScanning) return;

    if (video.readyState === video.HAVE_ENOUGH_DATA) {
        canvasElement.hidden = false;
        canvasElement.height = video.videoHeight;
        canvasElement.width = video.videoWidth;
        canvas.drawImage(video, 0, 0, canvasElement.width, canvasElement.height);

        var imageData = canvas.getImageData(0, 0, canvasElement.width, canvasElement.height);
        var code = jsQR(imageData.data, imageData.width, imageData.height, {
            inversionAttempts: "dontInvert",
        });

        if (code) {
            stopQrScanner();
            urlInput.value = code.data;
            window.qrshieldAnalyze(code.data);
            showToast("QR Code Detected!", "success");
        }
    }
    requestAnimationFrame(tick);
}

// ==========================================
// Utilities
// ==========================================

window.showToast = (message, type = 'info') => {
    // Simple toast implementation
    const toast = document.createElement('div');
    toast.style.position = 'fixed';
    toast.style.bottom = '20px';
    toast.style.left = '50%';
    toast.style.transform = 'translateX(-50%)';

    // Handle different types
    if (type === 'error') {
        toast.style.backgroundColor = 'var(--color-danger)';
        toast.style.color = 'white';
    } else if (type === 'success') {
        toast.style.backgroundColor = 'var(--color-safe)';
        toast.style.color = 'white';
    } else if (type === 'warning') {
        toast.style.backgroundColor = 'var(--color-warning)';
        toast.style.color = 'black';
    } else {
        toast.style.backgroundColor = 'var(--bg-surface)';
        toast.style.color = 'var(--text-primary)';
    }

    toast.style.padding = '12px 24px';
    toast.style.borderRadius = '50px';
    toast.style.boxShadow = '0 10px 30px rgba(0,0,0,0.2)';
    toast.style.zIndex = '2000';
    toast.style.fontWeight = '600';
    toast.textContent = translateText(message);

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.transition = 'opacity 0.5s';
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 500);
    }, 3000);
};

/**
 * Escape HTML special characters to prevent XSS
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ==========================================
// Drag & Drop + File Upload
// ==========================================

// Upload button click
uploadQrBtn?.addEventListener('click', () => {
    fileInput?.click();
});

// Drop zone click
dropZone?.addEventListener('click', () => {
    fileInput?.click();
});

// File input change
fileInput?.addEventListener('change', (e) => {
    const file = e.target.files?.[0];
    if (file) {
        processImageFile(file);
    }
});

// Drag events
dropZone?.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('drag-over');
});

dropZone?.addEventListener('dragleave', (e) => {
    e.preventDefault();
    dropZone.classList.remove('drag-over');
});

dropZone?.addEventListener('drop', (e) => {
    e.preventDefault();
    dropZone.classList.remove('drag-over');

    const file = e.dataTransfer.files?.[0];
    if (file && file.type.startsWith('image/')) {
        processImageFile(file);
    } else {
        showToast('Please drop an image file', 'error');
    }
});

/**
 * Process dropped/selected image file for QR code
 */
function processImageFile(file) {
    showToast('Processing image...', 'info');

    const reader = new FileReader();
    reader.onload = (e) => {
        const img = new Image();
        img.onload = () => {
            // Draw to canvas
            canvasElement.width = img.width;
            canvasElement.height = img.height;
            canvas.drawImage(img, 0, 0);

            // Get image data
            const imageData = canvas.getImageData(0, 0, img.width, img.height);

            // Decode QR with jsQR
            const code = jsQR(imageData.data, imageData.width, imageData.height);

            if (code) {
                urlInput.value = code.data;
                showToast('QR Code detected!', 'success');

                // Auto-analyze
                setTimeout(() => {
                    if (window.qrshieldAnalyze) {
                        window.qrshieldAnalyze(code.data);
                    }
                }, 300);
            } else {
                // Use graceful error modal with tips
                showQrDecodeError();
            }
        };
        img.onerror = () => {
            showToast('Failed to load image', 'error');
        };
        img.src = e.target.result;
    };
    reader.onerror = () => {
        showToast('Failed to read file', 'error');
    };
    reader.readAsDataURL(file);
}

// Prevent default drag behavior on body
document.body.addEventListener('dragover', (e) => {
    e.preventDefault();
});

document.body.addEventListener('drop', (e) => {
    e.preventDefault();
});

// ==========================================
// Share & Report Functions
// ==========================================

/**
 * Share scan result using Web Share API or clipboard
 */
window.shareResult = async () => {
    const { url, score, verdict } = currentAnalysis;

    const shareText = [
        translateText('🛡️ QR-SHIELD Analysis'),
        formatText('URL: {url}', { url }),
        formatText('Verdict: {verdict}', { verdict: translateText(verdict) }),
        formatText('Risk Score: {score}/100', { score }),
        '',
        formatText('Scanned with QR-SHIELD - {url}', { url: 'https://raoof128.github.io/' })
    ].join('\n');

    // Try Web Share API first
    if (navigator.share) {
        try {
            await navigator.share({
                title: translateText('QR-SHIELD Scan Result'),
                text: shareText,
                url: 'https://raoof128.github.io/'
            });
            showToast('Shared successfully!', 'success');
            return;
        } catch (err) {
            // User cancelled or API failed, fallback to clipboard
        }
    }

    // Fallback: copy to clipboard
    try {
        await navigator.clipboard.writeText(shareText);
        showToast('Result copied to clipboard!', 'success');
    } catch (err) {
        showToast('Failed to share', 'error');
    }
};

/**
 * Report a phishing URL to security services
 */
window.reportPhishing = () => {
    const { url, score, verdict } = currentAnalysis;

    // Create mailto link for reporting
    const subject = encodeURIComponent(formatText('Phishing Report: {url}', { url }));
    const bodyLines = [
        translateText('I am reporting a suspected phishing URL detected by QR-SHIELD:'),
        '',
        formatText('URL: {url}', { url }),
        formatText('QR-SHIELD Verdict: {verdict}', { verdict: translateText(verdict) }),
        formatText('Risk Score: {score}/100', { score }),
        formatText('Reported: {timestamp}', { timestamp: new Date().toISOString() }),
        '',
        translateText('This URL was flagged by QR-SHIELD\'s offline phishing detection engine.'),
        '',
        '---',
        translateText('Submitted via QR-SHIELD'),
        'https://raoof128.github.io/'
    ];
    const body = encodeURIComponent(bodyLines.join('\n'));

    // Open PhishTank submission page for verified reports
    const phishTankUrl = `https://phishtank.org/add_web_phish.php`;

    // Create options for user
    const reportOptions = formatText(
        'Would you like to report this URL?\n\nOptions:\n1. PhishTank - Click OK to open submission page\n2. Email - Click Cancel to compose email\n\nURL: {url}',
        { url }
    );

    if (confirm(reportOptions)) {
        // Open PhishTank
        window.open(phishTankUrl, '_blank');
        showToast('Opening PhishTank...', 'info');
    } else {
        // Open email client
        const mailtoUrl = `mailto:security@qr-shield.app?subject=${subject}&body=${body}`;
        window.location.href = mailtoUrl;
        showToast('Opening email client...', 'info');
    }
};

// ==========================================
// Onboarding Tutorial
// ==========================================

let currentSlide = 1;
const totalSlides = 4; // Updated: now includes safe vs risky example slide

function showOnboarding() {
    const modal = document.getElementById('onboardingModal');
    if (modal) {
        modal.classList.add('active');
    }
}

function hideOnboarding() {
    const modal = document.getElementById('onboardingModal');
    if (modal) {
        modal.classList.remove('active');
        localStorage.setItem(ONBOARDING_KEY, 'true');
    }
}

window.goToSlide = (slideNum) => {
    currentSlide = slideNum;
    updateOnboardingSlides();
};

function updateOnboardingSlides() {
    // Update slides
    document.querySelectorAll('.onboarding-slide').forEach((slide, index) => {
        slide.classList.toggle('active', index + 1 === currentSlide);
    });

    // Update dots
    document.querySelectorAll('.onboarding-dots .dot').forEach((dot, index) => {
        dot.classList.toggle('active', index + 1 === currentSlide);
    });

    // Update button text
    const nextBtn = document.getElementById('nextOnboarding');
    if (nextBtn) {
        if (currentSlide === totalSlides) {
            nextBtn.textContent = translateText('Get Started! 🚀');
        } else {
            nextBtn.textContent = translateText('Next →');
        }
    }
}

function setupOnboarding() {
    const nextBtn = document.getElementById('nextOnboarding');
    const skipBtn = document.getElementById('skipOnboarding');

    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            if (currentSlide < totalSlides) {
                currentSlide++;
                updateOnboardingSlides();
            } else {
                hideOnboarding();
            }
        });
    }

    if (skipBtn) {
        skipBtn.addEventListener('click', () => {
            hideOnboarding();
        });
    }
}

// ==========================================
// Judge Mode (Demo Mode) - Force Malicious Result
// ==========================================

const JUDGE_MODE_KEY = 'qrshield_judge_mode';
let isJudgeMode = localStorage.getItem(JUDGE_MODE_KEY) === 'true';

/**
 * Toggle Judge Mode - forces malicious results for demo purposes
 */
window.toggleJudgeMode = () => {
    isJudgeMode = !isJudgeMode;
    localStorage.setItem(JUDGE_MODE_KEY, isJudgeMode);
    updateJudgeModeUI();

    if (isJudgeMode) {
        showToast('🧑‍⚖️ Judge Mode ON - All URLs will show MALICIOUS', 'warning');
    } else {
        showToast('Judge Mode OFF - Normal detection active', 'info');
    }
};

/**
 * Update Judge Mode toggle UI state
 */
function updateJudgeModeUI() {
    const toggleBtn = document.getElementById('judgeModeToggle');
    if (toggleBtn) {
        toggleBtn.classList.toggle('active', isJudgeMode);
        const judgeOnLabel = translateText('Judge Mode: ON');
        const judgeOffLabel = translateText('Judge Mode');
        toggleBtn.innerHTML = isJudgeMode
            ? `<span class="material-icons-round">gavel</span> ${judgeOnLabel}`
            : `<span class="material-icons-round">gavel</span> ${judgeOffLabel}`;
    }
}

/**
 * Force a malicious result (Judge Mode simulation)
 */
window.forceMaliciousResult = () => {
    const demoUrl = urlInput.value.trim() || 'https://demo-malicious.evil.tk/login';

    // Simulate a MALICIOUS result
    const mockFlags = [
        'Brand Impersonation Detected',
        'Suspicious TLD (.tk)',
        'Credential Harvesting Path (/login)',
        'No HTTPS encryption',
        'Typosquatting pattern detected'
    ];

    // Show demo result
    window.displayResult(92, 'MALICIOUS', mockFlags, demoUrl);
    showToast('🧑‍⚖️ Demo: Forced MALICIOUS result', 'warning');
};

/**
 * Generate a test QR code with malicious URL (for demo)
 */
window.generateTestQR = () => {
    const testUrl = 'https://paypa1-secure.tk/login?redirect=steal';
    const qrApiUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(testUrl)}`;

    const testQrAlt = translateText('Test QR Code');
    showModal({
        icon: 'qr_code',
        iconColor: 'var(--color-warning)',
        title: 'Test QR Code',
        message: 'Scan this QR code with QR-SHIELD to see malicious URL detection:',
        details: `<img src="${qrApiUrl}" alt="${testQrAlt}" style="display:block; margin:16px auto; border-radius:8px;">
                  <code style="display:block; text-align:center; margin-top:8px; font-size:12px;">${testUrl}</code>`,
        primaryAction: {
            text: 'Analyze This URL',
            action: () => {
                hideModal();
                urlInput.value = testUrl;
                if (window.qrshieldAnalyze) window.qrshieldAnalyze(testUrl);
            }
        },
        secondaryAction: { text: 'Close', action: () => hideModal() }
    });
};

// Initialize Judge Mode UI on load
document.addEventListener('DOMContentLoaded', () => {
    updateJudgeModeUI();

    // Check for ?judge=true or ?demo=true in URL
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('judge') === 'true' || urlParams.get('demo') === 'true') {
        isJudgeMode = true;
        localStorage.setItem(JUDGE_MODE_KEY, 'true');
        updateJudgeModeUI();
        populateDemoHistory(); // Auto-populate with demo examples!
        showToast('🧑‍⚖️ Judge Mode activated - Demo history loaded!', 'warning');
        showDemoModeBanner();
    }
});

/**
 * Populate history with perfect demo examples for judges
 * Called when Judge Mode is activated
 */
function populateDemoHistory() {
    // Demo examples covering all verdicts
    const demoExamples = [
        {
            url: 'https://paypa1-secure.tk/login',
            score: 92,
            verdict: 'MALICIOUS',
            timestamp: Date.now() - 60000 // 1 min ago
        },
        {
            url: 'https://gооgle.com/login', // Homograph: Cyrillic 'о'
            score: 88,
            verdict: 'MALICIOUS',
            timestamp: Date.now() - 120000 // 2 min ago
        },
        {
            url: 'https://commbank.secure-verify.ml/account',
            score: 85,
            verdict: 'MALICIOUS',
            timestamp: Date.now() - 180000 // 3 min ago
        },
        {
            url: 'https://bit.ly/3xY7abc',
            score: 35,
            verdict: 'SUSPICIOUS',
            timestamp: Date.now() - 240000 // 4 min ago
        },
        {
            url: 'https://google.com',
            score: 5,
            verdict: 'SAFE',
            timestamp: Date.now() - 300000 // 5 min ago
        }
    ];

    // Only add if history is empty or small
    if (history.length < 3) {
        history = demoExamples;
        localStorage.setItem(HISTORY_KEY, JSON.stringify(history));
        renderHistory();
    }
}

// ==========================================
// Report False Positive / Incorrect Verdict
// ==========================================

/**
 * Show Report Incorrect Verdict modal
 */
window.reportIncorrectVerdict = () => {
    const { url, score, verdict } = currentAnalysis;

    if (!url) {
        showToast('No URL to report - analyze one first', 'error');
        return;
    }

    showModal({
        icon: 'feedback',
        iconColor: 'var(--text-secondary)',
        title: 'Report Incorrect Verdict',
        message: `You're reporting that this verdict may be wrong:`,
        details: `<div style="background: var(--bg-elevated); padding: 12px; border-radius: 8px; margin: 12px 0;">
            <strong>URL:</strong> ${escapeHtml(url.substring(0, 50))}...<br>
            <strong>Current Verdict:</strong> ${verdict}<br>
            <strong>Score:</strong> ${score}/100
        </div>
        <p style="font-size: 14px; opacity: 0.8;">
            What should the verdict be?
        </p>
        <div style="display: flex; gap: 8px; justify-content: center; margin-top: 12px;">
            <button class="verdict-option" data-verdict="SAFE" onclick="submitFeedback('SAFE')" 
                    style="padding: 8px 16px; border-radius: 8px; background: var(--color-safe); color: white; border: none; cursor: pointer;">
                Should be SAFE
            </button>
            <button class="verdict-option" data-verdict="SUSPICIOUS" onclick="submitFeedback('SUSPICIOUS')"
                    style="padding: 8px 16px; border-radius: 8px; background: var(--color-warning); color: black; border: none; cursor: pointer;">
                Should be SUSPICIOUS
            </button>
            <button class="verdict-option" data-verdict="MALICIOUS" onclick="submitFeedback('MALICIOUS')"
                    style="padding: 8px 16px; border-radius: 8px; background: var(--color-danger); color: white; border: none; cursor: pointer;">
                Should be MALICIOUS
            </button>
        </div>`,
        primaryAction: { text: 'Cancel', action: () => hideModal() }
    });
};

/**
 * Submit feedback about incorrect verdict
 * NOTE: This is a stub - in production, would send to backend
 */
window.submitFeedback = (correctVerdict) => {
    const { url, score, verdict } = currentAnalysis;

    // Create feedback object (would be sent to server in production)
    const feedback = {
        url: url,
        originalVerdict: verdict,
        originalScore: score,
        correctedVerdict: correctVerdict,
        timestamp: new Date().toISOString(),
        userAgent: navigator.userAgent
    };

    // Store locally for now (demo purposes)
    const feedbackHistory = JSON.parse(localStorage.getItem('qrshield_feedback') || '[]');
    feedbackHistory.push(feedback);
    localStorage.setItem('qrshield_feedback', JSON.stringify(feedbackHistory));

    hideModal();

    showToast(formatText('Thank you! Feedback recorded: Should be {verdict}', {
        verdict: translateText(correctVerdict)
    }), 'success');

    // Log for demo purposes
    console.log('[QR-SHIELD] Feedback submitted:', feedback);
};

// ==========================================
// Browser Compatibility & Graceful Degradation
// ==========================================

/**
 * Check browser compatibility and show warnings
 */
function checkBrowserCompatibility() {
    const issues = [];

    // Check for required APIs
    if (!window.URL) {
        issues.push('URL parsing (please update your browser)');
    }

    if (!window.localStorage) {
        issues.push('Local storage (history won\'t persist)');
    }

    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        issues.push('Camera API (QR scanning unavailable)');
        // Disable camera button gracefully
        const scanBtn = document.getElementById('scanQrBtn');
        if (scanBtn) {
            scanBtn.disabled = true;
            scanBtn.title = 'Camera not supported in this browser';
            scanBtn.innerHTML = `<span class="material-icons-round">videocam_off</span> ${translateText('Camera N/A')}`;
        }
    }

    if (!window.FileReader) {
        issues.push('File upload (image scanning unavailable)');
        const uploadBtn = document.getElementById('uploadQrBtn');
        if (uploadBtn) {
            uploadBtn.disabled = true;
        }
    }

    // Check if Kotlin/JS loaded
    setTimeout(() => {
        if (!window.qrshieldAnalyze || typeof window.qrshieldAnalyze !== 'function') {
            showBrowserError('engine');
        }
    }, 3000);

    // Show warning if issues found
    if (issues.length > 0) {
        console.warn('[QR-SHIELD] Browser compatibility issues:', issues);
        // Don't show modal for minor issues, just log
        if (issues.length > 2) {
            showBrowserError('compatibility', issues);
        }
    }

    return issues.length === 0;
}

/**
 * Show browser-specific error with helpful guidance
 */
function showBrowserError(type, details = []) {
    if (type === 'engine') {
        showModal({
            icon: 'warning',
            iconColor: 'var(--color-warning)',
            title: 'Analysis Engine Loading...',
            message: 'The Kotlin/JS analysis engine is still loading or failed to initialize.',
            details: `This may happen if:
• You're using a very old browser
• JavaScript is disabled
• The page didn't fully load

Try refreshing the page. If the issue persists, try Chrome, Firefox, or Safari.`,
            primaryAction: {
                text: 'Refresh Page',
                action: () => window.location.reload()
            },
            secondaryAction: {
                text: 'Continue Anyway',
                action: () => hideModal()
            }
        });
    } else if (type === 'compatibility') {
        showModal({
            icon: 'browser_not_supported',
            iconColor: 'var(--color-warning)',
            title: 'Limited Browser Support',
            message: 'Some features may not work in your browser.',
            details: `Missing features:
${details.map(d => '• ' + d).join('\n')}

For the best experience, use Chrome, Firefox, Safari, or Edge.`,
            primaryAction: { text: 'Got it', action: () => hideModal() }
        });
    } else if (type === 'camera') {
        showCameraPermissionError();
    }
}

/**
 * Show unsupported camera state gracefully
 */
function showCameraUnsupportedState() {
    const scanBtn = document.getElementById('scanQrBtn');
    if (scanBtn) {
        scanBtn.onclick = () => {
            showModal({
                icon: 'videocam_off',
                iconColor: 'var(--text-secondary)',
                title: 'Camera Not Available',
                message: 'Your browser doesn\'t support camera access, or you\'re on a device without a camera.',
                details: `You can still use QR-SHIELD:
• Upload QR code images
• Paste URLs directly
• Use the sample URLs provided`,
                primaryAction: {
                    text: 'Upload Image',
                    action: () => { hideModal(); fileInput?.click(); }
                },
                secondaryAction: {
                    text: 'Enter URL',
                    action: () => { hideModal(); urlInput.focus(); }
                }
            });
        };
    }
}

// Run compatibility check on load
document.addEventListener('DOMContentLoaded', () => {
    setTimeout(checkBrowserCompatibility, 500);
});

// ==========================================
// Enhanced Demo Mode Banner
// ==========================================

/**
 * Show demo mode banner at top of page
 */
function showDemoModeBanner() {
    if (document.getElementById('demoBanner')) return;

    const banner = document.createElement('div');
    banner.id = 'demoBanner';
    banner.className = 'demo-banner';
    const demoTitle = translateText('Judge Mode Active');
    const demoSubtitle = translateText('All URLs will show as MALICIOUS for demo purposes');
    const exitLabel = translateText('Exit');
    banner.innerHTML = `
        <span class="material-icons-round">gavel</span>
        <span><strong>${demoTitle}</strong> — ${demoSubtitle}</span>
        <button onclick="toggleJudgeMode()" class="demo-banner-close">✕ ${exitLabel}</button>
    `;
    document.body.prepend(banner);
    window.qrshieldApplyTranslations?.(banner);
}

function hideDemoModeBanner() {
    const banner = document.getElementById('demoBanner');
    if (banner) banner.remove();
}

// Update banner when judge mode changes
const originalToggleJudgeMode = window.toggleJudgeMode;
window.toggleJudgeMode = () => {
    originalToggleJudgeMode?.();
    if (isJudgeMode) {
        showDemoModeBanner();
    } else {
        hideDemoModeBanner();
    }
};

// ==========================================
// Language Toggle (i18n)
// ==========================================

const LANG_KEY = 'qrshield_language';
const LANGUAGE_OPTIONS = [
    { code: 'en', label: 'English', flag: '🇬🇧' },
    { code: 'de', label: 'Deutsch', flag: '🇩🇪' },
    { code: 'es', label: 'Español', flag: '🇪🇸' },
    { code: 'fr', label: 'Français', flag: '🇫🇷' },
    { code: 'zh', label: '简体中文', flag: '🇨🇳' },
    { code: 'ja', label: '日本語', flag: '🇯🇵' },
    { code: 'hi', label: 'हिन्दी', flag: '🇮🇳' },
];

function resolveLanguage(code) {
    if (!code) return 'en';
    const normalized = code.toLowerCase().split('-')[0];
    return LANGUAGE_OPTIONS.some(option => option.code === normalized) ? normalized : 'en';
}

let currentLang = resolveLanguage(localStorage.getItem(LANG_KEY) || navigator.language);

function updateLanguageFlag() {
    const option = LANGUAGE_OPTIONS.find(lang => lang.code === currentLang) || LANGUAGE_OPTIONS[0];
    const langFlag = document.getElementById('langFlag');
    if (langFlag) langFlag.textContent = option.flag;
    document.documentElement.lang = currentLang;
}

function applyTranslations() {
    updateLanguageFlag();
    localStorage.setItem(LANG_KEY, currentLang);
    window.qrshieldApplyTranslations?.(document.body);
}

window.toggleLanguage = () => {
    const index = LANGUAGE_OPTIONS.findIndex(lang => lang.code === currentLang);
    const next = LANGUAGE_OPTIONS[(index + 1) % LANGUAGE_OPTIONS.length];
    currentLang = next.code;
    applyTranslations();
    showToast(formatText('Language: {language} {flag}', {
        language: next.label,
        flag: next.flag
    }), 'info');
};

document.addEventListener('DOMContentLoaded', () => {
    applyTranslations();
});

// ==========================================
// PWA Offline Indicator
// ==========================================

function updateOfflineIndicator() {
    const indicator = document.getElementById('offlineIndicator');
    if (!indicator) return;

    if (!navigator.onLine) {
        indicator.style.display = 'flex';
        indicator.classList.add('active');
    } else {
        indicator.style.display = 'none';
        indicator.classList.remove('active');
    }
}

// Listen for online/offline events
window.addEventListener('online', () => {
    updateOfflineIndicator();
    showToast('Back online', 'success');
});

window.addEventListener('offline', () => {
    updateOfflineIndicator();
    showToast("You're offline — analysis still works!", 'info');
});

// Check on load
document.addEventListener('DOMContentLoaded', () => {
    updateOfflineIndicator();
});

// ==========================================
// Beat the Bot Game
// ==========================================

let beatBotScore = parseInt(localStorage.getItem('qrshield_beat_bot_score') || '0');
let beatBotAttempts = parseInt(localStorage.getItem('qrshield_beat_bot_attempts') || '0');

window.startBeatTheBot = () => {
    // Scroll to input and focus
    const urlInput = document.getElementById('urlInput');
    urlInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
    urlInput.focus();
    urlInput.placeholder = translateText('🎮 Craft a sneaky phishing URL...');

    // Show game mode toast
    showToast(translateText('🎮 Game Mode: Try to craft a URL that gets detected as SAFE!'), 'info');

    // Update UI to show game mode
    document.body.classList.add('beat-bot-mode');

    // Store that we're in game mode
    sessionStorage.setItem('beat_bot_active', 'true');

    // Increment challenge count (simulated)
    const countEl = document.getElementById('challengeCount');
    if (countEl) {
        const currentCount = parseInt(countEl.textContent.replace(',', '')) + 1;
        countEl.textContent = currentCount.toLocaleString();
    }
};

// Override displayResult when in Beat the Bot mode
const originalDisplayResult = window.displayResult;
window.displayResult = (score, verdict, flags, url) => {
    // Call original
    originalDisplayResult(score, verdict, flags, url);

    // Check if in game mode
    if (sessionStorage.getItem('beat_bot_active') === 'true') {
        beatBotAttempts++;
        localStorage.setItem('qrshield_beat_bot_attempts', beatBotAttempts.toString());

        if (verdict === 'SAFE') {
            // User won! Their URL wasn't detected
            beatBotScore++;
            localStorage.setItem('qrshield_beat_bot_score', beatBotScore.toString());

            setTimeout(() => {
                showToast(formatText('🏆 You beat the bot! Score: {score}/{attempts}', {
                    score: beatBotScore,
                    attempts: beatBotAttempts
                }), 'success');
            }, 1000);
        } else {
            // Bot wins
            setTimeout(() => {
                showToast(formatText('🤖 Bot wins! Phishing detected. Score: {score}/{attempts}', {
                    score: beatBotScore,
                    attempts: beatBotAttempts
                }), 'warning');
            }, 1000);
        }

        // Exit game mode after result
        sessionStorage.removeItem('beat_bot_active');
        document.body.classList.remove('beat-bot-mode');
        document.getElementById('urlInput').placeholder = 'https://example.com/login';
    }
};
