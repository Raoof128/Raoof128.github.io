// ============================================
// STATE MANAGEMENT
// ============================================

let scanHistory = JSON.parse(localStorage.getItem('qrshield_history') || '[]');
let scanCount = parseInt(localStorage.getItem('qrshield_count') || '0');
let isDarkTheme = localStorage.getItem('qrshield_theme') !== 'light';

// Initialize UI
document.addEventListener('DOMContentLoaded', function () {
    updateThemeUI();
    updateScanCount();
    renderHistory();

    // Focus input
    const urlInput = document.getElementById('urlInput');
    if (urlInput) urlInput.focus();

    // Check if Kotlin/JS engine loaded
    setTimeout(function () {
        if (window.qrshieldAnalyze) {
            console.log('✅ Kotlin/JS engine loaded successfully');
        } else {
            console.warn('⚠️ Kotlin/JS engine not detected');
        }
    }, 1000);
});

// ============================================
// THEME MANAGEMENT
// ============================================

function toggleTheme() {
    isDarkTheme = !isDarkTheme;
    localStorage.setItem('qrshield_theme', isDarkTheme ? 'dark' : 'light');
    updateThemeUI();
}

function updateThemeUI() {
    document.body.classList.toggle('light-theme', !isDarkTheme);
    document.getElementById('themeIcon').textContent = isDarkTheme ? '☀️' : '🌙';
    document.querySelector('.theme-toggle span:last-child').textContent = isDarkTheme ? 'Light' : 'Dark';
}

// ============================================
// URL ANALYSIS (Fallback until Kotlin/JS loads)
// ============================================

function analyzeUrl() {
    const urlInput = document.getElementById('urlInput');
    const url = urlInput.value.trim();

    if (!url) {
        showToast('Please enter a URL to analyze');
        return;
    }

    const btn = document.getElementById('analyzeBtn');
    btn.classList.add('loading');
    btn.innerHTML = '<div class="spinner"></div><span>Analyzing...</span>';
    btn.disabled = true;

    // Check if Kotlin/JS engine is loaded
    if (window.qrshieldAnalyze) {
        // Use Kotlin/JS engine
        try {
            window.qrshieldAnalyze(url);
        } catch (e) {
            console.error('Analysis error:', e);
            showToast('Error analyzing URL');
            resetAnalyzeButton();
        }
    } else {
        // Fallback - Kotlin/JS not loaded yet
        console.warn('Kotlin/JS engine not loaded, retrying...');
        setTimeout(function () {
            if (window.qrshieldAnalyze) {
                window.qrshieldAnalyze(url);
            } else {
                showToast('Engine loading, please try again');
                resetAnalyzeButton();
            }
        }, 500);
    }
}

function resetAnalyzeButton() {
    const btn = document.getElementById('analyzeBtn');
    btn.classList.remove('loading');
    btn.innerHTML = '<span>🔍</span><span>Analyze URL</span>';
    btn.disabled = false;
}

// Called by Kotlin/JS after analysis
function displayResult(score, verdict, flags, url) {
    const resultCard = document.getElementById('resultCard');
    const scoreDisplay = document.getElementById('scoreDisplay');
    const scoreBar = document.getElementById('scoreBar');
    const verdictBadge = document.getElementById('verdictBadge');
    const resultMessage = document.getElementById('resultMessage');
    const resultUrl = document.getElementById('resultUrl');
    const resultIcon = document.getElementById('resultIcon');
    const flagsSection = document.getElementById('flagsSection');
    const flagsList = document.getElementById('flagsList');

    // Remove old classes
    resultCard.classList.remove('safe', 'suspicious', 'malicious');

    // Get verdict info
    let verdictClass, iconSrc, message;
    if (verdict === 'SAFE') {
        verdictClass = 'safe';
        iconSrc = 'assets/shield-safe.svg';
        message = 'This URL appears to be safe';
        vibrate([50]); // Short vibe for safe
    } else if (verdict === 'SUSPICIOUS') {
        verdictClass = 'suspicious';
        iconSrc = 'assets/shield-warning.svg';
        message = 'This URL has some suspicious indicators';
        vibrate([100, 50, 100]); // Low Warning vibe
    } else if (verdict === 'MALICIOUS') {
        verdictClass = 'malicious';
        iconSrc = 'assets/shield-danger.svg';
        message = 'This URL is likely malicious - do not visit!';
        vibrate([200, 100, 200, 100, 200]); // Strong Danger vibe
    } else {
        verdictClass = 'suspicious';
        iconSrc = 'assets/shield-warning.svg';
        message = 'Unable to determine safety level';
    }

    // Play sound if possible (browser restrictions apply)
    try {
        const audio = new Audio(verdict === 'SAFE' ? 'assets/success.mp3' : 'assets/alert.mp3');
        // audio.play().catch(e => {}); // Only works if user interacted
    } catch (e) { }

    // Update UI
    resultCard.classList.add(verdictClass, 'visible');
    scoreDisplay.textContent = score;
    scoreBar.style.width = Math.min(score, 100) + '%';
    verdictBadge.textContent = verdict;
    resultMessage.textContent = message;
    resultUrl.textContent = url;
    document.getElementById('resultIconImg').src = iconSrc;

    // Update flags
    if (flags && flags.length > 0) {
        flagsList.innerHTML = '';
        flags.forEach(function (flag) {
            const li = document.createElement('li');
            li.className = 'flag-item';
            li.innerHTML = '<span>•</span><span>' + escapeHtml(flag) + '</span>';
            flagsList.appendChild(li);
        });
        flagsSection.style.display = 'block';
    } else {
        flagsSection.style.display = 'none';
    }

    // Update scan count
    scanCount++;
    localStorage.setItem('qrshield_count', scanCount);
    updateScanCount();

    // Add to history
    addToHistory(url, score, verdict);

    // Reset button
    resetAnalyzeButton();

    // Scroll to result
    resultCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// ============================================
// HISTORY MANAGEMENT
// ============================================

function addToHistory(url, score, verdict) {
    // Avoid duplicates at the top
    if (scanHistory.length > 0 && scanHistory[0].url === url) return;

    scanHistory.unshift({ url, score, verdict, timestamp: Date.now() });
    scanHistory = scanHistory.slice(0, 10); // Keep last 10
    localStorage.setItem('qrshield_history', JSON.stringify(scanHistory));
    renderHistory();
}

function renderHistory() {
    const section = document.getElementById('historySection');
    const list = document.getElementById('historyList');

    if (scanHistory.length === 0) {
        section.classList.remove('visible');
        return;
    }

    section.classList.add('visible');
    list.innerHTML = '';

    scanHistory.forEach(function (item) {
        const verdictClass = item.verdict.toLowerCase();
        const div = document.createElement('div');
        div.className = 'history-item';
        div.onclick = function () { loadFromHistory(item.url); };
        div.innerHTML = `
            <div class="history-indicator ${verdictClass}"></div>
            <div class="history-url">${escapeHtml(item.url)}</div>
            <div class="history-score ${verdictClass}">${item.score}</div>
        `;
        list.appendChild(div);
    });
}

function loadFromHistory(url) {
    document.getElementById('urlInput').value = url;
    document.getElementById('urlInput').focus();
    analyzeUrl(); // Auto verify history items
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

function updateScanCount() {
    document.getElementById('scanCount').textContent = scanCount;
}

async function pasteFromClipboard() {
    try {
        const text = await navigator.clipboard.readText();
        document.getElementById('urlInput').value = text;
        showToast('Pasted from clipboard');
    } catch (err) {
        showToast('Could not access clipboard');
    }
}

function clearInput() {
    document.getElementById('urlInput').value = '';
    document.getElementById('resultCard').classList.remove('visible');
    document.getElementById('urlInput').focus();
}

function showToast(message) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.classList.add('visible');
    vibrate(30);
    setTimeout(function () {
        toast.classList.remove('visible');
    }, 2500);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function vibrate(pattern) {
    if (navigator.vibrate) {
        navigator.vibrate(pattern);
    }
}

async function shareResult() {
    const url = document.getElementById('resultUrl').textContent;
    const verdict = document.getElementById('verdictBadge').textContent;

    if (navigator.share) {
        try {
            await navigator.share({
                title: 'QR-SHIELD Analysis',
                text: `QR-SHIELD Analysis: ${verdict} - ${url}`,
                url: window.location.href
            });
        } catch (err) {
            console.log('Share canceled');
        }
    } else {
        // Fallback copy
        try {
            await navigator.clipboard.writeText(`QR-SHIELD Analysis: ${verdict} - ${url}`);
            showToast('Result copied to clipboard');
        } catch (e) {
            showToast('Could not share result');
        }
    }
}

// Handle Enter key
document.getElementById('urlInput').addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        analyzeUrl();
    }
});

// ============================================
// SCANNING CAPABILITIES
// ============================================

let videoStream = null;
let isScanning = false;

function startCameraScan() {
    const modal = document.getElementById('scanModal');
    const video = document.getElementById('scanVideo');
    const status = document.getElementById('scanStatus');

    modal.classList.add('visible');
    status.textContent = "Requesting camera access...";

    navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment" } })
        .then(function (stream) {
            videoStream = stream;
            video.srcObject = stream;
            video.setAttribute("playsinline", true);
            video.play();

            isScanning = true;
            requestAnimationFrame(tick);
            status.textContent = "Point camera at QR code";
        })
        .catch(function (err) {
            console.error("Camera error:", err);
            showToast("Camera access denied or unavailable");
            stopCameraScan();
        });
}

function stopCameraScan() {
    const modal = document.getElementById('scanModal');
    const video = document.getElementById('scanVideo');

    isScanning = false;
    modal.classList.remove('visible');

    if (videoStream) {
        videoStream.getTracks().forEach(track => track.stop());
        videoStream = null;
    }
    video.srcObject = null;
}

function tick() {
    if (!isScanning) return;

    const video = document.getElementById('scanVideo');
    const canvas = document.getElementById('scanCanvas');
    const context = canvas.getContext('2d');
    const status = document.getElementById('scanStatus');

    if (video.readyState === video.HAVE_ENOUGH_DATA) {
        canvas.height = video.videoHeight;
        canvas.width = video.videoWidth;
        context.drawImage(video, 0, 0, canvas.width, canvas.height);

        const imageData = context.getImageData(0, 0, canvas.width, canvas.height);

        // Attempt to decode
        const code = jsQR(imageData.data, imageData.width, imageData.height, {
            inversionAttempts: "dontInvert",
        });

        if (code) {
            stopCameraScan();
            vibrate(50); // Feedback
            const url = code.data;
            document.getElementById('urlInput').value = url;
            showToast("QR Code detected!");
            setTimeout(() => analyzeUrl(), 300);
        } else {
            status.textContent = "Scanning...";
        }
    }

    requestAnimationFrame(tick);
}

function triggerFileUpload() {
    document.getElementById('fileInput').click();
}

function handleFileUpload(input) {
    if (input.files && input.files[0]) {
        const file = input.files[0];
        const reader = new FileReader();

        reader.onload = function (e) {
            const img = new Image();
            img.onload = function () {
                const canvas = document.createElement('canvas');
                const context = canvas.getContext('2d');
                canvas.width = img.width;
                canvas.height = img.height;
                context.drawImage(img, 0, 0);

                const imageData = context.getImageData(0, 0, canvas.width, canvas.height);
                const code = jsQR(imageData.data, imageData.width, imageData.height);

                if (code) {
                    document.getElementById('urlInput').value = code.data;
                    showToast("QR Code loaded from image!");
                    vibrate(50);
                    analyzeUrl();
                } else {
                    showToast("No QR code found in image");
                    vibrate([50, 50]); // Error vibe
                }
            };
            img.src = e.target.result;
        };

        reader.readAsDataURL(file);
    }
    input.value = '';
}
