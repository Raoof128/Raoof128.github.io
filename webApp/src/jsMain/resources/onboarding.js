/**
 * QR-SHIELD Onboarding Page Controller
 * 
 * Handles the offline privacy onboarding flow,
 * settings management, and navigation.
 * 
 * @author QR-SHIELD Team
 * @version 2.4.1
 */

// =============================================================================
// CONFIGURATION
// =============================================================================

const OnboardingConfig = {
    version: '2.4.1',
    settingsKey: 'qrshield_settings',
    onboardingCompleteKey: 'qrshield_onboarding_complete',
};

// =============================================================================
// STATE
// =============================================================================

const OnboardingState = {
    settings: {
        offlineMode: true,
        localSandbox: true,
        noCloudLogs: true,
        onDeviceDb: true,
    },
    isSidebarOpen: false,
};

// =============================================================================
// DOM ELEMENTS
// =============================================================================

const elements = {
    sidebar: null,
    menuToggle: null,
    enableOfflineBtn: null,
    readPolicyBtn: null,
    helpBtn: null,
    profileBtn: null,
    toast: null,
    toastMessage: null,
};

// =============================================================================
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('[QR-SHIELD Onboarding] Initializing v' + OnboardingConfig.version);

    // Cache DOM elements
    cacheElements();

    // Load existing settings
    loadSettings();

    // Setup event listeners
    setupEventListeners();

    // Setup settings controls
    setupSettingsListeners();

    // Add staggered animations
    animateFeatureCards();

    console.log('[QR-SHIELD Onboarding] Ready');
});

/**
 * Cache frequently accessed DOM elements
 */
function cacheElements() {
    elements.sidebar = document.getElementById('sidebar');
    elements.menuToggle = document.getElementById('menuToggle');
    elements.enableOfflineBtn = document.getElementById('enableOfflineBtn');
    elements.readPolicyBtn = document.getElementById('readPolicyBtn');
    elements.helpBtn = document.getElementById('helpBtn');
    elements.profileBtn = document.getElementById('profileBtn');
    elements.toast = document.getElementById('toast');
    elements.toastMessage = document.getElementById('toastMessage');
}

/**
 * Setup all event listeners
 */
function setupEventListeners() {
    // Mobile menu toggle
    elements.menuToggle?.addEventListener('click', toggleSidebar);

    // Primary action - Enable Offline Mode
    elements.enableOfflineBtn?.addEventListener('click', enableOfflineMode);

    // Secondary action - Read Policy
    elements.readPolicyBtn?.addEventListener('click', showPrivacyPolicy);

    // Header buttons
    elements.helpBtn?.addEventListener('click', showHelp);
    elements.profileBtn?.addEventListener('click', showProfile);

    // Feature card interactions
    setupFeatureCardInteractions();

    // Keyboard shortcuts
    setupKeyboardShortcuts();

    // Click outside sidebar to close (mobile)
    document.addEventListener('click', (e) => {
        if (OnboardingState.isSidebarOpen &&
            !elements.sidebar?.contains(e.target) &&
            !elements.menuToggle?.contains(e.target)) {
            closeSidebar();
        }
    });
}

// =============================================================================
// SETTINGS MANAGEMENT (Using QRShieldUI API)
// =============================================================================

/**
 * Load settings from localStorage and update UI
 */
function loadSettings() {
    // Wait for QRShieldUI to be available
    if (!window.QRShieldUI) {
        setTimeout(loadSettings, 100);
        return;
    }

    const settings = window.QRShieldUI.getSettings();
    OnboardingState.settings = settings;

    // Update sensitivity select
    const sensitivityEl = document.getElementById('settingSensitivity');
    if (sensitivityEl) {
        sensitivityEl.value = settings.sensitivity || 'balanced';
    }

    // Update toggle switches
    const toggleMappings = {
        'settingAutoBlock': 'autoBlock',
        'settingRealTime': 'realTimeScanning',
        'settingSoundEnabled': 'soundEnabled',
        'settingThreatAlerts': 'threatAlerts',
        'settingShowConfidence': 'showConfidence',
        'settingCompactView': 'compactView'
    };

    for (const [elementId, settingKey] of Object.entries(toggleMappings)) {
        const el = document.getElementById(elementId);
        if (el) {
            el.checked = settings[settingKey] !== false; // Default to true if not set
        }
    }
}

/**
 * Save settings to localStorage
 */
function saveSettings() {
    if (!window.QRShieldUI) return;

    window.QRShieldUI.saveSettings(OnboardingState.settings);
}

/**
 * Setup settings event listeners
 */
function setupSettingsListeners() {
    // Sensitivity select
    const sensitivityEl = document.getElementById('settingSensitivity');
    if (sensitivityEl) {
        sensitivityEl.addEventListener('change', (e) => {
            OnboardingState.settings.sensitivity = e.target.value;
            saveSettings();
            showToast(`Sensitivity set to ${e.target.value}`, 'info');
        });
    }

    // Toggle switches
    const toggleMappings = {
        'settingAutoBlock': { key: 'autoBlock', label: 'Auto-block' },
        'settingRealTime': { key: 'realTimeScanning', label: 'Real-time scanning' },
        'settingSoundEnabled': { key: 'soundEnabled', label: 'Sound alerts' },
        'settingThreatAlerts': { key: 'threatAlerts', label: 'Threat alerts' },
        'settingShowConfidence': { key: 'showConfidence', label: 'Confidence score' },
        'settingCompactView': { key: 'compactView', label: 'Compact view' }
    };

    for (const [elementId, { key, label }] of Object.entries(toggleMappings)) {
        const el = document.getElementById(elementId);
        if (el) {
            el.addEventListener('change', (e) => {
                OnboardingState.settings[key] = e.target.checked;
                saveSettings();
                showToast(`${label} ${e.target.checked ? 'enabled' : 'disabled'}`, 'success');
            });
        }
    }

    // Reset settings button
    const resetBtn = document.getElementById('resetSettingsBtn');
    if (resetBtn) {
        resetBtn.addEventListener('click', resetSettings);
    }
}

/**
 * Reset settings to defaults
 */
function resetSettings() {
    if (!window.QRShieldUI) return;

    // Get default settings
    const defaults = {
        sensitivity: 'balanced',
        autoBlock: true,
        realTimeScanning: true,
        offlineMode: true,
        localSandbox: true,
        noCloudLogs: true,
        onDeviceDb: true,
        soundEnabled: true,
        threatAlerts: true,
        scanSummary: true,
        darkMode: true,
        compactView: false,
        showConfidence: true
    };

    OnboardingState.settings = defaults;
    window.QRShieldUI.saveSettings(defaults);

    // Reload UI
    loadSettings();

    showToast('Settings reset to defaults', 'success');
}

// =============================================================================
// ACTIONS
// =============================================================================

/**
 * Enable offline mode and complete onboarding
 */
function enableOfflineMode() {
    // Ensure privacy settings are enabled
    OnboardingState.settings.offlineMode = true;
    OnboardingState.settings.localSandbox = true;
    OnboardingState.settings.noCloudLogs = true;
    OnboardingState.settings.onDeviceDb = true;

    // Save all settings
    saveSettings();

    // Mark onboarding complete
    try {
        localStorage.setItem(OnboardingConfig.onboardingCompleteKey, 'true');
    } catch (e) {
        console.error('[Onboarding] Failed to complete onboarding:', e);
    }

    // Show success animation
    showSuccessAnimation();

    // Show toast
    if (window.QRShieldUI) {
        window.QRShieldUI.showToast('Settings saved! Your data stays on-device.', 'success');
    } else {
        showToast('Settings saved! Your data stays on-device.', 'success');
    }

    // Navigate to dashboard after delay
    setTimeout(() => {
        window.location.href = 'dashboard.html';
    }, 1500);
}

/**
 * Show success animation on button
 */
function showSuccessAnimation() {
    const btn = elements.enableOfflineBtn;
    if (!btn) return;

    // Change button content
    btn.innerHTML = `
        <span class="material-symbols-outlined">check_circle</span>
        <span>Enabled!</span>
    `;

    // Add success style
    btn.style.backgroundColor = '#10b981';
    btn.style.boxShadow = '0 4px 20px rgba(16, 185, 129, 0.4)';
    btn.disabled = true;
}

/**
 * Show privacy policy
 */
function showPrivacyPolicy() {
    // In a real app, this would open a modal or navigate to policy page
    // For now, we'll show a toast with info
    showToast('Privacy policy: 100% offline, zero data collection', 'info');

    // Could also open GitHub privacy doc
    // window.open('https://github.com/Raoof128/Raoof128.github.io/blob/main/docs/PRIVACY.md', '_blank');
}

/**
 * Show help
 */
function showHelp() {
    showToast('Need help? Visit our GitHub for documentation', 'info');
}

/**
 * Show profile - opens the user profile dropdown or navigates to settings
 */
function showProfile() {
    // If QRShieldUI's profile dropdown is available, use it
    if (window.QRShieldUI && window.QRShieldUI.showProfileDropdown) {
        window.QRShieldUI.showProfileDropdown();
    } else {
        // Navigate to Trust Centre for settings management
        window.location.href = 'trust.html';
    }
}

// =============================================================================
// SIDEBAR CONTROLS
// =============================================================================

function toggleSidebar() {
    if (OnboardingState.isSidebarOpen) {
        closeSidebar();
    } else {
        openSidebar();
    }
}

function openSidebar() {
    elements.sidebar?.classList.add('open');
    OnboardingState.isSidebarOpen = true;
}

function closeSidebar() {
    elements.sidebar?.classList.remove('open');
    OnboardingState.isSidebarOpen = false;
}

// =============================================================================
// FEATURE CARD INTERACTIONS
// =============================================================================

function setupFeatureCardInteractions() {
    const cards = document.querySelectorAll('.feature-card');

    cards.forEach(card => {
        // Add click handler for feature details
        card.addEventListener('click', () => {
            const feature = card.dataset.feature;
            showFeatureDetails(feature);
        });

        // Add hover effect sounds (optional)
        card.addEventListener('mouseenter', () => {
            // Could add subtle hover sound
        });
    });
}

/**
 * Show feature details
 */
function showFeatureDetails(feature) {
    const details = {
        sandbox: {
            title: 'Local Sandbox',
            description: 'All code execution happens in an ephemeral container that is destroyed after each scan. This ensures malicious payloads can never escape to your system.',
        },
        cloud: {
            title: 'No Cloud Logs',
            description: 'QR-SHIELD strictly disables all outgoing telemetry. Your scan results, URL history, and image hashes never leave your device.',
        },
        database: {
            title: 'On-Device Database',
            description: 'The entire threat signature database (100,000+ patterns) is stored locally for millisecond lookups without any network requests.',
        },
    };

    const info = details[feature];
    if (info) {
        showToast(`${info.title}: ${info.description.substring(0, 50)}...`, 'info');
    }
}

// =============================================================================
// ANIMATIONS
// =============================================================================

/**
 * Animate feature cards with staggered entrance
 */
function animateFeatureCards() {
    const cards = document.querySelectorAll('.feature-card');

    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';

        setTimeout(() => {
            card.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 100 + (index * 100));
    });
}

// =============================================================================
// KEYBOARD SHORTCUTS
// =============================================================================

function setupKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Escape - go back / close sidebar
        if (e.key === 'Escape') {
            if (OnboardingState.isSidebarOpen) {
                closeSidebar();
            } else {
                window.location.href = 'dashboard.html';
            }
        }

        // Enter - enable offline mode
        if (e.key === 'Enter' && document.activeElement?.tagName !== 'BUTTON') {
            enableOfflineMode();
        }

        // P - read policy
        if (e.key === 'p' && document.activeElement?.tagName !== 'INPUT') {
            showPrivacyPolicy();
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
    }, 4000);
}

// =============================================================================
// ANALYTICS (Privacy-preserving)
// =============================================================================

/**
 * Track onboarding completion (local only)
 */
function trackOnboardingComplete() {
    try {
        const stats = JSON.parse(localStorage.getItem('qrshield_stats') || '{}');
        stats.onboardingCompletedAt = Date.now();
        stats.offlineModeEnabled = true;
        localStorage.setItem('qrshield_stats', JSON.stringify(stats));
    } catch (e) {
        // Silently fail - not critical
    }
}

// =============================================================================
// EXPORTS FOR TESTING
// =============================================================================

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        OnboardingState,
        OnboardingConfig,
        enableOfflineMode,
        loadSettings,
        saveSettings,
    };
}
