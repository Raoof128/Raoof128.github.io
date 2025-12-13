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

    const title = resultCard.querySelector('h3');
    if (verdict === 'SAFE') title.textContent = 'Safe to Visit';
    else if (verdict === 'SUSPICIOUS') title.textContent = 'Suspicious URL';
    else if (verdict === 'MALICIOUS') title.textContent = 'Malicious URL Content';
    else title.textContent = 'Unknown Verdict';

    // Update Flags
    const riskContainer = document.getElementById('riskFactors');
    riskContainer.innerHTML = '';

    if (flags && flags.length > 0) {
        flags.forEach(flag => {
            const div = document.createElement('div');
            div.className = 'risk-item';
            // Use textContent for the flag to prevent XSS
            const iconSpan = document.createElement('span');
            iconSpan.className = 'material-icons-round risk-icon';
            iconSpan.textContent = 'warning';

            const textSpan = document.createElement('span');
            textSpan.className = 'risk-text';
            textSpan.textContent = flag;

            div.appendChild(iconSpan);
            div.appendChild(textSpan);
            riskContainer.appendChild(div);
        });
    } else {
        // Empty state for safe URLs
        if (verdict === 'SAFE') {
            riskContainer.innerHTML = `
                <div style="text-align: center; color: var(--color-safe); padding: 10px;">
                    <span class="material-icons-round" style="font-size: 48px;">check_circle</span>
                    <p>No threats detected</p>
                </div>
            `;
        }
    }
}

// Button Click Listener (connects to global Kotlin function)
analyzeBtn.addEventListener('click', () => {
    const url = urlInput.value.trim();
    if (!url) {
        showToast("Please enter a URL", "error");
        return;
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
            showToast("Camera access denied or unavailable", "error");
            stopQrScanner();
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
                showToast('No QR code found in image', 'warning');
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
const totalSlides = 3;

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
