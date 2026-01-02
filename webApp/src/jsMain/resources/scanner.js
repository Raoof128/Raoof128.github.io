/**
 * Mehr Guard Scanner Dashboard Controller
 * 
 * Handles camera access, QR scanning, URL input, and scan history.
 * Integrates with Kotlin/JS PhishingEngine for analysis.
 * 
 * @author Mehr Guard Team
 * @version 2.4.0
 */

// =============================================================================
// CONFIGURATION
// =============================================================================

const ScannerConfig = {
    version: '2.4.0',
    scanInterval: 100, // ms between scan attempts
    defaultLatency: 4,
};

// =============================================================================
// STATE
// =============================================================================

const ScannerState = {
    isScanning: false,
    isCameraActive: false,
    stream: null,
    scanAnimationFrame: null,
    isSidebarOpen: false,
    isTorchOn: false,
};

function translateText(text) {
    if (window.mehrguardTranslateText) {
        return window.mehrguardTranslateText(text);
    }
    return text;
}

function formatText(template, params) {
    if (window.mehrguardFormatText) {
        return window.mehrguardFormatText(template, params);
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

    // Scanner
    scannerViewport: null,
    cameraFeed: null,
    scanCanvas: null,
    scanLine: null,
    liveStatus: null,
    emptyState: null,
    scanningState: null,
    enableCameraBtn: null,

    // Actions
    torchBtn: null,
    galleryBtn: null,
    pasteUrlBtn: null,
    imageInput: null,

    // Modal
    urlModal: null,
    urlInputField: null,
    analyzeUrlBtn: null,
    closeUrlModal: null,

    // Stats
    latencyBadge: null,
    scansList: null,
    viewAllBtn: null,

    // Toast
    toast: null,
    toastMessage: null,
};

// =============================================================================
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('[Mehr Guard Scanner] Initializing v' + ScannerConfig.version);

    // Cache DOM elements
    cacheElements();

    // Setup event listeners
    setupEventListeners();

    // Load scan history
    renderHistory();

    // Initialize Kotlin engine callback
    setupKotlinBridge();

    window.mehrguardApplyTranslations?.(document.body);
    
    // Handle demo_url parameter from Judge Demo Mode
    const urlParams = new URLSearchParams(window.location.search);
    const demoUrl = urlParams.get('demo_url');
    if (demoUrl) {
        console.log('[Mehr Guard Scanner] Demo URL detected:', demoUrl);
        // Small delay to ensure everything is loaded
        setTimeout(() => {
            analyzeUrlDirectly(demoUrl);
        }, 500);
    }

    console.log('[Mehr Guard Scanner] Ready');
});

/**
 * Cache frequently accessed DOM elements
 */
function cacheElements() {
    elements.sidebar = document.getElementById('sidebar');
    elements.menuToggle = document.getElementById('menuToggle');
    elements.scannerViewport = document.getElementById('scannerViewport');
    elements.cameraFeed = document.getElementById('cameraFeed');
    elements.scanCanvas = document.getElementById('scanCanvas');
    elements.scanLine = document.getElementById('scanLine');
    elements.liveStatus = document.getElementById('liveStatus');
    elements.emptyState = document.getElementById('emptyState');
    elements.scanningState = document.getElementById('scanningState');
    elements.enableCameraBtn = document.getElementById('enableCameraBtn');
    elements.torchBtn = document.getElementById('torchBtn');
    elements.galleryBtn = document.getElementById('galleryBtn');
    elements.pasteUrlBtn = document.getElementById('pasteUrlBtn');
    elements.imageInput = document.getElementById('imageInput');
    elements.urlModal = document.getElementById('urlModal');
    elements.urlInputField = document.getElementById('urlInputField');
    elements.analyzeUrlBtn = document.getElementById('analyzeUrlBtn');
    elements.closeUrlModal = document.getElementById('closeUrlModal');
    elements.latencyBadge = document.getElementById('latencyBadge');
    elements.scansList = document.getElementById('scansList');
    elements.viewAllBtn = document.getElementById('viewAllBtn');
    elements.toast = document.getElementById('toast');
    elements.toastMessage = document.getElementById('toastMessage');
}

/**
 * Setup all event listeners
 */
function setupEventListeners() {
    // Mobile menu toggle
    elements.menuToggle?.addEventListener('click', toggleSidebar);

    // Camera controls
    elements.enableCameraBtn?.addEventListener('click', enableCamera);
    elements.torchBtn?.addEventListener('click', toggleTorch);
    elements.galleryBtn?.addEventListener('click', openGallery);
    elements.pasteUrlBtn?.addEventListener('click', openUrlModal);

    // File input
    elements.imageInput?.addEventListener('change', handleImageUpload);

    // URL Modal
    elements.closeUrlModal?.addEventListener('click', closeUrlModal);
    elements.analyzeUrlBtn?.addEventListener('click', analyzeUrlFromModal);
    elements.urlInputField?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') analyzeUrlFromModal();
    });

    // View all history
    elements.viewAllBtn?.addEventListener('click', () => {
        window.location.href = 'threat.html#scan-history';
    });

    // Drag and drop
    setupDragAndDrop();

    // Keyboard shortcuts
    setupKeyboardShortcuts();

    // Click outside sidebar to close (mobile)
    document.addEventListener('click', (e) => {
        if (ScannerState.isSidebarOpen &&
            !elements.sidebar?.contains(e.target) &&
            !elements.menuToggle?.contains(e.target)) {
            closeSidebar();
        }
    });

    // Click outside modal to close
    elements.urlModal?.addEventListener('click', (e) => {
        if (e.target === elements.urlModal) closeUrlModal();
    });
}

/**
 * Navigate to the results page with scan data
 * @param {string} url - The analyzed URL
 * @param {string} verdict - The verdict (SAFE, SUSPICIOUS, MALICIOUS)
 * @param {number} score - The risk score
 */
function navigateToResults(url, verdict, score) {
    const params = new URLSearchParams();
    params.set('url', encodeURIComponent(url));
    params.set('verdict', verdict);
    params.set('score', score);

    console.log('[Scanner] Navigating to results:', { url, verdict, score });
    window.location.href = `results.html?${params.toString()}`;
}

// Also expose as window.openFullResults for compatibility with other pages
window.openFullResults = navigateToResults;

/**
 * Setup Kotlin/JS bridge
 */
function setupKotlinBridge() {
    // Wait for Kotlin engine
    if (!window.mehrguardAnalyze) {
        window.mehrguardAnalyze = (url) => {
            console.warn('[Scanner] Kotlin engine not ready');
            showToast('Engine initializing...', 'warning');
        };
    }

    // Override displayResult to handle results
    const originalDisplayResult = window.displayResult;
    window.displayResult = (score, verdict, flags, url) => {
        console.log('[Scanner] Analysis complete:', { url, verdict, score, flags });

        // Stop scanning animation
        hideScanningState();

        // Add to history via Shared UI
        if (window.MehrGuardUI && window.MehrGuardUI.addScanToHistory) {
            window.MehrGuardUI.addScanToHistory({
                url: url,
                verdict: verdict === 'MALICIOUS' ? 'HIGH' :
                    verdict === 'SUSPICIOUS' ? 'MEDIUM' :
                        verdict === 'SAFE' ? 'SAFE' : 'LOW',
                score: score || 0,
                signals: flags || []
            });
        }

        // Update local list
        renderHistory();

        // Navigate to results page
        navigateToResults(url, verdict, score);

        // Also call original if exists
        if (originalDisplayResult) {
            originalDisplayResult(score, verdict, flags, url);
        }
    };
}

// =============================================================================
// SIDEBAR CONTROLS
// =============================================================================

function toggleSidebar() {
    if (ScannerState.isSidebarOpen) {
        closeSidebar();
    } else {
        openSidebar();
    }
}

function openSidebar() {
    elements.sidebar?.classList.add('open');
    ScannerState.isSidebarOpen = true;
}

function closeSidebar() {
    elements.sidebar?.classList.remove('open');
    ScannerState.isSidebarOpen = false;
}

// =============================================================================
// CAMERA CONTROLS
// =============================================================================

/**
 * Enable camera and start scanning
 */
async function enableCamera() {
    if (ScannerState.isCameraActive) return;

    try {
        console.log('[Scanner] Requesting camera access...');

        const stream = await navigator.mediaDevices.getUserMedia({
            video: {
                facingMode: 'environment',
                width: { ideal: 1280 },
                height: { ideal: 720 },
            }
        });

        ScannerState.stream = stream;
        ScannerState.isCameraActive = true;

        // Setup video element
        elements.cameraFeed.srcObject = stream;
        elements.cameraFeed.setAttribute('playsinline', 'true');
        await elements.cameraFeed.play();

        // Update UI
        elements.cameraFeed.classList.add('active');
        elements.emptyState?.classList.add('hidden');
        updateLiveStatus('LIVE', 'connected');

        // Start scanning
        startScanning();

        showToast('Camera activated', 'success');

    } catch (error) {
        console.error('[Scanner] Camera error:', error);

        if (error.name === 'NotAllowedError') {
            showToast('Camera permission denied', 'error');
        } else if (error.name === 'NotFoundError') {
            showToast('No camera found', 'error');
        } else {
            showToast(formatText('Camera error: {error}', { error: error.message }), 'error');
        }
    }
}

/**
 * Stop camera and scanning
 */
function stopCamera() {
    stopScanning();

    if (ScannerState.stream) {
        ScannerState.stream.getTracks().forEach(track => track.stop());
        ScannerState.stream = null;
    }

    ScannerState.isCameraActive = false;
    elements.cameraFeed?.classList.remove('active');
    elements.emptyState?.classList.remove('hidden');
    updateLiveStatus('DISCONNECTED', 'disconnected');
}

/**
 * Toggle flashlight/torch
 */
async function toggleTorch() {
    if (!ScannerState.stream) {
        showToast('Enable camera first', 'warning');
        return;
    }

    try {
        const track = ScannerState.stream.getVideoTracks()[0];
        const capabilities = track.getCapabilities();

        if (!capabilities.torch) {
            showToast('Torch not available', 'warning');
            return;
        }

        ScannerState.isTorchOn = !ScannerState.isTorchOn;
        await track.applyConstraints({
            advanced: [{ torch: ScannerState.isTorchOn }]
        });

        // Update button state
        elements.torchBtn?.classList.toggle('active', ScannerState.isTorchOn);
        showToast(ScannerState.isTorchOn ? 'Torch on' : 'Torch off', 'success');

    } catch (error) {
        console.error('[Scanner] Torch error:', error);
        showToast('Torch error', 'error');
    }
}

// =============================================================================
// QR SCANNING
// =============================================================================

/**
 * Start QR code scanning loop
 */
function startScanning() {
    if (ScannerState.isScanning) return;

    ScannerState.isScanning = true;
    console.log('[Scanner] Started scanning');

    requestAnimationFrame(scanFrame);
}

/**
 * Stop scanning loop
 */
function stopScanning() {
    ScannerState.isScanning = false;

    if (ScannerState.scanAnimationFrame) {
        cancelAnimationFrame(ScannerState.scanAnimationFrame);
        ScannerState.scanAnimationFrame = null;
    }
}

/**
 * Process a single video frame for QR codes
 */
function scanFrame() {
    if (!ScannerState.isScanning) return;

    const video = elements.cameraFeed;
    const canvas = elements.scanCanvas;

    if (video.readyState === video.HAVE_ENOUGH_DATA) {
        // Set canvas size to match video
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;

        const ctx = canvas.getContext('2d');
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

        // Get image data for jsQR
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);

        // Scan for QR code
        const code = jsQR(imageData.data, imageData.width, imageData.height, {
            inversionAttempts: 'dontInvert'
        });

        if (code) {
            console.log('[Scanner] QR detected:', code.data);
            handleQRDetected(code.data);
            return; // Stop scanning after detection
        }
    }

    // Continue scanning
    ScannerState.scanAnimationFrame = requestAnimationFrame(scanFrame);
}

/**
 * Handle detected QR code
 */
function handleQRDetected(data) {
    // Stop scanning temporarily
    stopScanning();

    // Show scanning state
    showScanningState();

    // Vibrate if supported
    if (navigator.vibrate) {
        navigator.vibrate(100);
    }

    // Analyze the URL
    if (isValidUrl(data)) {
        showToast('QR Code detected!', 'success');

        // Call Kotlin analysis
        setTimeout(() => {
            if (window.mehrguardAnalyze) {
                window.mehrguardAnalyze(data);
            }
        }, 500);
    } else {
        showToast('Not a valid URL', 'warning');
        hideScanningState();

        // Resume scanning
        setTimeout(() => {
            startScanning();
        }, 1500);
    }
}

// =============================================================================
// IMAGE UPLOAD
// =============================================================================

/**
 * Open gallery/file picker
 */
function openGallery() {
    elements.imageInput?.click();
}

/**
 * Handle uploaded image
 */
function handleImageUpload(event) {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
        showToast('Please select an image', 'error');
        return;
    }

    showToast('Processing image...', 'info');

    const reader = new FileReader();
    reader.onload = (e) => {
        const img = new Image();
        img.onload = () => {
            scanImageForQR(img);
        };
        img.onerror = () => {
            showToast('Failed to load image', 'error');
        };
        img.src = e.target.result;
    };
    reader.readAsDataURL(file);

    // Reset input for next upload
    event.target.value = '';
}

/**
 * Scan an image for QR codes
 */
function scanImageForQR(img) {
    const canvas = elements.scanCanvas;
    canvas.width = img.width;
    canvas.height = img.height;

    const ctx = canvas.getContext('2d');
    ctx.drawImage(img, 0, 0);

    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    const code = jsQR(imageData.data, imageData.width, imageData.height, {
        inversionAttempts: 'attemptBoth'
    });

    if (code) {
        console.log('[Scanner] QR found in image:', code.data);
        handleQRDetected(code.data);
    } else {
        showToast('No QR code found in image', 'warning');
    }
}

// =============================================================================
// DRAG AND DROP
// =============================================================================

function setupDragAndDrop() {
    const viewport = elements.scannerViewport;
    if (!viewport) return;

    viewport.addEventListener('dragover', (e) => {
        e.preventDefault();
        viewport.classList.add('drag-over');
    });

    viewport.addEventListener('dragleave', (e) => {
        e.preventDefault();
        viewport.classList.remove('drag-over');
    });

    viewport.addEventListener('drop', (e) => {
        e.preventDefault();
        viewport.classList.remove('drag-over');

        const file = e.dataTransfer.files?.[0];
        if (file && file.type.startsWith('image/')) {
            const event = { target: { files: [file] } };
            handleImageUpload(event);
        } else {
            showToast('Please drop an image file', 'error');
        }
    });
}

// =============================================================================
// URL INPUT MODAL
// =============================================================================

function openUrlModal() {
    elements.urlModal?.classList.remove('hidden');
    elements.urlInputField?.focus();
}

function closeUrlModal() {
    elements.urlModal?.classList.add('hidden');
    elements.urlInputField.value = '';
}

function analyzeUrlFromModal() {
    const url = elements.urlInputField?.value.trim();

    if (!url) {
        showToast('Please enter a URL', 'warning');
        return;
    }

    // Auto-fix URLs without protocol
    let fixedUrl = url;
    if (!url.includes('://')) {
        fixedUrl = 'https://' + url;
    }

    if (!isValidUrl(fixedUrl)) {
        showToast('Invalid URL format', 'error');
        return;
    }

    closeUrlModal();
    showScanningState();

    // Analyze
    setTimeout(() => {
        if (window.mehrguardAnalyze) {
            window.mehrguardAnalyze(fixedUrl);
        }
    }, 500);
}

/**
 * Analyze a URL directly (for Judge Demo Mode / Red Team scenarios)
 */
function analyzeUrlDirectly(url) {
    if (!url) return;
    
    console.log('[Mehr Guard Scanner] Analyzing demo URL:', url);
    showToast('🕵️ Analyzing Red Team scenario...', 'info');
    showScanningState();
    
    // Analyze after short delay
    setTimeout(() => {
        if (window.mehrguardAnalyze) {
            window.mehrguardAnalyze(url);
        } else {
            // Fallback: show URL in modal for manual analysis
            if (elements.urlInputField) {
                elements.urlInputField.value = url;
            }
            openUrlModal();
            showToast('Paste URL loaded. Click Analyze.', 'warning');
        }
    }, 500);
}

// =============================================================================
// HISTORY MANAGEMENT
// =============================================================================

function renderHistory() {
    const list = elements.scansList;
    if (!list) return;

    list.innerHTML = '';

    // Get history from shared UI
    let history = [];
    if (window.MehrGuardUI && window.MehrGuardUI.getScanHistory) {
        history = window.MehrGuardUI.getScanHistory();
    }

    if (history.length === 0) {
        list.innerHTML = `
            <div class="scan-item" style="justify-content: center; opacity: 0.5;">
                <span style="color: var(--text-muted); font-size: 0.875rem;">No recent scans</span>
            </div>
        `;
        window.mehrguardApplyTranslations?.(list);
        return;
    }

    // Show max 4 items in sidebar
    const displayItems = history.slice(0, 4);

    displayItems.forEach(item => {
        const div = document.createElement('div');
        div.className = 'scan-item';
        div.onclick = () => {
            // Navigate to results
            window.location.href = `results.html?scanId=${encodeURIComponent(item.id)}`;
        };

        const iconClass = item.verdict === 'SAFE' || item.verdict === 'LOW' ? 'safe' :
            item.verdict === 'SUSPICIOUS' || item.verdict === 'MEDIUM' ? 'warning' : 'danger';

        const iconName = item.verdict === 'SAFE' || item.verdict === 'LOW' ? 'check_circle' : 'warning';

        let verdictText = item.verdict;
        if (verdictText === 'HIGH') verdictText = translateText('Malicious');
        else if (verdictText === 'MEDIUM') verdictText = translateText('Suspicious');
        else if (verdictText === 'SAFE') verdictText = translateText('VerdictSafeLabel');
        else if (verdictText === 'LOW') verdictText = translateText('LowRisk');
        else verdictText = translateText(verdictText.charAt(0) + verdictText.slice(1).toLowerCase());

        const timeAgo = getTimeAgo(item.timestamp);

        div.innerHTML = `
            <div class="scan-icon ${iconClass}">
                <span class="material-symbols-outlined">${iconName}</span>
            </div>
            <div class="scan-info">
                <span class="scan-url">${escapeHtml(getDomainFromUrl(item.url))}</span>
                <span class="scan-meta">${timeAgo} • ${verdictText}</span>
            </div>
        `;

        list.appendChild(div);
    });

    window.mehrguardApplyTranslations?.(list);
}

// =============================================================================
// UI HELPERS
// =============================================================================

function updateLiveStatus(text, state) {
    const liveText = elements.liveStatus?.querySelector('.live-text');
    const liveDot = elements.liveStatus?.querySelector('.live-dot');

    if (liveText) {
        liveText.textContent = state === 'connected'
            ? translateText('LiveFeedActive')
            : translateText('LiveFeedDisconnected');
    }

    if (liveDot) {
        liveDot.style.backgroundColor = state === 'connected' ? 'var(--success)' : 'var(--danger)';
    }
}

function showScanningState() {
    elements.scanningState?.classList.remove('hidden');
    elements.emptyState?.classList.add('hidden');
}

function hideScanningState() {
    elements.scanningState?.classList.add('hidden');
}

function showToast(message, type = 'success') {
    if (!elements.toast || !elements.toastMessage) return;

    elements.toastMessage.textContent = translateText(message);

    const icon = elements.toast.querySelector('.toast-icon');
    if (icon) {
        icon.classList.remove('error');

        switch (type) {
            case 'success':
                icon.textContent = 'check_circle';
                icon.style.color = 'var(--success)';
                break;
            case 'warning':
                icon.textContent = 'warning';
                icon.style.color = 'var(--warning)';
                break;
            case 'error':
                icon.textContent = 'error';
                icon.style.color = 'var(--danger)';
                icon.classList.add('error');
                break;
            case 'info':
                icon.textContent = 'info';
                icon.style.color = 'var(--info)';
                break;
        }
    }

    elements.toast.classList.remove('hidden');
    elements.toast.classList.add('show');

    // Longer duration for info messages to ensure readability
    const duration = type === 'info' ? 5500 : 4000;
    setTimeout(() => {
        elements.toast.classList.remove('show');
        setTimeout(() => {
            elements.toast.classList.add('hidden');
        }, 300);
    }, duration);
}

// =============================================================================
// KEYBOARD SHORTCUTS
// =============================================================================

function setupKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Escape - close modal or stop camera
        if (e.key === 'Escape') {
            if (!elements.urlModal?.classList.contains('hidden')) {
                closeUrlModal();
            } else if (ScannerState.isCameraActive) {
                stopCamera();
            }
        }

        // V - paste URL (when not in input)
        if (e.key === 'v' && !e.ctrlKey && !e.metaKey &&
            document.activeElement?.tagName !== 'INPUT') {
            openUrlModal();
        }

        // G - open gallery
        if (e.key === 'g' && document.activeElement?.tagName !== 'INPUT') {
            openGallery();
        }

        // C - toggle camera
        if (e.key === 'c' && document.activeElement?.tagName !== 'INPUT') {
            if (ScannerState.isCameraActive) {
                stopCamera();
            } else {
                enableCamera();
            }
        }
    });
}

// =============================================================================
// UTILITY FUNCTIONS
// =============================================================================

function isValidUrl(string) {
    try {
        const url = new URL(string);
        return url.protocol === 'http:' || url.protocol === 'https:';
    } catch {
        return false;
    }
}

function getDomainFromUrl(url) {
    try {
        return new URL(url).hostname;
    } catch {
        return url;
    }
}

function getTimeAgo(timestamp) {
    const seconds = Math.floor((Date.now() - timestamp) / 1000);

    if (seconds < 60) return translateText('JustNow');
    if (seconds < 3600) {
        const minutes = Math.floor(seconds / 60);
        return minutes === 1
            ? formatText('{minutes} minute ago', { minutes })
            : formatText('{minutes} minutes ago', { minutes });
    }
    if (seconds < 86400) {
        const hours = Math.floor(seconds / 3600);
        return hours === 1
            ? formatText('{hours} hour ago', { hours })
            : formatText('{hours} hours ago', { hours });
    }
    const days = Math.floor(seconds / 86400);
    return days === 1
        ? formatText('{days} day ago', { days })
        : formatText('{days} days ago', { days });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// =============================================================================
// CLEANUP
// =============================================================================

window.addEventListener('beforeunload', () => {
    stopCamera();
});

// =============================================================================
// EXPORTS FOR TESTING
// =============================================================================

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        ScannerState,
        ScannerConfig,
        enableCamera,
        stopCamera,
        handleQRDetected,
    };
}
