# Desktop App - Decorative UI Audit & Fix Task List

> **Generated**: December 24, 2025
> **Purpose**: Identify all decorative/non-functional UI elements and wire them to real functionality

---

## Overview

This document tracks all UI elements in the Desktop application that are currently **decorative** (static/placeholder data) and need to be connected to **functional code**.

### Priority Legend
- 🔴 **Critical** - Core feature, completely non-functional
- 🟠 **High** - Important feature, partially functional
- 🟡 **Medium** - Secondary feature, works but with placeholder data
- 🟢 **Low** - Minor enhancement

---

## 1. Beat the Bot (Training Screen) 🔴

**Current State**: UI displays static training scenarios but game loop is incomplete.

### Issues Identified:

| Component | Web Implementation | Desktop Status | Gap |
|-----------|-------------------|----------------|-----|
| Game State | `GameState` object with full tracking | `TrainingState` data class | ✅ Exists |
| Challenge Database | 10 diverse phishing/safe URLs | 3 hardcoded scenarios | 🔴 Missing |
| Round Progression | Dynamic loading + shuffling | Static `trainingScenarioIndex` | 🟠 Partial |
| Phishing/Legitimate Buttons | `handleDecision(isPhishing)` | `submitTrainingVerdict` exists | 🟠 Partial |
| Result Modal | Full modal with points/time | No modal, inline state only | 🔴 Missing |
| Game Over Modal | Final scores, play again | Missing | 🔴 Missing |
| Bot Score | Dynamic, always correct | Missing entirely | 🔴 Missing |
| Response Time | Tracked per decision | Not tracked | 🟠 Missing |
| Best Streak | Tracked and saved | Missing | 🟡 Missing |
| Live Hints | Dynamic per challenge | Static text | 🟠 Partial |
| Educational Notes | Per-challenge explanations | Static placeholder | 🟠 Partial |
| Scoreboard | Player vs Bot comparison | Player only | 🔴 Missing |
| Session ID | Generated, displayed | Static | 🟡 Missing |
| End Session Button | Confirms and redirects | Missing click handler | 🟡 Missing |
| Keyboard Shortcuts | P/L keys for decisions | Missing | 🟡 Missing |

### Tasks Required:

```
[ ] 1.1 Create Challenge database with 10 phishing/safe scenarios matching web
    File: AppViewModel.kt
    Add: Complete CHALLENGES list with url, message, sender, isPhishing, hint, reasons, educational

[ ] 1.2 Add Bot scoring logic
    File: AppViewModel.kt
    Add: botScore, botStreak, updateBotScore() function

[ ] 1.3 Add response time tracking
    File: AppViewModel.kt
    Add: roundStartTime, trackResponseTime()

[ ] 1.4 Create Result Modal component
    File: TrainingScreen.kt
    Add: ResultModal composable with points, time, next round button

[ ] 1.5 Create Game Over Modal component
    File: TrainingScreen.kt
    Add: GameOverModal with final scores, play again, return to dashboard

[ ] 1.6 Wire "End Session" button
    File: TrainingScreen.kt
    Add: Click handler with confirmation dialog

[ ] 1.7 Add keyboard shortcuts
    File: TrainingScreen.kt
    Add: Key event handling for P=Phishing, L=Legitimate, Enter=Next

[ ] 1.8 Create VS Mode Scoreboard component
    File: TrainingScreen.kt
    Add: Side-by-side comparison with animated bars

[ ] 1.9 Persist game stats locally
    File: AppViewModel.kt
    Add: Save/load high scores, best streaks using SettingsManager
```

---

## 2. Profile/User Section 🟠

**Current State**: Static "Security Analyst" text, clicking navigates to Settings but no profile management.

### Issues Identified:

| Component | Web Implementation | Desktop Status | Gap |
|-----------|-------------------|----------------|-----|
| Profile Data | Dynamic user info | Hardcoded in `SampleData.kt` | 🟠 Partial |
| Avatar | Initials in gradient circle | ✅ Exists | Match |
| Profile Dropdown | Full dropdown with actions | Click → Settings only | 🟠 Partial |
| Edit Profile | Form to update name/role | Missing | 🔴 Missing |
| Profile Stats | Scans today, threats blocked | Missing | 🟡 Missing |
| Sign Out | Button in dropdown | Missing | 🟡 Missing |
| Account Plan | "Enterprise Plan" display | Hardcoded in SampleData | 🟡 Static |
| User Preferences | Theme, Language in dropdown | Done via TrustCentreAlt | ✅ Works |

### Tasks Required:

```
[ ] 2.1 Create Profile data model with persistence
    File: AppViewModel.kt
    Add: UserProfile state, loadProfile(), saveProfile()

[ ] 2.2 Create ProfileDropdown component (similar to NotificationPanel)
    File: ui/ProfileDropdown.kt (new)
    Add: Dropdown with avatar, name, role, stats, actions

[ ] 2.3 Add profile stats tracking
    File: AppViewModel.kt
    Add: scansToday, threatsBlocked computed from history

[ ] 2.4 Allow profile name/initials editing
    File: ProfileDropdown.kt
    Add: Edit mode with text fields

[ ] 2.5 Wire profile click to show dropdown
    File: AppSidebar.kt
    Update: onProfileClick to toggle dropdown vs navigate

[ ] 2.6 Add "View Full Profile" action
    File: ProfileDropdown.kt
    Add: Link to TrustCentreAlt screen
```

---

## 3. Notification System 🟡

**Current State**: NotificationPanel created but uses sample data. No real notifications triggered.

### Issues Identified:

| Component | Web Implementation | Desktop Status | Gap |
|-----------|-------------------|----------------|-----|
| Notification Panel | Dropdown with actions | ✅ Just implemented | New |
| Sample Data | Placeholder notifications | Hardcoded list | 🟡 Static |
| Threat Detection | Triggers notification | Not wired | 🔴 Missing |
| Scan Complete | Triggers notification | Not wired | 🔴 Missing |
| Settings Save | Triggers notification | Not wired | 🟡 Missing |
| Notification Persistence | LocalStorage | Not persisted | 🟡 Missing |
| Notification Badge Count | Dynamic unread count | ✅ Works | Match |
| Mark Read | Clicking marks as read | ✅ Works | Match |
| Clear All | Clears list | ✅ Works | Match |

### Tasks Required:

```
[ ] 3.1 Trigger notification on threat detection
    File: AppViewModel.kt (updateResult function)
    Add: if (verdict == MALICIOUS) addNotification(...)

[ ] 3.2 Trigger notification on safe scan
    File: AppViewModel.kt (updateResult function)
    Add: if (verdict == SAFE) addNotification(...)

[ ] 3.3 Trigger notification on settings save
    File: AppViewModel.kt (persistSettings function)
    Add: addNotification("Settings Saved", ...)

[ ] 3.4 Persist notifications to settings store
    File: AppViewModel.kt
    Add: Save/load notifications list

[ ] 3.5 Wire notification panel to ALL screens (not just Dashboard)
    Current: Only DashboardScreen
    Needed: LiveScanScreen, ScanHistoryScreen, ResultScreens, etc.
```

---

## 4. Result Screens (Safe/Suspicious/Dangerous) 🟠

**Current State**: UI displays analysis data correctly BUT many elements are decorative.

### Issues Identified:

| Component | Web Implementation | Desktop Status | Gap |
|-----------|-------------------|----------------|-----|
| Verdict Card | Dynamic from analysis | ✅ Works | Match |
| Confidence Score | From assessment.confidence | ✅ Works | Match |
| Analysis Time | Tracked | ✅ Works | Match |
| URL Display | From scan | ✅ Works | Match |
| Visit URL | Opens browser | ✅ Works (openUrl) | Match |
| Copy Link | Clipboard | ✅ Works (copyUrl) | Match |
| Share Report | Text summary | shareTextReport exists | 🟠 Partial |
| Export Report | Navigate to export | Works | Match |
| Notification Icon | Static message | Not using NotificationPanel | 🟡 Missing |
| Analysis Cards | Static text | Need dynamic from verdictDetails | 🟠 Partial |
| Technical Rows | Hardcoded values | Need from assessment | 🔴 Hardcoded |
| Destination Preview | "Preview unavailable" | Decorative | 🟡 Intentional |
| Sandbox Button | Web opens in sandbox | Not applicable to desktop | N/A |
| Risk Meter | 5 segments | Exists but hardcoded | 🟠 Partial |

### Tasks Required:

```
[ ] 4.1 Wire notification icon to NotificationPanel
    Files: ResultSafeScreen.kt, ResultDangerousScreen.kt, ResultSuspiciousScreen.kt
    Update: Replace showInfo() with toggleNotificationPanel()

[ ] 4.2 Make Analysis Cards dynamic
    Files: All Result screens
    Update: Populate from verdictDetails.factors instead of hardcoded

[ ] 4.3 Make Technical Rows dynamic
    Files: All Result screens
    Current: "DigiCert Inc (US)" hardcoded
    Update: Pull from RiskAssessment triggers/indicators

[ ] 4.4 Wire Risk Meter correctly
    Files: ResultDangerousScreen.kt, ResultSuspiciousScreen.kt
    Update: Calculate segment fill from score

[ ] 4.5 Add action bar with consistent buttons
    Files: All Result screens
    Add: Back, Share Report, Copy Link buttons in fixed bar

[ ] 4.6 Review each result screen individually
    - ResultSafeScreen.kt: Mostly works, technical rows hardcoded
    - ResultSuspiciousScreen.kt: Check warnings from assessment
    - ResultDangerousScreen.kt: Show threats from assessment
    - ResultDangerousAltScreen.kt: Same as above
```

---

## 5. Dashboard Screen 🟡

**Current State**: Mostly functional but some decorative elements remain.

### Issues Identified:

| Component | Status | Notes |
|-----------|--------|-------|
| Stats Cards | ✅ Works | Pulls from historyStats |
| Recent Scans Table | ✅ Works | From scanHistory |
| URL Analyze | ✅ Works | analyzeUrlDirectly |
| Quick Actions | ✅ Works | Navigate correctly |
| Notification Icon | ✅ Fixed | Uses NotificationPanel now |
| Settings Icon | ✅ Works | Navigates to TrustCentreAlt |
| Check Updates | 🟡 Decorative | Shows info message only |
| Engine Status | 🟡 Static | "v2.4 Offline" hardcoded |
| Security Score Ring | 🟡 Static | Always shows same value |

### Tasks Required:

```
[ ] 5.1 Calculate dynamic Security Score
    File: DashboardScreen.kt
    Update: Compute from historyStats (safe % × 100)

[ ] 5.2 Make Engine Status dynamic
    File: DashboardScreen.kt
    Add: Show actual Phishing Engine version

[ ] 5.3 Consider "Check Updates" functionality
    File: DashboardScreen.kt
    Either: Remove button or add actual version check
```

---

## 6. Settings/Trust Centre Screens 🟡

**Current State**: UI exists but some toggles are decorative.

### Issues Identified:

| Component | Status | Notes |
|-----------|--------|-------|
| Strict Offline Toggle | ✅ Works | Persisted |
| Telemetry Toggle | ✅ Works | Persisted |
| Auto-Copy Safe Toggle | ✅ Works | Persisted |
| Heuristic Sensitivity | ✅ Works | Updates engine |
| Allowlist/Blocklist | ✅ Works | Fully functional |
| Auto-Block Threats | 🟡 Toggle exists | Not connected to logic |
| Real-Time Scanning | 🟡 Toggle exists | Not connected to logic |
| Sound Alerts | 🟡 Toggle exists | Not connected to logic |
| Threat Alerts | 🟡 Toggle exists | Not connected to logic |
| Show Confidence Score | 🟡 Toggle exists | Not used in UI |
| Language Selector | ✅ Works | Changes app language |

### Tasks Required:

```
[ ] 6.1 Wire Auto-Block Threats toggle
    File: AppViewModel.kt
    Effect: Automatically add malicious domains to blocklist

[ ] 6.2 Wire Real-Time Scanning toggle
    File: AppViewModel.kt
    Effect: Analytics purpose only or enable camera monitoring

[ ] 6.3 Wire Sound Alerts toggle
    File: AppViewModel.kt
    Effect: Play sound on threat detection (needs audio API)

[ ] 6.4 Wire Threat Alerts toggle
    File: AppViewModel.kt
    Effect: Show desktop notification on threat

[ ] 6.5 Use Show Confidence Score preference
    Files: Result screens
    Effect: Conditionally show/hide confidence percentage
```

---

## 7. Scan History Screen 🟢

**Current State**: Mostly functional.

### Issues Identified:

| Component | Status | Notes |
|-----------|--------|-------|
| History List | ✅ Works | From repository |
| Filter Tabs | ✅ Works | All/Safe/Suspicious/Dangerous |
| Search | ✅ Works | Filters by URL |
| Click to View | ✅ Works | analyzeUrl with recordHistory=false |
| Delete History | 🟡 Missing | Cannot clear individual items |
| Export CSV | ✅ Works | exportHistoryCsv function |
| Notification Icon | 🟡 Static | Uses showInfo() |

### Tasks Required:

```
[ ] 7.1 Add delete/clear history functionality
    File: ScanHistoryScreen.kt, AppViewModel.kt
    Add: Clear all history button, individual delete

[ ] 7.2 Wire notification icon
    File: ScanHistoryScreen.kt
    Update: Use NotificationPanel
```

---

## 8. Live Scan Screen 🟢

**Current State**: Mostly functional.

### Issues Identified:

| Component | Status | Notes |
|-----------|--------|-------|
| Camera Scan | ✅ Works | startCameraScan() |
| Image Import | ✅ Works | pickImageAndScan() |
| Clipboard Paste | ✅ Works | analyzeClipboardUrl() |
| Visual/Raw Toggle | ✅ Works | View mode switch |
| Notification Icon | 🟡 Static | Uses showInfo() |

### Tasks Required:

```
[ ] 8.1 Wire notification icon
    File: LiveScanScreen.kt
    Update: Use NotificationPanel
```

---

## 9. Reports/Export Screen 🟢

**Current State**: Mostly functional.

### Issues Identified:

| Component | Status | Notes |
|-----------|--------|-------|
| Format Selector | ✅ Works | PDF/JSON toggle |
| Filename Input | ✅ Works | Editable |
| Include Options | ✅ Works | Checkboxes wired |
| Export Button | ✅ Works | exportReport() |
| Preview Area | 🟡 Static | Shows placeholder |

### Tasks Required:

```
[ ] 9.1 Make preview dynamic
    File: ReportsExportScreen.kt
    Update: Show actual report preview from current assessment
```

---

## Summary by Priority

### 🔴 Critical (Must Fix)
1. Beat the Bot game loop completion
2. Notification triggers from scans
3. Dynamic analysis data in Result screens

### 🟠 High (Should Fix)
1. Profile dropdown with actions
2. Bot scoring in Training
3. Risk Meter calculation
4. Analysis cards from real data

### 🟡 Medium (Nice to Have)
1. Keyboard shortcuts in Training
2. Settings persistence for new toggles
3. Notification persistence
4. Preview in Export screen

### 🟢 Low (Optional)
1. Delete individual history items
2. Sound alerts
3. Desktop notifications

---

## Implementation Recommendations

### Phase 1: Core Functionality (1-2 days)
- Complete Beat the Bot game loop
- Wire notifications to scan results
- Make Result screen data dynamic

### Phase 2: User Experience (1 day)
- Profile dropdown
- Notification persistence
- All screens use NotificationPanel

### Phase 3: Polish (1 day)
- Keyboard shortcuts
- Sound/desktop alerts
- Preview improvements

---

## Files Requiring Modification

| File | Priority | Estimated Changes |
|------|----------|-------------------|
| `AppViewModel.kt` | 🔴 | +200 lines (game logic, notifications) |
| `TrainingScreen.kt` | 🔴 | +400 lines (modals, scoreboard) |
| `ResultSafeScreen.kt` | 🟠 | ~50 lines (dynamic data) |
| `ResultDangerousScreen.kt` | 🟠 | ~50 lines (dynamic data) |
| `ResultSuspiciousScreen.kt` | 🟠 | ~50 lines (dynamic data) |
| `ui/ProfileDropdown.kt` | 🟠 | New file ~150 lines |
| `SampleData.kt` | 🟡 | Remove, use SettingsManager |
| `DashboardScreen.kt` | 🟡 | ~20 lines |
| `ScanHistoryScreen.kt` | 🟢 | ~20 lines |
| `LiveScanScreen.kt` | 🟢 | ~10 lines |

---

*This task list will be updated as items are completed.*
