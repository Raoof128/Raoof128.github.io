/**
 * QR-SHIELD Trust Centre Page Controller
 * 
 * Handles sensitivity settings, allowlist/blocklist management,
 * privacy toggles, and settings persistence.
 * 
 * @author QR-SHIELD Team
 * @version 2.4.1
 */

// =============================================================================
// CONFIGURATION
// =============================================================================

const TrustConfig = {
    version: '2.4.1',
    settingsKey: 'qrshield_trust_settings',
    allowlistKey: 'qrshield_allowlist',
    blocklistKey: 'qrshield_blocklist',
};

// =============================================================================
// STATE
// =============================================================================

const TrustState = {
    sensitivity: 2, // 1=Low, 2=Balanced, 3=Paranoia
    settings: {
        strictOffline: true,
        anonymousTelemetry: false,
        autoCopySafeLinks: true,
    },
    allowlist: ['internal-corp.net', 'localhost'],
    blocklist: [],
    modalTarget: null, // 'allowlist' or 'blocklist'
    isSidebarOpen: false,
};

// =============================================================================
// DOM ELEMENTS
// =============================================================================

const elements = {
    // Sidebar
    sidebar: null,
    menuToggle: null,

    // Sensitivity
    sensitivitySlider: null,
    sensitivityBadge: null,
    sensitivityInfo: null,
    sliderLabels: null,

    // Lists
    allowlistContent: null,
    blocklistContent: null,
    addAllowlistBtn: null,
    addBlocklistBtn: null,

    // Toggles
    offlineToggle: null,
    telemetryToggle: null,
    autoCopyToggle: null,

    // Modal
    addDomainModal: null,
    modalTitle: null,
    domainInput: null,
    addDomainBtn: null,
    closeModal: null,

    // Other
    auditBtn: null,
    resetBtn: null,
    toast: null,
    toastMessage: null,
};

// =============================================================================
// SENSITIVITY LEVELS
// =============================================================================

const SensitivityLevels = {
    1: {
        name: 'Low',
        title: 'Low Sensitivity Mode',
        description: 'Only blocks known malicious URLs from the threat database. Minimal false positives, but may miss novel attacks.',
    },
    2: {
        name: 'Balanced',
        title: 'Balanced Mode (Recommended)',
        description: 'Scans for known malicious patterns and heuristic mismatches. Blocks homoglyph attacks and redirect chains. Low false positive rate expected.',
    },
    3: {
        name: 'Paranoia',
        title: 'Paranoia Mode',
        description: 'Maximum protection. Blocks any suspicious patterns including newly registered domains, uncommon TLDs, and any URL with potential encoding tricks.',
    },
};

// =============================================================================
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('[QR-SHIELD Trust Centre] Initializing v' + TrustConfig.version);

    // Cache DOM elements
    cacheElements();

    // Load saved settings
    loadSettings();

    // Setup event listeners
    setupEventListeners();

    // Render initial UI
    renderUI();

    console.log('[QR-SHIELD Trust Centre] Ready');
});

/**
 * Cache frequently accessed DOM elements
 */
function cacheElements() {
    elements.sidebar = document.getElementById('sidebar');
    elements.menuToggle = document.getElementById('menuToggle');
    elements.sensitivitySlider = document.getElementById('sensitivitySlider');
    elements.sensitivityBadge = document.getElementById('sensitivityBadge');
    elements.sensitivityInfo = document.getElementById('sensitivityInfo');
    elements.sliderLabels = document.querySelectorAll('.slider-labels span');
    elements.allowlistContent = document.getElementById('allowlistContent');
    elements.blocklistContent = document.getElementById('blocklistContent');
    elements.addAllowlistBtn = document.getElementById('addAllowlistBtn');
    elements.addBlocklistBtn = document.getElementById('addBlocklistBtn');
    elements.offlineToggle = document.getElementById('offlineToggle');
    elements.telemetryToggle = document.getElementById('telemetryToggle');
    elements.autoCopyToggle = document.getElementById('autoCopyToggle');
    elements.addDomainModal = document.getElementById('addDomainModal');
    elements.modalTitle = document.getElementById('modalTitle');
    elements.domainInput = document.getElementById('domainInput');
    elements.addDomainBtn = document.getElementById('addDomainBtn');
    elements.closeModal = document.getElementById('closeModal');
    elements.auditBtn = document.getElementById('auditBtn');
    elements.resetBtn = document.getElementById('resetBtn');
    elements.toast = document.getElementById('toast');
    elements.toastMessage = document.getElementById('toastMessage');
}

/**
 * Setup all event listeners
 */
function setupEventListeners() {
    // Mobile menu toggle
    elements.menuToggle?.addEventListener('click', toggleSidebar);

    // Sensitivity slider
    elements.sensitivitySlider?.addEventListener('input', handleSensitivityChange);

    // Add buttons
    elements.addAllowlistBtn?.addEventListener('click', () => openModal('allowlist'));
    elements.addBlocklistBtn?.addEventListener('click', () => openModal('blocklist'));

    // Modal
    elements.closeModal?.addEventListener('click', closeModal);
    elements.addDomainBtn?.addEventListener('click', addDomain);
    elements.addDomainModal?.addEventListener('click', (e) => {
        if (e.target === elements.addDomainModal) closeModal();
    });
    elements.domainInput?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') addDomain();
    });

    // Toggles
    elements.offlineToggle?.addEventListener('change', (e) => {
        TrustState.settings.strictOffline = e.target.checked;
        updateToggleStyle(e.target);
        saveSettings();
        showToast(e.target.checked ? 'Strict Offline Mode enabled' : 'Strict Offline Mode disabled', 'success');
    });

    elements.telemetryToggle?.addEventListener('change', (e) => {
        TrustState.settings.anonymousTelemetry = e.target.checked;
        updateToggleStyle(e.target);
        saveSettings();
        showToast(e.target.checked ? 'Anonymous Telemetry enabled' : 'Anonymous Telemetry disabled', 'info');
    });

    elements.autoCopyToggle?.addEventListener('change', (e) => {
        TrustState.settings.autoCopySafeLinks = e.target.checked;
        updateToggleStyle(e.target);
        saveSettings();
        showToast(e.target.checked ? 'Auto-Copy Safe Links enabled' : 'Auto-Copy Safe Links disabled', 'success');
    });

    // Audit button
    elements.auditBtn?.addEventListener('click', () => {
        showToast('Security audit report coming soon', 'info');
    });

    // Reset button
    elements.resetBtn?.addEventListener('click', resetSettings);

    // Keyboard shortcuts
    setupKeyboardShortcuts();

    // Blocklist empty state
    document.querySelector('.add-manual-btn')?.addEventListener('click', () => openModal('blocklist'));
}

// =============================================================================
// SETTINGS MANAGEMENT
// =============================================================================

/**
 * Load settings from localStorage
 */
function loadSettings() {
    try {
        // Load trust settings
        const stored = localStorage.getItem(TrustConfig.settingsKey);
        if (stored) {
            const parsed = JSON.parse(stored);
            TrustState.sensitivity = parsed.sensitivity || 2;
            TrustState.settings = { ...TrustState.settings, ...parsed.settings };
        }

        // Load allowlist
        const allowlist = localStorage.getItem(TrustConfig.allowlistKey);
        if (allowlist) {
            TrustState.allowlist = JSON.parse(allowlist);
        }

        // Load blocklist
        const blocklist = localStorage.getItem(TrustConfig.blocklistKey);
        if (blocklist) {
            TrustState.blocklist = JSON.parse(blocklist);
        }
    } catch (e) {
        console.error('[Trust Centre] Failed to load settings:', e);
    }
}

/**
 * Save settings to localStorage
 */
function saveSettings() {
    try {
        localStorage.setItem(TrustConfig.settingsKey, JSON.stringify({
            sensitivity: TrustState.sensitivity,
            settings: TrustState.settings,
        }));
        localStorage.setItem(TrustConfig.allowlistKey, JSON.stringify(TrustState.allowlist));
        localStorage.setItem(TrustConfig.blocklistKey, JSON.stringify(TrustState.blocklist));
    } catch (e) {
        console.error('[Trust Centre] Failed to save settings:', e);
    }
}

/**
 * Reset all settings to default
 */
function resetSettings() {
    if (!confirm('Reset all Trust Centre settings to default?')) {
        return;
    }

    TrustState.sensitivity = 2;
    TrustState.settings = {
        strictOffline: true,
        anonymousTelemetry: false,
        autoCopySafeLinks: true,
    };
    TrustState.allowlist = ['internal-corp.net', 'localhost'];
    TrustState.blocklist = [];

    saveSettings();
    renderUI();
    showToast('Settings reset to default', 'success');
}

// =============================================================================
// UI RENDERING
// =============================================================================

/**
 * Render all UI components
 */
function renderUI() {
    renderSensitivity();
    renderLists();
    renderToggles();
}

/**
 * Render sensitivity slider and info
 */
function renderSensitivity() {
    const level = TrustState.sensitivity;
    const info = SensitivityLevels[level];

    // Update slider
    if (elements.sensitivitySlider) {
        elements.sensitivitySlider.value = level;
    }

    // Update badge
    if (elements.sensitivityBadge) {
        elements.sensitivityBadge.textContent = info.name;
    }

    // Update labels
    if (elements.sliderLabels) {
        elements.sliderLabels.forEach((label, index) => {
            label.classList.toggle('active', index + 1 === level);
        });
    }

    // Update info box
    if (elements.sensitivityInfo) {
        elements.sensitivityInfo.innerHTML = `
            <span class="material-symbols-outlined">info</span>
            <div class="info-content">
                <p class="info-title">${info.title}</p>
                <p class="info-description">${info.description}</p>
            </div>
        `;
    }
}

/**
 * Render allowlist and blocklist
 */
function renderLists() {
    renderAllowlist();
    renderBlocklist();
}

/**
 * Render allowlist
 */
function renderAllowlist() {
    if (!elements.allowlistContent) return;

    if (TrustState.allowlist.length === 0) {
        elements.allowlistContent.innerHTML = `
            <div class="empty-state">
                <span class="material-symbols-outlined">verified_user</span>
                <p>No domains in allowlist.</p>
                <button class="add-manual-btn" onclick="openModal('allowlist')">Add domain</button>
            </div>
        `;
        elements.allowlistContent.classList.add('empty');
    } else {
        elements.allowlistContent.classList.remove('empty');
        elements.allowlistContent.innerHTML = TrustState.allowlist.map((domain, index) => `
            <div class="list-item">
                <div class="item-info">
                    <span class="item-domain">${escapeHtml(domain)}</span>
                    <span class="item-date">Added ${getRandomDate()}</span>
                </div>
                <button class="delete-btn" onclick="removeDomain('allowlist', ${index})">
                    <span class="material-symbols-outlined">delete</span>
                </button>
            </div>
        `).join('');
    }
}

/**
 * Render blocklist
 */
function renderBlocklist() {
    if (!elements.blocklistContent) return;

    if (TrustState.blocklist.length === 0) {
        elements.blocklistContent.innerHTML = `
            <div class="empty-state">
                <span class="material-symbols-outlined">playlist_remove</span>
                <p>No custom domains blocked.</p>
                <button class="add-manual-btn" onclick="openModal('blocklist')">Add manually</button>
            </div>
        `;
        elements.blocklistContent.classList.add('empty');
    } else {
        elements.blocklistContent.classList.remove('empty');
        elements.blocklistContent.innerHTML = TrustState.blocklist.map((domain, index) => `
            <div class="list-item">
                <div class="item-info">
                    <span class="item-domain">${escapeHtml(domain)}</span>
                    <span class="item-date">Added ${getRandomDate()}</span>
                </div>
                <button class="delete-btn" onclick="removeDomain('blocklist', ${index})">
                    <span class="material-symbols-outlined">delete</span>
                </button>
            </div>
        `).join('');
    }
}

/**
 * Render toggle states
 */
function renderToggles() {
    if (elements.offlineToggle) {
        elements.offlineToggle.checked = TrustState.settings.strictOffline;
        updateToggleStyle(elements.offlineToggle);
    }

    if (elements.telemetryToggle) {
        elements.telemetryToggle.checked = TrustState.settings.anonymousTelemetry;
        updateToggleStyle(elements.telemetryToggle);
    }

    if (elements.autoCopyToggle) {
        elements.autoCopyToggle.checked = TrustState.settings.autoCopySafeLinks;
        updateToggleStyle(elements.autoCopyToggle);
    }
}

/**
 * Update toggle switch visual state
 */
function updateToggleStyle(toggle) {
    const label = toggle.closest('.toggle-switch');
    if (label) {
        label.classList.toggle('on', toggle.checked);
    }
}

// =============================================================================
// SENSITIVITY HANDLING
// =============================================================================

/**
 * Handle sensitivity slider change
 */
function handleSensitivityChange(event) {
    TrustState.sensitivity = parseInt(event.target.value);
    renderSensitivity();
    saveSettings();

    const level = SensitivityLevels[TrustState.sensitivity];
    showToast(`Sensitivity set to ${level.name}`, 'success');
}

// =============================================================================
// LIST MANAGEMENT
// =============================================================================

/**
 * Open add domain modal
 */
function openModal(target) {
    TrustState.modalTarget = target;

    if (elements.modalTitle) {
        elements.modalTitle.textContent = target === 'allowlist' ? 'Add to Allowlist' : 'Add to Blocklist';
    }

    if (elements.domainInput) {
        elements.domainInput.value = '';
        elements.domainInput.focus();
    }

    elements.addDomainModal?.classList.remove('hidden');
}

// Expose to global scope for onclick handlers
window.openModal = openModal;

/**
 * Close add domain modal
 */
function closeModal() {
    elements.addDomainModal?.classList.add('hidden');
    TrustState.modalTarget = null;
}

/**
 * Add domain to list
 */
function addDomain() {
    const domain = elements.domainInput?.value.trim().toLowerCase();

    if (!domain) {
        showToast('Please enter a domain', 'warning');
        return;
    }

    // Basic validation
    if (!isValidDomain(domain)) {
        showToast('Please enter a valid domain', 'warning');
        return;
    }

    const list = TrustState.modalTarget === 'allowlist' ? TrustState.allowlist : TrustState.blocklist;

    if (list.includes(domain)) {
        showToast('Domain already in list', 'warning');
        return;
    }

    list.push(domain);
    saveSettings();
    renderLists();
    closeModal();

    showToast(`Added ${domain} to ${TrustState.modalTarget}`, 'success');
}

/**
 * Remove domain from list
 */
function removeDomain(listType, index) {
    const list = listType === 'allowlist' ? TrustState.allowlist : TrustState.blocklist;
    const domain = list[index];

    list.splice(index, 1);
    saveSettings();
    renderLists();

    showToast(`Removed ${domain} from ${listType}`, 'success');
}

// Expose to global scope for onclick handlers
window.removeDomain = removeDomain;

// =============================================================================
// SIDEBAR CONTROLS
// =============================================================================

function toggleSidebar() {
    if (TrustState.isSidebarOpen) {
        closeSidebar();
    } else {
        openSidebar();
    }
}

function openSidebar() {
    elements.sidebar?.classList.add('open');
    TrustState.isSidebarOpen = true;
}

function closeSidebar() {
    elements.sidebar?.classList.remove('open');
    TrustState.isSidebarOpen = false;
}

// =============================================================================
// KEYBOARD SHORTCUTS
// =============================================================================

function setupKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Escape - close modal or go back
        if (e.key === 'Escape') {
            if (!elements.addDomainModal?.classList.contains('hidden')) {
                closeModal();
            } else if (TrustState.isSidebarOpen) {
                closeSidebar();
            } else {
                window.history.back();
            }
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

    // Increased duration for better readability (5.5s for help messages)
    const duration = type === 'info' ? 5500 : 4000;
    setTimeout(() => {
        elements.toast.classList.remove('show');
        setTimeout(() => {
            elements.toast.classList.add('hidden');
        }, 300);
    }, duration);
}

// =============================================================================
// UTILITY FUNCTIONS
// =============================================================================

function isValidDomain(domain) {
    // Basic domain validation
    const pattern = /^[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,}$/i;
    return pattern.test(domain) || domain === 'localhost';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function getRandomDate() {
    const options = ['2 days ago', '1 week ago', '3 weeks ago', '1 month ago'];
    return options[Math.floor(Math.random() * options.length)];
}

// =============================================================================
// EXPORTS FOR TESTING
// =============================================================================

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        TrustState,
        TrustConfig,
        SensitivityLevels,
        loadSettings,
        saveSettings,
        resetSettings,
    };
}
