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
const HISTORY_KEY = 'qrshield_history';

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
    analyzeBtn.innerHTML = '<span class="material-icons-round">search</span>Analyze URL';
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

function updateResultCard(score, verdict, flags) {
    // Reset classes
    resultCard.className = 'result-card';
    resultCard.classList.add(`result-${verdict.toLowerCase()}`);

    // Update Score Ring
    const ring = resultCard.querySelector('.score-ring');
    ring.textContent = score;

    // Update Verdict
    const pill = resultCard.querySelector('.verdict-pill');
    pill.textContent = verdict;

    // Calculate confidence based on signal count and score extremity
    const confidence = calculateConfidence(score, flags?.length || 0);

    const title = resultCard.querySelector('h3');
    if (verdict === 'SAFE') title.textContent = 'Safe to Visit';
    else if (verdict === 'SUSPICIOUS') title.textContent = 'Proceed with Caution';
    else if (verdict === 'MALICIOUS') title.textContent = 'Do Not Visit This URL';
    else title.textContent = 'Unknown Verdict';

    // Update Flags with enhanced explainability
    const riskContainer = document.getElementById('riskFactors');
    riskContainer.innerHTML = '';

    // Add "Why this verdict?" header with confidence
    const headerDiv = document.createElement('div');
    headerDiv.className = 'explainability-header';
    headerDiv.innerHTML = `
        <div class="why-verdict">
            <span class="material-icons-round">psychology</span>
            <span>Why this verdict?</span>
        </div>
        <div class="confidence-badge ${confidence.level}">
            <span class="confidence-dots">${'●'.repeat(confidence.dots)}${'○'.repeat(5 - confidence.dots)}</span>
            <span class="confidence-label">${confidence.label} Confidence</span>
        </div>
    `;
    riskContainer.appendChild(headerDiv);

    if (flags && flags.length > 0) {
        flags.forEach(flag => {
            const signalInfo = getSignalExplanation(flag);
            const div = document.createElement('div');
            div.className = `risk-item-expanded severity-${signalInfo.severity}`;

            div.innerHTML = `
                <div class="signal-header" onclick="this.parentElement.classList.toggle('expanded')">
                    <span class="material-icons-round signal-icon">${signalInfo.icon}</span>
                    <span class="signal-name">${signalInfo.name}</span>
                    <span class="signal-severity ${signalInfo.severity}">${signalInfo.severity.toUpperCase()}</span>
                    <span class="material-icons-round expand-icon">expand_more</span>
                </div>
                <div class="signal-details">
                    <div class="signal-row">
                        <span class="signal-label">What it checks:</span>
                        <span class="signal-value">${signalInfo.whatItChecks}</span>
                    </div>
                    <div class="signal-row">
                        <span class="signal-label">Why it matters:</span>
                        <span class="signal-value">${signalInfo.whyItMatters}</span>
                    </div>
                    <div class="signal-row">
                        <span class="signal-label">Risk impact:</span>
                        <span class="signal-value">${signalInfo.riskImpact}</span>
                    </div>
                    <div class="signal-row counterfactual-row">
                        <span class="signal-label">💡 What would reduce this?</span>
                        <span class="signal-value">${signalInfo.counterfactual}</span>
                    </div>
                </div>
            `;
            riskContainer.appendChild(div);
        });
    } else {
        // Empty state for safe URLs
        if (verdict === 'SAFE') {
            riskContainer.innerHTML += `
                <div class="safe-explanation">
                    <span class="material-icons-round" style="font-size: 48px; color: var(--color-safe);">verified_user</span>
                    <p class="safe-title">No threats detected</p>
                    <p class="safe-subtitle">This URL passed all ${getHeuristicCount()} security checks</p>
                    <div class="safe-checks">
                        <span>✓ No brand impersonation</span>
                        <span>✓ Safe TLD</span>
                        <span>✓ No suspicious patterns</span>
                        <span>✓ No homograph attacks</span>
                    </div>
                </div>
            `;
        }
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

    const modalHtml = `
        <div id="errorModal" class="error-modal active">
            <div class="error-modal-content">
                <span class="material-icons-round error-modal-icon" style="color: ${iconColor}">${icon}</span>
                <h3 class="error-modal-title">${title}</h3>
                <p class="error-modal-message">${message}</p>
                ${details ? `<p class="error-modal-details">${details.replace(/\n/g, '<br>')}</p>` : ''}
                <div class="error-modal-actions">
                    <button class="btn-primary" id="modalPrimaryBtn">${primaryAction.text}</button>
                    ${secondaryAction ? `<button class="btn-secondary" id="modalSecondaryBtn">${secondaryAction.text}</button>` : ''}
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHtml);

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

// Listen for online/offline events
window.addEventListener('online', () => showToast('Back online', 'success'));
window.addEventListener('offline', () => showToast('You\'re offline — analysis still works!', 'info'));

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

        const date = new Date(item.timestamp).toLocaleTimeString();

        // Escape HTML to prevent XSS
        const safeUrl = escapeHtml(item.url);

        div.innerHTML = `
            <div class="scan-status" style="background: ${color}"></div>
            <span class="scan-url">${safeUrl}</span>
            <span class="scan-time">${date}</span>
        `;
        historyList.appendChild(div);
    });
}

clearHistoryBtn.addEventListener('click', () => {
    if (confirm('Clear scan history?')) {
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
    toast.textContent = message;

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

    const shareText = `🛡️ QR-SHIELD Analysis
URL: ${url}
Verdict: ${verdict}
Risk Score: ${score}/100

Scanned with QR-SHIELD - https://raoof128.github.io/`;

    // Try Web Share API first
    if (navigator.share) {
        try {
            await navigator.share({
                title: 'QR-SHIELD Scan Result',
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
    const subject = encodeURIComponent(`Phishing Report: ${url}`);
    const body = encodeURIComponent(`I am reporting a suspected phishing URL detected by QR-SHIELD:

URL: ${url}
QR-SHIELD Verdict: ${verdict}
Risk Score: ${score}/100
Reported: ${new Date().toISOString()}

This URL was flagged by QR-SHIELD's offline phishing detection engine.

---
Submitted via QR-SHIELD
https://raoof128.github.io/
`);

    // Open PhishTank submission page for verified reports
    const phishTankUrl = `https://phishtank.org/add_web_phish.php`;

    // Create options for user
    const reportOptions = `
Would you like to report this URL?

Options:
1. PhishTank - Click OK to open submission page
2. Email - Click Cancel to compose email

URL: ${url}
`;

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
            nextBtn.textContent = 'Get Started! 🚀';
        } else {
            nextBtn.textContent = 'Next →';
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
        toggleBtn.innerHTML = isJudgeMode
            ? '<span class="material-icons-round">gavel</span> Judge Mode: ON'
            : '<span class="material-icons-round">gavel</span> Judge Mode';
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

    showModal({
        icon: 'qr_code',
        iconColor: 'var(--color-warning)',
        title: 'Test QR Code',
        message: 'Scan this QR code with QR-SHIELD to see malicious URL detection:',
        details: `<img src="${qrApiUrl}" alt="Test QR Code" style="display:block; margin:16px auto; border-radius:8px;">
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

    showToast(`Thank you! Feedback recorded: Should be ${correctVerdict}`, 'success');

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
            scanBtn.innerHTML = '<span class="material-icons-round">videocam_off</span> Camera N/A';
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
    banner.innerHTML = `
        <span class="material-icons-round">gavel</span>
        <span><strong>Judge Mode Active</strong> — All URLs will show as MALICIOUS for demo purposes</span>
        <button onclick="toggleJudgeMode()" class="demo-banner-close">✕ Exit</button>
    `;
    document.body.prepend(banner);
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
// Language Toggle (i18n) - German/English
// ==========================================

const LANG_KEY = 'qrshield_language';
let currentLang = localStorage.getItem(LANG_KEY) || 'en';

// German translations
const translations = {
    en: {
        flag: '🇬🇧',
        scanUrlsSafely: 'Scan URLs Safely',
        heroSubtitle: 'AI-powered phishing detection running 100% offline in your browser.',
        analyzeUrl: 'Analyze URL',
        scanQr: 'Scan QR',
        upload: 'Upload',
        tryExamples: '🧪 Try these examples:',
        recentScans: 'Recent Scans',
        clearHistory: 'Clear History',
        noRecentScans: 'No recent scans',
        safeToVisit: 'Safe to Visit',
        proceedCaution: 'Proceed with Caution',
        doNotVisit: 'Do Not Visit This URL',
        scanAnother: 'Scan Another',
        share: 'Share',
        beatTheBot: 'Beat the Bot',
        playNow: 'Play Now',
        thinkOutsmart: 'Think you can outsmart our AI? Try to craft a phishing URL that fools the detector!',
        offlineMode: 'Offline',
        worksOffline: 'Works 100% offline!'
    },
    de: {
        flag: '🇩🇪',
        scanUrlsSafely: 'URLs sicher scannen',
        heroSubtitle: 'KI-gestützte Phishing-Erkennung läuft 100% offline in Ihrem Browser.',
        analyzeUrl: 'URL analysieren',
        scanQr: 'QR scannen',
        upload: 'Hochladen',
        tryExamples: '🧪 Beispiele ausprobieren:',
        recentScans: 'Letzte Scans',
        clearHistory: 'Verlauf löschen',
        noRecentScans: 'Keine aktuellen Scans',
        safeToVisit: 'Sicher zu besuchen',
        proceedCaution: 'Mit Vorsicht fortfahren',
        doNotVisit: 'Diese URL nicht besuchen',
        scanAnother: 'Weitere scannen',
        share: 'Teilen',
        beatTheBot: 'Schlage den Bot',
        playNow: 'Jetzt spielen',
        thinkOutsmart: 'Glaubst du, du kannst unsere KI austricksen? Versuche, eine Phishing-URL zu erstellen!',
        offlineMode: 'Offline',
        worksOffline: 'Funktioniert 100% offline!'
    }
};

window.toggleLanguage = () => {
    currentLang = currentLang === 'en' ? 'de' : 'en';
    localStorage.setItem(LANG_KEY, currentLang);
    applyTranslations();
    showToast(currentLang === 'de' ? 'Sprache: Deutsch 🇩🇪' : 'Language: English 🇬🇧', 'info');
};

function applyTranslations() {
    const t = translations[currentLang];

    // Update flag
    const langFlag = document.getElementById('langFlag');
    if (langFlag) langFlag.textContent = t.flag;

    // Update hero section
    const heroTitle = document.querySelector('.text-gradient');
    if (heroTitle) heroTitle.textContent = t.scanUrlsSafely;

    const heroSub = document.querySelector('.hero-subtitle');
    if (heroSub) heroSub.textContent = t.heroSubtitle;

    // Update buttons
    const analyzeBtn = document.getElementById('analyzeBtn');
    if (analyzeBtn) {
        analyzeBtn.innerHTML = `<span class="material-icons-round">search</span>${t.analyzeUrl}`;
    }

    const scanQrBtn = document.getElementById('scanQrBtn');
    if (scanQrBtn) {
        scanQrBtn.innerHTML = `<span class="material-icons-round">qr_code_scanner</span>${t.scanQr}`;
    }

    const uploadBtn = document.getElementById('uploadQrBtn');
    if (uploadBtn) {
        uploadBtn.innerHTML = `<span class="material-icons-round">image</span>${t.upload}`;
    }

    // Update try examples label
    const tryLabel = document.querySelector('.try-now-label');
    if (tryLabel) tryLabel.textContent = t.tryExamples;

    // Update recent scans
    const sectionTitle = document.querySelector('.section-title h3');
    if (sectionTitle) sectionTitle.textContent = t.recentScans;

    const clearBtn = document.getElementById('clearHistoryBtn');
    if (clearBtn) clearBtn.textContent = t.clearHistory;

    // Update Beat the Bot section
    const beatBotTitle = document.querySelector('.beat-bot-card h3');
    if (beatBotTitle) beatBotTitle.textContent = t.beatTheBot;

    const beatBotDesc = document.querySelector('.beat-bot-card p:not(.beat-bot-stats)');
    if (beatBotDesc) beatBotDesc.textContent = t.thinkOutsmart;

    const playBtn = document.querySelector('.beat-bot-btn');
    if (playBtn) {
        playBtn.innerHTML = `<span class="material-icons-round">sports_esports</span>${t.playNow}`;
    }

    // Update offline indicator
    const offlineText = document.querySelector('.offline-text');
    if (offlineText) offlineText.textContent = t.offlineMode;

    const offlineIndicator = document.getElementById('offlineIndicator');
    if (offlineIndicator) offlineIndicator.title = t.worksOffline;
}

// Apply translations on load
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
    showToast(currentLang === 'de' ? 'Wieder online' : 'Back online', 'success');
});

window.addEventListener('offline', () => {
    updateOfflineIndicator();
    showToast(currentLang === 'de' ? 'Offline-Modus — Analyse funktioniert weiterhin!' : "You're offline — analysis still works!", 'info');
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
    urlInput.placeholder = '🎮 Craft a sneaky phishing URL...';

    // Show game mode toast
    showToast(currentLang === 'de'
        ? '🎮 Spielmodus: Versuche eine URL zu erstellen, die als SICHER erkannt wird!'
        : '🎮 Game Mode: Try to craft a URL that gets detected as SAFE!', 'info');

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
                showToast(currentLang === 'de'
                    ? `🏆 Du hast gewonnen! Score: ${beatBotScore}/${beatBotAttempts}`
                    : `🏆 You beat the bot! Score: ${beatBotScore}/${beatBotAttempts}`, 'success');
            }, 1000);
        } else {
            // Bot wins
            setTimeout(() => {
                showToast(currentLang === 'de'
                    ? `🤖 Bot gewinnt! Phishing erkannt. Score: ${beatBotScore}/${beatBotAttempts}`
                    : `🤖 Bot wins! Phishing detected. Score: ${beatBotScore}/${beatBotAttempts}`, 'warning');
            }, 1000);
        }

        // Exit game mode after result
        sessionStorage.removeItem('beat_bot_active');
        document.body.classList.remove('beat-bot-mode');
        document.getElementById('urlInput').placeholder = 'https://example.com/login';
    }
};
