/**
 * Mehr Guard Shared UI Controller
 * Handles common interactive elements across all pages:
 * - User Profile dropdown and settings
 * - Notification system
 * - Quick actions
 * - App statistics
 */

(function () {
    'use strict';

    const NORMALIZE_RE = /\s+/g;

    function normalizeKey(text) {
        return text ? text.replace(NORMALIZE_RE, ' ').trim() : '';
    }

    function translateText(text) {
        const normalized = normalizeKey(text);
        if (!normalized) return text;
        if (window.mehrguardGetTranslation) {
            return window.mehrguardGetTranslation(normalized);
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

    window.mehrguardTranslateText = translateText;
    window.mehrguardFormatText = formatText;

    // ==========================================================================
    // USER PROFILE MANAGEMENT
    // ==========================================================================

    const DEFAULT_USER = {
        name: 'John Smith',
        initials: 'JS',
        email: 'john.smith@example.com',
        plan: 'Enterprise Plan',
        role: 'Security Analyst',
        scansToday: 0,
        totalScans: 0,
        threatsBlocked: 0
    };

    /**
     * Get current user from localStorage
     */
    function getUser() {
        const stored = localStorage.getItem('mehrguard_user');
        if (stored) {
            try {
                return { ...DEFAULT_USER, ...JSON.parse(stored) };
            } catch (e) {
                return DEFAULT_USER;
            }
        }
        return DEFAULT_USER;
    }

    /**
     * Save user to localStorage
     */
    function saveUser(user) {
        localStorage.setItem('mehrguard_user', JSON.stringify(user));
        updateAllUserUI();
    }

    /**
     * Update all user-related UI elements on the page
     */
    function updateAllUserUI() {
        const user = getUser();

        // Update user avatars
        document.querySelectorAll('.user-avatar').forEach(el => {
            el.textContent = user.initials;
        });

        // Update user names
        document.querySelectorAll('.user-name').forEach(el => {
            el.textContent = user.name;
        });

        // Update user plans/roles
        document.querySelectorAll('.user-plan').forEach(el => {
            el.textContent = user.plan;
        });

        document.querySelectorAll('.user-role').forEach(el => {
            el.textContent = user.role;
        });
    }

    /**
     * Check if profile dropdown is currently visible
     */
    function isProfileDropdownOpen() {
        const existing = document.getElementById('profileDropdown');
        return existing && existing.classList.contains('visible');
    }

    /**
     * Toggle the profile dropdown - close if open, open if closed
     */
    function toggleProfileDropdown(anchorElement) {
        if (isProfileDropdownOpen()) {
            hideProfileDropdown();
        } else {
            showProfileDropdown(anchorElement);
        }
    }

    /**
     * Create and show the profile dropdown
     */
    function showProfileDropdown(anchorElement) {
        // Remove existing dropdown
        hideProfileDropdown();

        // Guard: If no anchor element, find one from the page
        if (!anchorElement) {
            anchorElement = document.getElementById('profileBtn') ||
                document.querySelector('.user-profile') ||
                document.querySelector('.user-avatar');
            // If still no anchor, position in top-right corner
            if (!anchorElement) {
                console.warn('[SharedUI] No anchor element for profile dropdown, using fallback position');
            }
        }

        const user = getUser();
        const stats = getAppStats();

        const dropdown = document.createElement('div');
        dropdown.className = 'profile-dropdown';
        dropdown.id = 'profileDropdown';
        dropdown.innerHTML = `
            <div class="profile-dropdown-header">
                <div class="profile-avatar-lg">${user.initials}</div>
                <div class="profile-info">
                    <h4>${user.name}</h4>
                    <p>${user.email}</p>
                    <span class="profile-badge">${user.plan}</span>
                </div>
            </div>
            <div class="profile-stats">
                <div class="stat-item">
                    <span class="stat-value">${stats.scansToday}</span>
                    <span class="stat-label">Today</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">${stats.totalScans}</span>
                    <span class="stat-label">Total Scans</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">${stats.threatsBlocked}</span>
                    <span class="stat-label">Blocked</span>
                </div>
            </div>
            <div class="profile-dropdown-actions">
                <button class="profile-action" id="editProfileBtn">
                    <span class="material-symbols-outlined">edit</span>
                    Edit Profile
                </button>
                <button class="profile-action" id="exportDataBtn">
                    <span class="material-symbols-outlined">download</span>
                    Export Data
                </button>
                <button class="profile-action" id="viewSettingsBtn">
                    <span class="material-symbols-outlined">settings</span>
                    Settings
                </button>
            </div>
            <div class="profile-dropdown-footer">
                <span class="offline-indicator">
                    <span class="offline-dot"></span>
                    Offline Ready
                </span>
                <span class="version-text">v2.4.1</span>
            </div>
        `;


        document.body.appendChild(dropdown);
        window.mehrguardApplyTranslations?.(dropdown);
        window.mehrguardApplyTranslations?.(dropdown);

        // Smart positioning - prevent dropdown from going off-screen
        const dropdownWidth = 280;
        const dropdownHeight = 320;

        // Handle case where anchorElement is still null
        if (!anchorElement) {
            // Fallback: position in top-right corner
            dropdown.style.top = '80px';
            dropdown.style.right = '16px';
            dropdown.style.left = 'auto';
            dropdown.style.bottom = 'auto';
        } else {
            const rect = anchorElement.getBoundingClientRect();
            const spaceBelow = window.innerHeight - rect.bottom;
            const spaceAbove = rect.top;
            const spaceRight = window.innerWidth - rect.left;

            // Vertical positioning
            if (spaceBelow < dropdownHeight && spaceAbove > dropdownHeight) {
                // Open upward
                dropdown.style.bottom = `${window.innerHeight - rect.top + 8}px`;
                dropdown.style.top = 'auto';
            } else {
                // Open downward (default)
                dropdown.style.top = `${rect.bottom + 8}px`;
                dropdown.style.bottom = 'auto';
            }

            // Horizontal positioning - ensure it stays on screen
            if (spaceRight < dropdownWidth) {
                // Not enough space on right, align to right edge
                dropdown.style.right = '16px';
                dropdown.style.left = 'auto';
            } else if (rect.left < 16) {
                // Too close to left edge
                dropdown.style.left = '16px';
                dropdown.style.right = 'auto';
            } else {
                dropdown.style.left = `${rect.left}px`;
                dropdown.style.right = 'auto';
            }
        }

        // Animate in (always runs)
        requestAnimationFrame(() => {
            dropdown.classList.add('visible');
        });

        // Add event listeners (always runs)
        dropdown.querySelector('#editProfileBtn').addEventListener('click', () => {
            hideProfileDropdown();
            showEditProfileModal();
        });

        dropdown.querySelector('#exportDataBtn').addEventListener('click', () => {
            hideProfileDropdown();
            exportUserData();
        });

        dropdown.querySelector('#viewSettingsBtn').addEventListener('click', () => {
            hideProfileDropdown();
            window.location.href = 'onboarding.html';
        });

        // Close on outside click - faster
        setTimeout(() => {
            document.addEventListener('click', handleOutsideClick);
        }, 50);
    }

    function hideProfileDropdown() {
        const existing = document.getElementById('profileDropdown');
        if (existing) {
            existing.classList.remove('visible');
            setTimeout(() => existing.remove(), 150);
        }
        document.removeEventListener('click', handleOutsideClick);
    }

    function handleOutsideClick(e) {
        const dropdown = document.getElementById('profileDropdown');
        if (dropdown && !dropdown.contains(e.target) && !e.target.closest('.user-profile') && !e.target.closest('.user-avatar')) {
            hideProfileDropdown();
        }
    }

    /**
     * Show the edit profile modal
     */
    function showEditProfileModal() {
        const user = getUser();

        const modal = document.createElement('div');
        modal.className = 'shared-modal-overlay';
        modal.id = 'editProfileModal';
        modal.innerHTML = `
            <div class="shared-modal">
                <div class="shared-modal-header">
                    <h3>Edit Profile</h3>
                    <button class="modal-close-btn" id="closeProfileModal">
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>
                <div class="shared-modal-content">
                    <div class="form-group">
                        <label for="profileName">Name</label>
                        <input type="text" id="profileName" value="${user.name}" placeholder="Your name">
                    </div>
                    <div class="form-group">
                        <label for="profileEmail">Email</label>
                        <input type="email" id="profileEmail" value="${user.email}" placeholder="your.email@example.com">
                    </div>
                    <div class="form-group">
                        <label for="profileRole">Role</label>
                        <input type="text" id="profileRole" value="${user.role}" placeholder="Your role">
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="profileInitials">Initials</label>
                            <input type="text" id="profileInitials" value="${user.initials}" maxlength="2" placeholder="AB">
                        </div>
                        <div class="form-group">
                            <label for="profilePlan">Plan</label>
                            <select id="profilePlan">
                                <option value="Free Plan" ${user.plan === 'Free Plan' ? 'selected' : ''}>Free Plan</option>
                                <option value="Pro Plan" ${user.plan === 'Pro Plan' ? 'selected' : ''}>Pro Plan</option>
                                <option value="Enterprise Plan" ${user.plan === 'Enterprise Plan' ? 'selected' : ''}>Enterprise Plan</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="shared-modal-footer">
                    <button class="btn-cancel" id="cancelProfileBtn">Cancel</button>
                    <button class="btn-save" id="saveProfileBtn">Save Changes</button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);
        window.mehrguardApplyTranslations?.(modal);

        // Animate in
        requestAnimationFrame(() => {
            modal.classList.add('visible');
        });

        // Event listeners
        modal.querySelector('#closeProfileModal').addEventListener('click', () => closeModal(modal));
        modal.querySelector('#cancelProfileBtn').addEventListener('click', () => closeModal(modal));
        modal.querySelector('#saveProfileBtn').addEventListener('click', () => {
            const updatedUser = {
                ...user,
                name: document.getElementById('profileName').value,
                email: document.getElementById('profileEmail').value,
                role: document.getElementById('profileRole').value,
                initials: document.getElementById('profileInitials').value.toUpperCase() || user.name.split(' ').map(n => n[0]).join('').toUpperCase(),
                plan: document.getElementById('profilePlan').value
            };
            saveUser(updatedUser);
            closeModal(modal);
            showToast('Profile updated successfully!', 'success');
        });

        // Close on overlay click
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModal(modal);
        });

        // Auto-generate initials
        modal.querySelector('#profileName').addEventListener('input', (e) => {
            const name = e.target.value;
            const initials = name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
            document.getElementById('profileInitials').value = initials;
        });
    }

    function closeModal(modal) {
        modal.classList.remove('visible');
        setTimeout(() => modal.remove(), 150);
    }

    // ==========================================================================
    // NOTIFICATION SYSTEM
    // ==========================================================================

    const NOTIFICATIONS_KEY = 'mehrguard_notifications';

    function getNotifications() {
        const stored = localStorage.getItem(NOTIFICATIONS_KEY);
        if (stored) {
            try {
                return JSON.parse(stored);
            } catch (e) {
                return getDefaultNotifications();
            }
        }
        return getDefaultNotifications();
    }

    function getDefaultNotifications() {
        return [
            {
                id: 1,
                type: 'success',
                icon: 'security',
                title: 'Security Engine Active',
                message: 'Phishing detection is running normally',
                time: 'Just now',
                read: false
            },
            {
                id: 2,
                type: 'info',
                icon: 'update',
                title: 'Database Updated',
                message: 'Threat signatures updated to v2.4.1',
                time: '2 hours ago',
                read: false
            },
            {
                id: 3,
                type: 'warning',
                icon: 'warning',
                title: 'High-Risk URL Blocked',
                message: 'A phishing attempt was successfully blocked',
                time: 'Yesterday',
                read: true
            }
        ];
    }

    function saveNotifications(notifications) {
        localStorage.setItem(NOTIFICATIONS_KEY, JSON.stringify(notifications));
        updateNotificationBadge();
    }

    function addNotification(notification) {
        const notifications = getNotifications();
        notifications.unshift({
            id: Date.now(),
            read: false,
            time: 'Just now',
            ...notification
        });
        saveNotifications(notifications.slice(0, 20)); // Keep last 20
    }

    function markNotificationRead(id) {
        const notifications = getNotifications();
        const notif = notifications.find(n => n.id === id);
        if (notif) {
            notif.read = true;
            saveNotifications(notifications);
        }
    }

    function markAllNotificationsRead() {
        const notifications = getNotifications();
        notifications.forEach(n => n.read = true);
        saveNotifications(notifications);
    }

    function updateNotificationBadge() {
        const notifications = getNotifications();
        const unreadCount = notifications.filter(n => !n.read).length;

        document.querySelectorAll('.notification-dot').forEach(dot => {
            dot.style.display = unreadCount > 0 ? 'block' : 'none';
        });
    }

    /**
     * Check if notification dropdown is currently visible
     */
    function isNotificationDropdownOpen() {
        const existing = document.getElementById('notificationDropdown');
        return existing && existing.classList.contains('visible');
    }

    /**
     * Toggle the notification dropdown - close if open, open if closed
     */
    function toggleNotificationDropdown(anchorElement) {
        if (isNotificationDropdownOpen()) {
            hideNotificationDropdown();
        } else {
            showNotificationDropdown(anchorElement);
        }
    }

    function showNotificationDropdown(anchorElement) {
        hideNotificationDropdown();

        const notifications = getNotifications();

        const dropdown = document.createElement('div');
        dropdown.className = 'notification-dropdown';
        dropdown.id = 'notificationDropdown';

        const notifItems = notifications.slice(0, 5).map(n => `
            <div class="notification-item ${n.read ? 'read' : 'unread'}" data-id="${n.id}">
                <div class="notification-icon ${n.type}">
                    <span class="material-symbols-outlined">${n.icon}</span>
                </div>
                <div class="notification-content">
                    <h5>${n.title}</h5>
                    <p>${n.message}</p>
                    <span class="notification-time">${n.time}</span>
                </div>
            </div>
        `).join('');

        dropdown.innerHTML = `
            <div class="notification-header">
                <h4>Notifications</h4>
                <button class="mark-all-read" id="markAllRead">Mark all read</button>
            </div>
            <div class="notification-list">
                ${notifItems || '<div class="no-notifications">No notifications</div>'}
            </div>
            <div class="notification-footer">
                <button class="view-all-btn" id="viewAllNotifs">View All</button>
            </div>
        `;

        document.body.appendChild(dropdown);

        // Smart positioning - prevent dropdown from going off-screen
        const rect = anchorElement.getBoundingClientRect();
        const dropdownWidth = 340;
        const dropdownHeight = 400;
        const spaceBelow = window.innerHeight - rect.bottom;
        const spaceAbove = rect.top;
        const spaceRight = window.innerWidth - rect.left;

        // Vertical positioning
        if (spaceBelow < dropdownHeight && spaceAbove > dropdownHeight) {
            // Open upward
            dropdown.style.bottom = `${window.innerHeight - rect.top + 8}px`;
            dropdown.style.top = 'auto';
        } else {
            // Open downward (default)
            dropdown.style.top = `${rect.bottom + 8}px`;
            dropdown.style.bottom = 'auto';
        }

        // Horizontal positioning - ensure it stays on screen
        if (spaceRight < dropdownWidth) {
            dropdown.style.right = '16px';
            dropdown.style.left = 'auto';
        } else {
            dropdown.style.left = `${rect.left}px`;
            dropdown.style.right = 'auto';
        }

        requestAnimationFrame(() => {
            dropdown.classList.add('visible');
        });

        // Event listeners
        dropdown.querySelector('#markAllRead').addEventListener('click', () => {
            markAllNotificationsRead();
            dropdown.querySelectorAll('.notification-item').forEach(el => {
                el.classList.remove('unread');
                el.classList.add('read');
            });
            showToast('All notifications marked as read', 'success');
        });

        dropdown.querySelectorAll('.notification-item').forEach(item => {
            item.addEventListener('click', () => {
                const id = parseInt(item.dataset.id);
                markNotificationRead(id);
                item.classList.remove('unread');
                item.classList.add('read');
            });
        });

        // View All button - navigate to Scan History (threat.html) which shows all notifications and scan history
        dropdown.querySelector('#viewAllNotifs').addEventListener('click', () => {
            hideNotificationDropdown();
            window.location.href = 'threat.html#scan-history';
        });

        // Close on outside click - faster
        setTimeout(() => {
            document.addEventListener('click', handleNotifOutsideClick);
        }, 50);
    }

    function hideNotificationDropdown() {
        const existing = document.getElementById('notificationDropdown');
        if (existing) {
            existing.classList.remove('visible');
            setTimeout(() => existing.remove(), 150);
        }
        document.removeEventListener('click', handleNotifOutsideClick);
    }

    function handleNotifOutsideClick(e) {
        const dropdown = document.getElementById('notificationDropdown');
        if (dropdown && !dropdown.contains(e.target) && !e.target.closest('.notification-btn')) {
            hideNotificationDropdown();
        }
    }

    // ==========================================================================
    // APP STATISTICS
    // ==========================================================================

    function getAppStats() {
        const stored = localStorage.getItem('mehrguard_stats');
        const defaults = {
            scansToday: 0,
            totalScans: 0,
            threatsBlocked: 0,
            safeUrls: 0,
            lastScanDate: null
        };

        if (stored) {
            try {
                const stats = JSON.parse(stored);
                // Reset daily counter if new day
                const today = new Date().toDateString();
                if (stats.lastScanDate !== today) {
                    stats.scansToday = 0;
                    stats.lastScanDate = today;
                    saveAppStats(stats);
                }
                return { ...defaults, ...stats };
            } catch (e) {
                return defaults;
            }
        }
        return defaults;
    }

    function saveAppStats(stats) {
        localStorage.setItem('mehrguard_stats', JSON.stringify(stats));
    }

    function incrementScanCount(isThreat = false) {
        const stats = getAppStats();
        stats.scansToday++;
        stats.totalScans++;
        stats.lastScanDate = new Date().toDateString();

        if (isThreat) {
            stats.threatsBlocked++;
        } else {
            stats.safeUrls++;
        }

        saveAppStats(stats);

        // Update any visible counters
        document.querySelectorAll('[data-stat="scans-today"]').forEach(el => {
            el.textContent = stats.scansToday;
        });
        document.querySelectorAll('[data-stat="total-scans"]').forEach(el => {
            el.textContent = stats.totalScans;
        });
        document.querySelectorAll('[data-stat="threats-blocked"]').forEach(el => {
            el.textContent = stats.threatsBlocked;
        });
    }

    // ==========================================================================
    // DATA EXPORT
    // ==========================================================================

    function exportUserData() {
        const user = getUser();
        const stats = getAppStats();
        const notifications = getNotifications();
        const scanHistory = JSON.parse(localStorage.getItem('mehrguard_history') || '[]');

        const exportData = {
            exportDate: new Date().toISOString(),
            user: user,
            statistics: stats,
            scanHistory: scanHistory,
            settings: {
                sensitivity: localStorage.getItem('mehrguard_sensitivity') || 'balanced',
                allowlist: JSON.parse(localStorage.getItem('mehrguard_allowlist') || '[]'),
                blocklist: JSON.parse(localStorage.getItem('mehrguard_blocklist') || '[]')
            }
        };

        const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `mehrguard-data-${new Date().toISOString().split('T')[0]}.json`;
        a.click();
        URL.revokeObjectURL(url);

        showToast('Data exported successfully!', 'success');
    }

    // ==========================================================================
    // TOAST NOTIFICATIONS
    // ==========================================================================

    function showToast(message, type = 'info') {
        // Remove existing toast
        const existing = document.querySelector('.shared-toast');
        if (existing) existing.remove();

        const icons = {
            success: 'check_circle',
            error: 'error',
            warning: 'warning',
            info: 'info'
        };

        const resolvedMessage = translateText(message);
        const toast = document.createElement('div');
        toast.className = `shared-toast ${type}`;
        toast.innerHTML = `
            <span class="material-symbols-outlined">${icons[type]}</span>
            <span class="toast-message">${resolvedMessage}</span>
        `;

        document.body.appendChild(toast);

        requestAnimationFrame(() => {
            toast.classList.add('visible');
        });

        setTimeout(() => {
            toast.classList.remove('visible');
            setTimeout(() => toast.remove(), 150);
        }, 2500);
    }

    // ==========================================================================
    // INITIALIZATION
    // ==========================================================================

    function init() {
        // Update all user UI elements
        updateAllUserUI();
        updateNotificationBadge();

        // Attach profile dropdown to user avatars, profiles, and header profile buttons
        // Uses toggle so clicking again closes the dropdown
        document.querySelectorAll('.user-avatar, .user-profile, #profileBtn').forEach(el => {
            el.style.cursor = 'pointer';
            el.addEventListener('click', (e) => {
                e.stopPropagation();
                toggleProfileDropdown(el);
            });
        });

        // Attach notification dropdown - use multiple selectors to catch all notification buttons
        // Uses toggle so clicking again closes the dropdown
        document.querySelectorAll('.notification-btn, #notificationBtn, .header-btn.notification').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                toggleNotificationDropdown(btn);
            });
        });

        // Attach settings button to navigate to settings page
        const settingsBtn = document.getElementById('settingsBtn');
        if (settingsBtn) {
            settingsBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                window.location.href = 'onboarding.html';
            });
        }

        // Attach help button click handler on all pages
        document.querySelectorAll('#helpBtn, .help-btn').forEach(btn => {
            btn.style.cursor = 'pointer';
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                showHelpModal();
            });
        });

        // ======================================================================
        // GLOBAL KEYBOARD SHORTCUTS  
        // ======================================================================
        setupKeyboardShortcuts();

        console.log('[SharedUI] Initialized profile, notification, help, and keyboard shortcuts');
    }

    /**
     * Setup global keyboard shortcuts
     * Uses simple letter keys (when not typing) to avoid browser shortcut conflicts
     * Note: Cmd/Ctrl+S/I/D are reserved by browsers (Save, Italics, Bookmarks)
     */
    function setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            // Don't trigger shortcuts when typing in inputs
            const isInputActive =
                document.activeElement?.tagName === 'INPUT' ||
                document.activeElement?.tagName === 'TEXTAREA' ||
                document.activeElement?.isContentEditable;

            // Escape - Close any open modal/menu (works even in inputs)
            if (e.key === 'Escape') {
                hideProfileDropdown();
                hideNotificationDropdown();
                const helpModal = document.getElementById('helpModal');
                if (helpModal) {
                    helpModal.classList.remove('visible');
                    setTimeout(() => helpModal.remove(), 150);
                }
                return;
            }

            // Don't trigger letter shortcuts when typing
            if (isInputActive) return;

            // S - Start Scanner (simple key, no modifier to avoid browser conflicts)
            if (e.key.toLowerCase() === 's' && !e.metaKey && !e.ctrlKey && !e.altKey) {
                e.preventDefault();
                window.location.href = 'scanner.html';
                return;
            }

            // I - Import Image
            if (e.key.toLowerCase() === 'i' && !e.metaKey && !e.ctrlKey && !e.altKey) {
                e.preventDefault();
                // Try to trigger file input on scanner page
                const fileInput = document.getElementById('qrImageInput') || document.getElementById('imageInput');
                if (fileInput) {
                    fileInput.click();
                } else {
                    // Navigate to scanner if not on scanner page
                    window.location.href = 'scanner.html';
                }
                return;
            }

            // D - Navigate to Dashboard  
            if (e.key.toLowerCase() === 'd' && !e.metaKey && !e.ctrlKey && !e.altKey) {
                e.preventDefault();
                window.location.href = 'dashboard.html';
                return;
            }

            // H - Navigate to Scan History
            if (e.key.toLowerCase() === 'h' && !e.metaKey && !e.ctrlKey && !e.altKey) {
                e.preventDefault();
                window.location.href = 'threat.html';
                return;
            }

            // T - Navigate to Trust Centre / Allow List
            if (e.key.toLowerCase() === 't' && !e.metaKey && !e.ctrlKey && !e.altKey) {
                e.preventDefault();
                window.location.href = 'trust.html';
                return;
            }

            // G - Navigate to Game (Beat the Bot) - Skip on scanner page where G is used for gallery
            if (e.key.toLowerCase() === 'g' && !e.metaKey && !e.ctrlKey && !e.altKey) {
                // Don't intercept on scanner page - let scanner.js handle it for gallery
                if (!window.location.pathname.includes('scanner')) {
                    e.preventDefault();
                    window.location.href = 'game.html';
                }
                return;
            }

            // ? - Show Help Modal
            if (e.key === '?' || (e.shiftKey && e.key === '/')) {
                e.preventDefault();
                showHelpModal();
                return;
            }
        });
    }

    // ==========================================================================
    // HELP MODAL
    // ==========================================================================

    /**
     * Show help modal with keyboard shortcuts - consistent dark theme
     */
    function showHelpModal() {
        // Remove existing modal if any
        const existingModal = document.getElementById('helpModal');
        if (existingModal) existingModal.remove();

        // Detect platform for display
        const isMac = navigator.platform.toUpperCase().indexOf('MAC') >= 0;
        const platformNote = isMac
            ? translateText('Works on Mac and Windows/Linux')
            : translateText('Works on Windows, Linux and Mac');

        const modal = document.createElement('div');
        modal.id = 'helpModal';
        modal.className = 'shared-modal-overlay';
        modal.innerHTML = `
            <div class="shared-modal" style="max-width: 520px;">
                <div class="shared-modal-header">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <span class="material-symbols-outlined" style="color: var(--primary, #6366f1);">help</span>
                        <h3 style="margin: 0; color: var(--text-primary, #f1f5f9);" data-i18n="HelpKeyboardShortcuts">${translateText('Help & Keyboard Shortcuts')}</h3>
                    </div>
                    <button class="modal-close-btn" id="closeHelpModal">
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>
                
                <div class="shared-modal-content" style="padding: 20px;">
                    <div class="help-section">
                        <h4 style="margin: 0 0 12px 0; color: var(--text-primary, #f1f5f9); font-size: 14px; font-weight: 600;" data-i18n="KeyboardShortcuts">${translateText('Keyboard Shortcuts')}</h4>
                        <p style="font-size: 12px; color: var(--text-muted, #64748b); margin: 0 0 12px 0;">${translateText('Press these keys when not typing in an input field:')}</p>
                        <div class="help-shortcuts-list">
                            <div class="shortcut-item">
                                <span class="shortcut-label" data-i18n="StartScanner">${translateText('Start Scanner')}</span>
                                <kbd class="shortcut-key">S</kbd>
                            </div>
                            <div class="shortcut-item">
                                <span class="shortcut-label" data-i18n="ImportImage">${translateText('Import Image')}</span>
                                <kbd class="shortcut-key">I</kbd>
                            </div>
                            <div class="shortcut-item">
                                <span class="shortcut-label" data-i18n="NavigateToDashboard">${translateText('Navigate to Dashboard')}</span>
                                <kbd class="shortcut-key">D</kbd>
                            </div>
                            <div class="shortcut-item">
                                <span class="shortcut-label" data-i18n="NavigateToHistory">${translateText('Scan History')}</span>
                                <kbd class="shortcut-key">H</kbd>
                            </div>
                            <div class="shortcut-item">
                                <span class="shortcut-label" data-i18n="NavigateToTrust">${translateText('Trust Centre / Allow List')}</span>
                                <kbd class="shortcut-key">T</kbd>
                            </div>
                            <div class="shortcut-item">
                                <span class="shortcut-label" data-i18n="NavigateToGame">${translateText('Beat the Bot Game')}</span>
                                <kbd class="shortcut-key">G</kbd>
                            </div>
                            <div class="shortcut-item">
                                <span class="shortcut-label" data-i18n="CloseMenuModal">${translateText('Close Menu / Modal')}</span>
                                <kbd class="shortcut-key">Escape</kbd>
                            </div>
                            <div class="shortcut-item">
                                <span class="shortcut-label" data-i18n="ShowHelp">${translateText('Show Help')}</span>
                                <kbd class="shortcut-key">?</kbd>
                            </div>
                        </div>
                    </div>
                    
                    <div class="help-section" style="margin-top: 16px;">
                        <h4 style="margin: 0 0 12px 0; color: var(--text-primary, #f1f5f9); font-size: 14px; font-weight: 600;" data-i18n="AboutQrShield">${translateText('About Mehr Guard')}</h4>
                        <p style="font-size: 14px; color: var(--text-secondary, #94a3b8); line-height: 1.6; margin: 0;" data-i18n="AboutDescription">
                            ${translateText('Enterprise-grade QR code security with 100% offline analysis. Your data never leaves your device. All threat detection is performed locally using our advanced phishing detection engine.')}
                        </p>
                    </div>

                    <div class="help-section" style="margin-top: 16px;">
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <span class="offline-dot" style="width: 8px; height: 8px; border-radius: 50%; background: #10b981;"></span>
                            <span style="font-size: 13px; color: var(--text-secondary, #94a3b8);" data-i18n="VersionOfflineReady">${translateText('Version 2.4.1 • Offline Ready')}</span>
                        </div>
                    </div>
                </div>
                
                <div class="shared-modal-footer">
                    <button class="btn-save" id="closeHelpBtn" data-i18n="GotIt">${translateText('Got it')}</button>
                </div>
            </div>
        `;

        // Add help-specific styles if not already present
        if (!document.getElementById('helpModalStyles')) {
            const styles = document.createElement('style');
            styles.id = 'helpModalStyles';
            styles.textContent = `
                .help-section {
                    background: var(--surface-dark, rgba(30, 41, 59, 0.5));
                    border: 1px solid var(--surface-border, rgba(255, 255, 255, 0.1));
                    border-radius: 12px;
                    padding: 16px;
                }
                .help-shortcuts-list {
                    display: flex;
                    flex-direction: column;
                    gap: 10px;
                }
                .shortcut-item {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .shortcut-label {
                    font-size: 14px;
                    color: var(--text-secondary, #94a3b8);
                }
                .shortcut-key {
                    background: var(--primary-bg, rgba(99, 102, 241, 0.15));
                    color: var(--primary, #6366f1);
                    padding: 4px 10px;
                    border-radius: 6px;
                    font-family: ui-monospace, monospace;
                    font-size: 12px;
                    font-weight: 600;
                    border: 1px solid var(--primary-border, rgba(99, 102, 241, 0.3));
                }
            `;
            document.head.appendChild(styles);
        }

        document.body.appendChild(modal);

        // Animate in
        requestAnimationFrame(() => {
            modal.classList.add('visible');
        });

        // Close modal function
        const closeModal = () => {
            modal.classList.remove('visible');
            setTimeout(() => modal.remove(), 150);
        };

        // Add event listeners
        document.getElementById('closeHelpModal')?.addEventListener('click', closeModal);
        document.getElementById('closeHelpBtn')?.addEventListener('click', closeModal);
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModal();
        });

        // Close on Escape
        const escHandler = (e) => {
            if (e.key === 'Escape') {
                closeModal();
                document.removeEventListener('keydown', escHandler);
            }
        };
        document.addEventListener('keydown', escHandler);
    }

    // ==========================================================================
    // SETTINGS MANAGEMENT
    // ==========================================================================

    const SETTINGS_KEY = 'mehrguard_settings';

    const DEFAULT_SETTINGS = {
        // Detection settings
        sensitivity: 'balanced', // strict, balanced, permissive
        autoBlock: true,
        realTimeScanning: true,

        // Privacy settings
        offlineMode: true,
        onDeviceAnalysis: true,
        noCloudLogs: true,
        onDeviceDb: true,

        // Notification settings
        soundEnabled: true,
        threatAlerts: true,
        scanSummary: true,

        // Display settings
        darkMode: true,
        compactView: false,
        showConfidence: true
    };

    function getSettings() {
        const stored = localStorage.getItem(SETTINGS_KEY);
        if (stored) {
            try {
                return { ...DEFAULT_SETTINGS, ...JSON.parse(stored) };
            } catch (e) {
                return DEFAULT_SETTINGS;
            }
        }
        return DEFAULT_SETTINGS;
    }

    function saveSettings(settings) {
        localStorage.setItem(SETTINGS_KEY, JSON.stringify({ ...DEFAULT_SETTINGS, ...settings }));
    }

    function updateSetting(key, value) {
        const settings = getSettings();
        settings[key] = value;
        saveSettings(settings);
        return settings;
    }

    // ==========================================================================
    // SCAN HISTORY MANAGEMENT
    // ==========================================================================

    const HISTORY_KEY = 'mehrguard_scan_history';
    const MAX_HISTORY_ITEMS = 50;

    function getScanHistory() {
        const stored = localStorage.getItem(HISTORY_KEY);
        if (stored) {
            try {
                return JSON.parse(stored);
            } catch (e) {
                return [];
            }
        }
        return [];
    }

    function addScanToHistory(scanResult) {
        const history = getScanHistory();

        // Normalize URL to prevent duplicates (with/without protocol, trailing slash)
        const normalizeUrl = (url) => {
            if (!url) return '';
            let normalized = url.toLowerCase().trim();
            // Remove protocol
            normalized = normalized.replace(/^https?:\/\//i, '');
            // Remove trailing slash
            normalized = normalized.replace(/\/$/, '');
            // Remove www. prefix for comparison
            normalized = normalized.replace(/^www\./i, '');
            return normalized;
        };

        const normalizedNewUrl = normalizeUrl(scanResult.url);

        // Check for duplicate - same URL scanned within last 10 seconds
        const TEN_SECONDS = 10 * 1000;
        const now = Date.now();
        const isDuplicate = history.some(entry => {
            const normalizedExisting = normalizeUrl(entry.url);
            const timeDiff = now - entry.timestamp;
            return normalizedExisting === normalizedNewUrl && timeDiff < TEN_SECONDS;
        });

        if (isDuplicate) {
            console.log('[History] Skipping duplicate entry for:', scanResult.url);
            // Return the existing entry instead of creating new
            return history.find(entry => normalizeUrl(entry.url) === normalizedNewUrl);
        }

        const entry = {
            id: `scan_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
            timestamp: Date.now(),
            url: scanResult.url || '',
            verdict: scanResult.verdict || 'UNKNOWN',
            score: scanResult.score || 0,
            signals: scanResult.signals || [],
            blocked: false,
            ...scanResult
        };

        // Add to beginning
        history.unshift(entry);

        // Keep only last N items
        const trimmed = history.slice(0, MAX_HISTORY_ITEMS);

        localStorage.setItem(HISTORY_KEY, JSON.stringify(trimmed));

        // Update stats
        const isThreat = entry.verdict === 'HIGH' || entry.verdict === 'MEDIUM' || entry.score >= 50;
        incrementScanCount(isThreat);

        return entry;
    }

    function getScanById(scanId) {
        const history = getScanHistory();
        return history.find(s => s.id === scanId);
    }

    function markScanBlocked(scanId) {
        const history = getScanHistory();
        const scan = history.find(s => s.id === scanId);
        if (scan) {
            scan.blocked = true;
            localStorage.setItem(HISTORY_KEY, JSON.stringify(history));
        }
    }

    function clearScanHistory() {
        localStorage.removeItem(HISTORY_KEY);
    }

    function getHistorySummary() {
        const history = getScanHistory();
        const now = Date.now();
        const oneDayAgo = now - (24 * 60 * 60 * 1000);
        const oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000);

        const today = history.filter(s => s.timestamp >= oneDayAgo);
        const thisWeek = history.filter(s => s.timestamp >= oneWeekAgo);

        return {
            total: history.length,
            today: today.length,
            thisWeek: thisWeek.length,
            threats: history.filter(s => s.verdict === 'HIGH' || s.verdict === 'MEDIUM').length,
            safe: history.filter(s => s.verdict === 'SAFE' || s.verdict === 'LOW').length,
            blocked: history.filter(s => s.blocked).length
        };
    }

    // Initialize when DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Expose public API
    window.MehrGuardUI = {
        // User & Stats
        getUser,
        saveUser,
        getAppStats,
        incrementScanCount,

        // Notifications
        getNotifications,
        addNotification,
        markNotificationRead,
        markAllNotificationsRead,

        // UI Helpers
        showToast,

        // Data export
        exportUserData,

        // Dropdowns
        showProfileDropdown,
        hideProfileDropdown,
        toggleProfileDropdown,
        showNotificationDropdown,
        hideNotificationDropdown,
        toggleNotificationDropdown,

        // Settings
        getSettings,
        saveSettings,
        updateSetting,

        // Scan History
        getScanHistory,
        addScanToHistory,
        getScanById,
        markScanBlocked,
        clearScanHistory,
        getHistorySummary,

        // Help Modal
        showHelpModal
    };

})();
