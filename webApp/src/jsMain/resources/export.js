/**
 * QR-SHIELD Report Export Page Controller
 * 
 * Handles format selection, report generation,
 * export actions, and live preview updates.
 * 
 * @author QR-SHIELD Team
 * @version 2.4.1
 */

// =============================================================================
// CONFIGURATION
// =============================================================================

const ExportConfig = {
    version: '2.4.1',
    defaultFormat: 'pdf',
    reportDataKey: 'qrshield_last_analysis',
};

// =============================================================================
// STATE
// =============================================================================

const ExportState = {
    selectedFormat: 'pdf',
    reportData: null,
    isSidebarOpen: false,
};

// =============================================================================
// DOM ELEMENTS
// =============================================================================

const elements = {
    // Sidebar
    sidebar: null,
    menuToggle: null,

    // Format selection
    formatRadios: null,

    // Actions
    exportBtn: null,
    copyBtn: null,
    shareBtn: null,

    // Preview
    previewFilename: null,
    reportDate: null,
    verdictCard: null,
    verdictValue: null,
    riskScore: null,
    targetUrl: null,
    analysisSummary: null,
    jsonPreview: null,

    // Toast
    toast: null,
    toastMessage: null,
};

// =============================================================================
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('[QR-SHIELD Export] Initializing v' + ExportConfig.version);

    // Cache DOM elements
    cacheElements();

    // Load last analysis data
    loadReportData();

    // Setup event listeners
    setupEventListeners();

    // Update preview with data
    updatePreview();

    console.log('[QR-SHIELD Export] Ready');
});

/**
 * Cache frequently accessed DOM elements
 */
function cacheElements() {
    elements.sidebar = document.getElementById('sidebar');
    elements.menuToggle = document.getElementById('menuToggle');
    elements.formatRadios = document.querySelectorAll('input[name="format"]');
    elements.exportBtn = document.getElementById('exportBtn');
    elements.copyBtn = document.getElementById('copyBtn');
    elements.shareBtn = document.getElementById('shareBtn');
    elements.previewFilename = document.getElementById('previewFilename');
    elements.reportDate = document.getElementById('reportDate');
    elements.verdictCard = document.getElementById('verdictCard');
    elements.verdictValue = document.getElementById('verdictValue');
    elements.riskScore = document.getElementById('riskScore');
    elements.targetUrl = document.getElementById('targetUrl');
    elements.analysisSummary = document.getElementById('analysisSummary');
    elements.jsonPreview = document.getElementById('jsonPreview');
    elements.toast = document.getElementById('toast');
    elements.toastMessage = document.getElementById('toastMessage');
}

/**
 * Setup all event listeners
 */
function setupEventListeners() {
    // Mobile menu toggle
    elements.menuToggle?.addEventListener('click', toggleSidebar);

    // Format selection
    elements.formatRadios?.forEach(radio => {
        radio.addEventListener('change', handleFormatChange);
    });

    // Export button
    elements.exportBtn?.addEventListener('click', exportReport);

    // Copy button
    elements.copyBtn?.addEventListener('click', copyReport);

    // Share button
    elements.shareBtn?.addEventListener('click', shareReport);

    // Keyboard shortcuts
    setupKeyboardShortcuts();

    // Click outside sidebar to close (mobile)
    document.addEventListener('click', (e) => {
        if (ExportState.isSidebarOpen &&
            !elements.sidebar?.contains(e.target) &&
            !elements.menuToggle?.contains(e.target)) {
            closeSidebar();
        }
    });
}

// =============================================================================
// DATA MANAGEMENT
// =============================================================================

/**
 * Load report data from localStorage or URL params
 */
function loadReportData() {
    // Try to get from URL params first
    const params = new URLSearchParams(window.location.search);
    const urlParam = params.get('url');
    const verdictParam = params.get('verdict');
    const scoreParam = params.get('score');

    if (urlParam) {
        ExportState.reportData = {
            url: decodeURIComponent(urlParam),
            verdict: verdictParam || 'SUSPICIOUS',
            score: parseInt(scoreParam) || 85,
            timestamp: Date.now(),
        };
    } else {
        // Try localStorage
        try {
            const stored = localStorage.getItem(ExportConfig.reportDataKey);
            if (stored) {
                ExportState.reportData = JSON.parse(stored);
            }
        } catch (e) {
            console.error('[Export] Failed to load report data:', e);
        }
    }

    // If still no data, use demo data
    if (!ExportState.reportData) {
        ExportState.reportData = getDemoData();
    }
}

/**
 * Get demo data for preview
 */
function getDemoData() {
    return {
        url: 'http://login-secure-banking-xyz.auth-gateway.net/ref=882',
        verdict: 'SUSPICIOUS',
        score: 85,
        timestamp: Date.now(),
        threatType: 'Phishing',
        sslIssuer: "Let's Encrypt (R3)",
        redirects: 2,
        heuristics: ['suspicious_tld', 'subdomain_chaining'],
        summary: 'The destination URL employs <span class="highlight">homoglyph obfuscation</span> characters often associated with phishing campaigns targeting financial institutions. The domain age is <span class="danger">&lt; 24 hours</span>.',
    };
}

// =============================================================================
// FORMAT HANDLING
// =============================================================================

/**
 * Handle format radio change
 */
function handleFormatChange(event) {
    ExportState.selectedFormat = event.target.value;
    updateFilename();
    console.log('[Export] Format changed to:', ExportState.selectedFormat);
}

/**
 * Update filename in preview based on selected format
 */
function updateFilename() {
    const scanId = Math.floor(Math.random() * 10000);
    const ext = ExportState.selectedFormat === 'pdf' ? 'pdf' : 'json';

    if (elements.previewFilename) {
        elements.previewFilename.textContent = `scan_result_id_${scanId}.${ext}`;
    }
}

// =============================================================================
// PREVIEW UPDATES
// =============================================================================

/**
 * Update the live preview with current data
 */
function updatePreview() {
    const data = ExportState.reportData;
    if (!data) return;

    // Update date
    if (elements.reportDate) {
        const date = new Date(data.timestamp);
        elements.reportDate.textContent = `Generated on ${formatDate(date)} • ${formatTime(date)} UTC`;
    }

    // Update verdict
    if (elements.verdictValue) {
        const verdictMap = {
            'SAFE': 'Safe',
            'SUSPICIOUS': 'Suspicious',
            'MALICIOUS': 'High Risk',
        };
        elements.verdictValue.textContent = verdictMap[data.verdict] || data.verdict;
    }

    // Update verdict card styling
    if (elements.verdictCard) {
        elements.verdictCard.classList.remove('safe', 'danger');
        if (data.verdict === 'SAFE') {
            elements.verdictCard.classList.add('safe');
        }
    }

    // Update risk score
    if (elements.riskScore) {
        elements.riskScore.textContent = data.score || 85;
    }

    // Update URL
    if (elements.targetUrl) {
        elements.targetUrl.textContent = data.url;
    }

    // Update summary
    if (elements.analysisSummary && data.summary) {
        elements.analysisSummary.innerHTML = data.summary;
    }

    // Update JSON preview
    updateJsonPreview(data);

    // Update filename
    updateFilename();
}

/**
 * Update JSON preview section
 */
function updateJsonPreview(data) {
    if (!elements.jsonPreview) return;

    const jsonData = {
        threat_type: data.threatType || 'Phishing',
        ssl_issuer: data.sslIssuer || "Let's Encrypt (R3)",
        redirects: data.redirects || 2,
        heuristics: data.heuristics || ['suspicious_tld', 'subdomain_chaining'],
    };

    elements.jsonPreview.innerHTML = formatJsonWithColors(jsonData);
}

/**
 * Format JSON with syntax highlighting
 */
function formatJsonWithColors(obj) {
    const json = JSON.stringify(obj, null, 2);

    return json
        .replace(/"([^"]+)":/g, '<span class="json-key">"$1"</span>:')
        .replace(/: "([^"]+)"/g, ': <span class="json-string">"$1"</span>')
        .replace(/: (\d+)/g, ': <span class="json-number">$1</span>');
}

// =============================================================================
// EXPORT ACTIONS
// =============================================================================

/**
 * Export the report in selected format
 */
function exportReport() {
    const format = ExportState.selectedFormat;
    const data = ExportState.reportData;

    showToast(`Generating ${format.toUpperCase()} report...`, 'info');

    // Simulate export delay
    setTimeout(() => {
        if (format === 'pdf') {
            exportAsPDF(data);
        } else {
            exportAsJSON(data);
        }
    }, 500);
}

/**
 * Export as PDF (generates HTML and triggers print)
 */
function exportAsPDF(data) {
    // Create a printable version
    const printWindow = window.open('', '_blank');

    const htmlContent = `
<!DOCTYPE html>
<html>
<head>
    <title>QR-SHIELD Threat Report</title>
    <style>
        body { font-family: 'Inter', sans-serif; padding: 40px; max-width: 800px; margin: 0 auto; }
        h1 { color: #111; margin-bottom: 10px; }
        .meta { color: #666; font-size: 14px; margin-bottom: 30px; }
        .verdict { padding: 20px; background: ${data.verdict === 'SAFE' ? '#d1fae5' : '#fee2e2'}; border-radius: 8px; margin-bottom: 20px; }
        .verdict h2 { color: ${data.verdict === 'SAFE' ? '#059669' : '#dc2626'}; margin: 0; }
        .section { margin-bottom: 20px; }
        .label { font-size: 12px; font-weight: 600; color: #666; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 8px; }
        .url { padding: 12px; background: #f3f4f6; border-radius: 4px; font-family: monospace; word-break: break-all; }
        .json { padding: 16px; background: #1f2937; color: #10b981; border-radius: 8px; font-family: monospace; font-size: 12px; white-space: pre; }
        .footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #e5e7eb; text-align: center; color: #9ca3af; font-size: 12px; }
    </style>
</head>
<body>
    <h1>QR-SHIELD Threat Analysis Report</h1>
    <p class="meta">Generated on ${new Date().toISOString()}</p>
    
    <div class="verdict">
        <h2>${data.verdict} - Risk Score: ${data.score}/100</h2>
    </div>
    
    <div class="section">
        <div class="label">Target URL</div>
        <div class="url">${escapeHtml(data.url)}</div>
    </div>
    
    <div class="section">
        <div class="label">Technical Indicators</div>
        <div class="json">${JSON.stringify({
        threat_type: data.threatType || 'Phishing',
        ssl_issuer: data.sslIssuer || "Let's Encrypt (R3)",
        redirects: data.redirects || 2,
        heuristics: data.heuristics || ['suspicious_tld', 'subdomain_chaining'],
    }, null, 2)}</div>
    </div>
    
    <div class="footer">
        Verified by QR-SHIELD Enterprise v${ExportConfig.version}
    </div>
</body>
</html>
    `;

    printWindow.document.write(htmlContent);
    printWindow.document.close();
    printWindow.focus();

    setTimeout(() => {
        printWindow.print();
        showToast('PDF report ready for download', 'success');
    }, 500);
}

/**
 * Export as JSON file
 */
function exportAsJSON(data) {
    const jsonData = {
        version: ExportConfig.version,
        generated_at: new Date().toISOString(),
        scan_result: {
            url: data.url,
            verdict: data.verdict,
            risk_score: data.score,
            threat_type: data.threatType || 'Phishing',
            ssl_issuer: data.sslIssuer || "Let's Encrypt (R3)",
            redirect_count: data.redirects || 2,
            heuristics: data.heuristics || ['suspicious_tld', 'subdomain_chaining'],
        },
        metadata: {
            engine_version: '2.4.1',
            analysis_mode: 'offline',
            platform: navigator.platform,
        },
    };

    const blob = new Blob([JSON.stringify(jsonData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = `qrshield_report_${Date.now()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    showToast('JSON report downloaded', 'success');
}

/**
 * Copy report to clipboard
 */
async function copyReport() {
    const data = ExportState.reportData;

    const reportText = `
QR-SHIELD Threat Analysis Report
================================
Generated: ${new Date().toISOString()}

URL: ${data.url}
Verdict: ${data.verdict}
Risk Score: ${data.score}/100

Technical Indicators:
- Threat Type: ${data.threatType || 'Phishing'}
- SSL Issuer: ${data.sslIssuer || "Let's Encrypt (R3)"}
- Redirects: ${data.redirects || 2}
- Heuristics: ${(data.heuristics || ['suspicious_tld', 'subdomain_chaining']).join(', ')}

Verified by QR-SHIELD Enterprise v${ExportConfig.version}
    `.trim();

    try {
        await navigator.clipboard.writeText(reportText);
        showToast('Report copied to clipboard', 'success');
    } catch (err) {
        console.error('[Export] Copy failed:', err);
        showToast('Failed to copy report', 'error');
    }
}

/**
 * Share report using Web Share API
 */
async function shareReport() {
    const data = ExportState.reportData;

    const shareData = {
        title: 'QR-SHIELD Threat Report',
        text: `URL: ${data.url}\nVerdict: ${data.verdict}\nRisk Score: ${data.score}/100`,
        url: window.location.href,
    };

    if (navigator.share) {
        try {
            await navigator.share(shareData);
            showToast('Report shared', 'success');
        } catch (err) {
            if (err.name !== 'AbortError') {
                console.error('[Export] Share failed:', err);
                showToast('Failed to share report', 'error');
            }
        }
    } else {
        // Fallback: copy URL
        try {
            await navigator.clipboard.writeText(window.location.href);
            showToast('Report URL copied to clipboard', 'success');
        } catch (err) {
            showToast('Sharing not supported', 'warning');
        }
    }
}

// =============================================================================
// SIDEBAR CONTROLS
// =============================================================================

function toggleSidebar() {
    if (ExportState.isSidebarOpen) {
        closeSidebar();
    } else {
        openSidebar();
    }
}

function openSidebar() {
    elements.sidebar?.classList.add('open');
    ExportState.isSidebarOpen = true;
}

function closeSidebar() {
    elements.sidebar?.classList.remove('open');
    ExportState.isSidebarOpen = false;
}

// =============================================================================
// KEYBOARD SHORTCUTS
// =============================================================================

function setupKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Escape - go back or close sidebar
        if (e.key === 'Escape') {
            if (ExportState.isSidebarOpen) {
                closeSidebar();
            } else {
                window.history.back();
            }
        }

        // E - export
        if (e.key === 'e' && !e.ctrlKey && !e.metaKey &&
            document.activeElement?.tagName !== 'INPUT') {
            exportReport();
        }

        // C - copy
        if (e.key === 'c' && !e.ctrlKey && !e.metaKey &&
            document.activeElement?.tagName !== 'INPUT') {
            copyReport();
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

function formatDate(date) {
    const options = { month: 'short', day: 'numeric', year: 'numeric' };
    return date.toLocaleDateString('en-US', options);
}

function formatTime(date) {
    const options = { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false };
    return date.toLocaleTimeString('en-US', options);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// =============================================================================
// EXPORTS FOR TESTING
// =============================================================================

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        ExportState,
        ExportConfig,
        exportReport,
        copyReport,
        shareReport,
    };
}
