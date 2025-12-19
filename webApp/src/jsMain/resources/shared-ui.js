/**
 * QR-SHIELD Shared UI Controller
 * Handles common interactive elements across all pages:
 * - User Profile dropdown and settings
 * - Notification system
 * - Quick actions
 * - App statistics
 */

(function () {
    'use strict';

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
        const stored = localStorage.getItem('qrshield_user');
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
        localStorage.setItem('qrshield_user', JSON.stringify(user));
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
     * Create and show the profile dropdown
     */
    function showProfileDropdown(anchorElement) {
        // Remove existing dropdown
        hideProfileDropdown();

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

        // Position the dropdown - open upward if at bottom of screen
        const rect = anchorElement.getBoundingClientRect();
        const dropdownHeight = 320; // Approximate height of dropdown
        const spaceBelow = window.innerHeight - rect.bottom;
        const spaceAbove = rect.top;

        // If not enough space below, open upward
        if (spaceBelow < dropdownHeight && spaceAbove > dropdownHeight) {
            // Open upward
            dropdown.style.bottom = `${window.innerHeight - rect.top + 8}px`;
            dropdown.style.top = 'auto';
        } else {
            // Open downward (default)
            dropdown.style.top = `${rect.bottom + 8}px`;
            dropdown.style.bottom = 'auto';
        }
        dropdown.style.left = `${rect.left}px`;

        // Animate in
        requestAnimationFrame(() => {
            dropdown.classList.add('visible');
        });

        // Add event listeners
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

    const NOTIFICATIONS_KEY = 'qrshield_notifications';

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

        // Position
        const rect = anchorElement.getBoundingClientRect();
        dropdown.style.top = `${rect.bottom + 8}px`;
        dropdown.style.right = `${window.innerWidth - rect.right}px`;

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
        const stored = localStorage.getItem('qrshield_stats');
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
        localStorage.setItem('qrshield_stats', JSON.stringify(stats));
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
        const scanHistory = JSON.parse(localStorage.getItem('qrshield_history') || '[]');

        const exportData = {
            exportDate: new Date().toISOString(),
            user: user,
            statistics: stats,
            scanHistory: scanHistory,
            settings: {
                sensitivity: localStorage.getItem('qrshield_sensitivity') || 'balanced',
                allowlist: JSON.parse(localStorage.getItem('qrshield_allowlist') || '[]'),
                blocklist: JSON.parse(localStorage.getItem('qrshield_blocklist') || '[]')
            }
        };

        const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `qrshield-data-${new Date().toISOString().split('T')[0]}.json`;
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

        const toast = document.createElement('div');
        toast.className = `shared-toast ${type}`;
        toast.innerHTML = `
            <span class="material-symbols-outlined">${icons[type]}</span>
            <span class="toast-message">${message}</span>
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

        // Attach profile dropdown to user avatars and profiles
        document.querySelectorAll('.user-avatar, .user-profile').forEach(el => {
            el.style.cursor = 'pointer';
            el.addEventListener('click', (e) => {
                e.stopPropagation();
                showProfileDropdown(el);
            });
        });

        // Attach notification dropdown
        document.querySelectorAll('.notification-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                showNotificationDropdown(btn);
            });
        });

        console.log('[SharedUI] Initialized profile and notification systems');
    }

    // Initialize when DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Expose public API
    window.QRShieldUI = {
        getUser,
        saveUser,
        getAppStats,
        incrementScanCount,
        getNotifications,
        addNotification,
        showToast,
        exportUserData,
        showProfileDropdown,
        showNotificationDropdown
    };

})();
