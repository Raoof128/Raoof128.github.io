# EXPORT_SURFACE.md - Web to Desktop Parity Mapping

## Overview

This document inventories the Web app's behavioral surface and maps each item to its Desktop equivalent.

**Generated:** 2025-12-27  
**Web App Version:** 2.4.1  
**Desktop App Version:** Based on current codebase analysis

## Status Legend

| Status | Description |
|--------|-------------|
| ✅ MATCHED | Full parity - behavior matches Web implementation |
| ⚠️ PARTIAL | Exists but behavior differs or is incomplete |
| ❌ MISSING | Not implemented in Desktop |

## Applicability Legend

| Label | Description |
|-------|-------------|
| MUST PORT | Core behavior that Desktop must have |
| DESKTOP ADAPT | Same intent, needs desktop-native implementation |
| SKIP | Web-only concept with no sensible desktop analogue |

---

## 1. UI SCREENS

| Web Item | Category | Location | Desktop Status | Applicability | Notes |
|----------|----------|----------|----------------|---------------|-------|
| `dashboard.html` | UI Screen | `webApp/src/jsMain/resources/dashboard.html` | ✅ MATCHED | - | `DashboardScreen.kt` |
| `scanner.html` | UI Screen | `webApp/src/jsMain/resources/scanner.html` | ✅ MATCHED | - | `LiveScanScreen.kt` |
| `results.html` | UI Screen | `webApp/src/jsMain/resources/results.html` | ✅ MATCHED | - | `ResultSafeScreen.kt`, `ResultSuspiciousScreen.kt`, `ResultDangerousScreen.kt` |
| `game.html` | UI Screen | `webApp/src/jsMain/resources/game.html` | ✅ MATCHED | - | `TrainingScreen.kt` |
| `trust.html` | UI Screen | `webApp/src/jsMain/resources/trust.html` | ✅ MATCHED | - | `TrustCentreScreen.kt` |
| `export.html` | UI Screen | `webApp/src/jsMain/resources/export.html` | ✅ MATCHED | - | `ReportsExportScreen.kt` |
| `onboarding.html` | UI Screen | `webApp/src/jsMain/resources/onboarding.html` | ⚠️ PARTIAL | DESKTOP ADAPT | `TrustCentreAltScreen.kt` - Settings exist but no dedicated onboarding flow |
| `threat.html` (Scan History) | UI Screen | `webApp/src/jsMain/resources/threat.html` | ✅ MATCHED | - | `ScanHistoryScreen.kt` |

---

## 2. DASHBOARD FUNCTIONS (`dashboard.js`)

| Web Function | Category | Location (Lines) | Inputs/Outputs | Desktop Status | Notes |
|--------------|----------|------------------|----------------|----------------|-------|
| `renderStats()` | Domain Logic | dashboard.js:158-174 | Gets stats from MehrGuardUI, updates DOM | ✅ MATCHED | `historyStats` in AppViewModel |
| `renderHistory()` | UI Component | dashboard.js:225-326 | Renders history table with click handlers | ✅ MATCHED | `DashboardScreen.kt` - RecentScanRow |
| `handleImport()` | Action | dashboard.js:373-391 | Opens file picker, navigates to scanner | ✅ MATCHED | `pickImageAndScan()` in AppViewModel |
| `handleUpdateDb()` | Action | dashboard.js:396-414 | Shows loading, displays toast | ✅ MATCHED | Desktop shows static version info |
| `analyzeUrl()` | Domain Logic | dashboard.js:471-513 | Validates URL, calls Kotlin engine | ✅ MATCHED | `analyzeUrl()` in AppViewModel |
| `toggleSidebar()` | UI | dashboard.js:531-536 | Toggles mobile sidebar | SKIP | Not applicable - Desktop uses Compose layout |
| `setupKeyboardShortcuts()` | Interaction | dashboard.js:553-574 | S=scan, I=import, Esc=close | ✅ MATCHED | `Main.kt` - handleGlobalKeyEvent |
| `showToast()` | UI Helper | dashboard.js:583-619 | Shows notification toast | ✅ MATCHED | `statusMessage` in AppViewModel |

---

## 3. SCANNER FUNCTIONS (`scanner.js`)

| Web Function | Category | Location | Inputs/Outputs | Desktop Status | Applicability | Notes |
|--------------|----------|----------|----------------|----------------|---------------|-------|
| `enableCamera()` | Camera | scanner.js:284-327 | Requests camera access, starts scanning | SKIP | - | Web uses MediaDevices API; Desktop would need native camera |
| `stopCamera()` | Camera | scanner.js:332-344 | Stops camera stream | SKIP | - | Not applicable without camera |
| `toggleTorch()` | Camera | scanner.js:349-377 | Toggles flashlight | SKIP | - | Mobile/camera only |
| `scanFrame()` | QR Scanning | scanner.js:410-441 | Processes video frame for QR codes | SKIP | - | Camera-based scanning |
| `handleQRDetected()` | Domain Logic | scanner.js:446-477 | Validates QR, calls analysis | ✅ MATCHED | - | `handleScanResult()` in AppViewModel |
| `openGallery()` | Action | scanner.js:486-488 | Opens file picker | ✅ MATCHED | - | `pickImageAndScan()` |
| `handleImageUpload()` | Domain Logic | scanner.js:493-519 | Reads image, scans for QR | ✅ MATCHED | - | `scanImageFile()` |
| `scanImageForQR()` | Domain Logic | scanner.js:524-543 | Scans image for QR codes | ✅ MATCHED | - | Uses DesktopQrScanner |
| `setupDragAndDrop()` | UI | scanner.js:549-575 | Drag and drop for images | ❌ MISSING | DESKTOP ADAPT | Desktop should support drag-drop |
| `openUrlModal()` | UI | scanner.js:581-584 | Opens URL input modal | ✅ MATCHED | - | URL input in LiveScanScreen |
| `analyzeUrlFromModal()` | Domain Logic | scanner.js:591-619 | Validates and analyzes URL | ✅ MATCHED | - | `analyzeUrl()` |
| `renderHistory()` | UI | scanner.js:625-684 | Shows recent scans in sidebar | ✅ MATCHED | - | Shown in DashboardScreen |

---

## 4. RESULTS FUNCTIONS (`results.js`)

| Web Function | Category | Location | Inputs/Outputs | Desktop Status | Notes |
|--------------|----------|----------|----------------|----------------|-------|
| `initializeFromURL()` | Init | results.js:72-138 | Parses URL params, loads scan data | ✅ MATCHED | AppViewModel handles navigation with result |
| `getFactorsForVerdict()` | Domain Logic | results.js:152-181 | Returns analysis factors based on verdict | ✅ MATCHED | `VerdictEngine.enrich()` |
| `displayResult()` | UI | results.js:302-326 | Updates UI with scan result | ✅ MATCHED | ResultSafeScreen/SuspiciousScreen/DangerousScreen |
| `updateVerdictDisplay()` | UI | results.js:331-405 | Sets verdict styling and text | ✅ MATCHED | Composables in result screens |
| `updateRiskMeter()` | UI | results.js:410-489 | Visual risk meter with segments | ✅ MATCHED | Desktop has risk visualization |
| `updateFactors()` | UI | results.js:494-529 | Renders analysis factor cards | ✅ MATCHED | Factor cards in result screens |
| `shareReport()` | Action | results.js:552-576 | Uses Web Share API or fallback | ✅ MATCHED | `shareTextReport()` in AppViewModel |
| `openInSandbox()` | Action | results.js:589-720+ | Shows URL analysis modal | ✅ MATCHED | `openUrl()` with offline check |
| `copyLink()` | Action | results.js:265 (implied) | Copies URL to clipboard | ✅ MATCHED | `copyUrl()` |

---

## 5. GAME/TRAINING FUNCTIONS (`game.js`)

| Web Function | Category | Location | Inputs/Outputs | Desktop Status | Notes |
|--------------|----------|----------|----------------|----------------|-------|
| `CHALLENGES` array | Data | game.js:61-192 | 10 phishing scenarios | ✅ MATCHED | `trainingScenarios` in AppViewModel |
| `initializeGame()` | Init | game.js:357-373 | Shuffles challenges, sets up game | ✅ MATCHED | `resetTrainingGame()` |
| `loadRound()` | Game Logic | game.js:375-388 | Loads current challenge | ✅ MATCHED | `currentTrainingScenario` |
| `handleDecision()` | Game Logic | game.js:390-430 | Processes player answer | ✅ MATCHED | `submitTrainingVerdict()` |
| `nextRound()` | Game Logic | game.js:432-441 | Advances to next round | ✅ MATCHED | `dismissTrainingResultModal()` |
| `endGame()` | Game Logic | game.js:443-489 | Shows game over modal | ✅ MATCHED | `endTrainingSession()` |
| `resetGame()` | Game Logic | game.js:491-508 | Resets game state | ✅ MATCHED | `resetTrainingGame()` |
| `updateScoreDisplay()` | UI | game.js:548-579 | Updates score bars and stats | ✅ MATCHED | TrainingScreen composables |
| `showResultModal()` | UI | game.js:638-676 | Shows correct/wrong modal | ✅ MATCHED | `showResultModal` in TrainingState |
| `saveGameStats()` | Persistence | game.js:694-705 | Saves to localStorage | ⚠️ PARTIAL | MUST PORT | Game stats not persisted to file in Desktop |

---

## 6. TRUST CENTRE FUNCTIONS (`trust.js`)

| Web Function | Category | Location | Inputs/Outputs | Desktop Status | Notes |
|--------------|----------|----------|----------------|----------------|-------|
| `loadSettings()` | Persistence | trust.js:237-275 | Loads from localStorage | ✅ MATCHED | SettingsManager.load() |
| `saveSettings()` | Persistence | trust.js:280-291 | Saves to localStorage | ✅ MATCHED | SettingsManager.save() |
| `resetSettings()` | Action | trust.js:296-316 | Resets to defaults | ⚠️ PARTIAL | MUST PORT | No explicit reset button in Desktop |
| `SensitivityLevels` | Config | trust.js:100-117 | Low/Balanced/Paranoia settings | ✅ MATCHED | HeuristicSensitivity enum |
| `handleSensitivityChange()` | Action | trust.js:479-486 | Updates sensitivity setting | ✅ MATCHED | `updateHeuristicSensitivity()` |
| `renderAllowlist()` | UI | trust.js:379-407 | Renders allowlist items | ✅ MATCHED | AllowListCard composable |
| `renderBlocklist()` | UI | trust.js:412-440 | Renders blocklist items | ✅ MATCHED | BlockListCard composable |
| `openModal()` / `closeModal()` | UI | trust.js:495-519 | Add domain modal | ✅ MATCHED | Dialog in TrustCentreScreen |
| `addDomain()` | Action | trust.js:524-560 | Adds domain to list | ✅ MATCHED | `addAllowlistDomain()` / `addBlocklistDomain()` |
| `removeDomain()` | Action | trust.js:565-578 | Removes domain from list | ✅ MATCHED | `removeAllowlistDomain()` / `removeBlocklistDomain()` |
| `renderToggles()` | UI | trust.js:445-460 | Privacy toggles | ✅ MATCHED | ToggleCard composable |
| `generateSecurityAudit()` | Export | trust.js:735-796 | Downloads audit JSON | ⚠️ PARTIAL | MUST PORT | Desktop has export but not full audit format |

---

## 7. EXPORT FUNCTIONS (`export.js`)

| Web Function | Category | Location | Inputs/Outputs | Desktop Status | Notes |
|--------------|----------|----------|----------------|----------------|-------|
| `loadReportData()` | Init | export.js:164-194 | Gets data from URL params or localStorage | ✅ MATCHED | Uses currentAssessment from AppViewModel |
| `handleFormatChange()` | UI | export.js:220-224 | Switches PDF/JSON format | ✅ MATCHED | `exportFormat` in AppViewModel |
| `updatePreview()` | UI | export.js:245-297 | Live preview of report | ⚠️ PARTIAL | MUST PORT | Desktop preview could be more dynamic |
| `exportAsPDF()` | Export | export.js:353-431 | Generates HTML, triggers print | ⚠️ PARTIAL | DESKTOP ADAPT | Uses HTML output, not native PDF |
| `exportAsJSON()` | Export | export.js:436-468 | Downloads JSON file | ✅ MATCHED | `exportReport()` with ExportFormat.Json |
| `copyReport()` | Action | export.js:473-512 | Copies text report to clipboard | ✅ MATCHED | `shareTextReport()` |
| `shareReport()` | Action | export.js:517-545 | Uses Web Share API or fallback | ⚠️ PARTIAL | DESKTOP ADAPT | Desktop copies to clipboard (no native share) |

---

## 8. SHARED UI FUNCTIONS (`shared-ui.js`)

| Web Function | Category | Location | Inputs/Outputs | Desktop Status | Notes |
|--------------|----------|----------|----------------|----------------|-------|
| `getUser()` / `saveUser()` | Persistence | shared-ui.js:57-75 | User profile management | ⚠️ PARTIAL | MUST PORT | Desktop has profile dropdown but no user persistence |
| `showProfileDropdown()` | UI | shared-ui.js:125-244 | User profile popup | ✅ MATCHED | ProfileDropdown.kt |
| `showEditProfileModal()` | UI | shared-ui.js:265-350 | Edit profile form | ❌ MISSING | MUST PORT | No profile editing in Desktop |
| `getNotifications()` | Persistence | shared-ui.js:363-373 | Gets notifications from localStorage | ✅ MATCHED | `notifications` in AppViewModel |
| `addNotification()` | Action | shared-ui.js:412-421 | Adds new notification | ✅ MATCHED | `addNotification()` |
| `markNotificationRead()` | Action | shared-ui.js:423-430 | Marks single notification read | ✅ MATCHED | `markNotificationRead()` |
| `markAllNotificationsRead()` | Action | shared-ui.js:432-436 | Marks all read | ✅ MATCHED | `markAllNotificationsRead()` |
| `showNotificationDropdown()` | UI | shared-ui.js:466-564 | Notification popup | ✅ MATCHED | NotificationPanel.kt |
| `getAppStats()` | Domain Logic | shared-ui.js:586-612 | Gets scan statistics | ✅ MATCHED | `historyStats` |
| `incrementScanCount()` | Domain Logic | shared-ui.js:618-642 | Updates scan counters | ✅ MATCHED | Automatic via historyRepository |
| `exportUserData()` | Export | shared-ui.js:648-675 | Full data export | ⚠️ PARTIAL | MUST PORT | Desktop export doesn't include all user data |
| `showToast()` | UI | shared-ui.js:681-711 | Toast notification | ✅ MATCHED | statusMessage in AppViewModel |
| `getSettings()` / `saveSettings()` | Persistence | shared-ui.js:782-803 | App settings | ✅ MATCHED | SettingsManager |
| `getScanHistory()` | Persistence | shared-ui.js:812-822 | Gets scan history | ✅ MATCHED | `scanHistory` via historyRepository |
| `addScanToHistory()` | Persistence | shared-ui.js:824-851 | Adds scan to history | ✅ MATCHED | `historyManager.recordScan()` |
| `getScanById()` | Persistence | shared-ui.js:853-856 | Retrieves scan by ID | ✅ MATCHED | Can be added via repository |
| `clearScanHistory()` | Action | shared-ui.js:867-869 | Clears all history | ❌ MISSING | MUST PORT | No clear history option in Desktop |
| `getHistorySummary()` | Domain Logic | shared-ui.js:871-888 | Summary stats | ✅ MATCHED | `computeStats()` |

---

## 9. SETTINGS & CONFIGURATION

| Web Setting | Category | Location | Desktop Status | Notes |
|-------------|----------|----------|----------------|-------|
| `sensitivity` | Detection | trust.js | ✅ MATCHED | `heuristicSensitivity` |
| `strictOffline` | Privacy | trust.js | ✅ MATCHED | `trustCentreToggles.strictOffline` |
| `anonymousTelemetry` | Privacy | trust.js | ✅ MATCHED | `trustCentreToggles.anonymousTelemetry` |
| `autoCopySafeLinks` | Behavior | trust.js | ✅ MATCHED | `trustCentreToggles.autoCopySafe` |
| `autoBlock` | Detection | shared-ui.js:763 | ✅ MATCHED | `autoBlockThreats` |
| `realTimeScanning` | Detection | shared-ui.js:764 | ✅ MATCHED | `realTimeScanning` |
| `soundEnabled` | Notification | shared-ui.js:772 | ✅ MATCHED | `soundAlerts` |
| `threatAlerts` | Notification | shared-ui.js:773 | ✅ MATCHED | `threatAlerts` |
| `showConfidence` | Display | shared-ui.js:779 | ✅ MATCHED | `showConfidenceScore` |
| `darkMode` | Display | shared-ui.js:777 | ✅ MATCHED | `isDarkMode` |

---

## 10. NAVIGATION & KEYBOARD

| Web Feature | Category | Desktop Status | Notes |
|-------------|----------|----------------|-------|
| Mobile sidebar toggle | Navigation | SKIP | Desktop uses fixed sidebar |
| Keyboard: S = Start scan | Shortcut | ⚠️ PARTIAL | Cmd+2 goes to LiveScan |
| Keyboard: I = Import | Shortcut | ❌ MISSING | MUST PORT |
| Keyboard: Escape = Close | Shortcut | ✅ MATCHED | handleEscapeKey |
| Keyboard: V = Paste URL | Shortcut | ⚠️ PARTIAL | Cmd+V works |
| Keyboard: G = Gallery | Shortcut | ❌ MISSING | MUST PORT |
| Keyboard: C = Camera | Shortcut | SKIP | No camera in Desktop |

---

## SUMMARY - Items Requiring Action

### MUST PORT (11 items)

1. **Edit Profile Modal** - shared-ui.js:265-350 → Add to ProfileDropdown
2. **Clear Scan History** - shared-ui.js:867-869 → Add to ScanHistoryScreen
3. **Reset Settings to Default** - trust.js:296-316 → Add to TrustCentreScreen
4. **Security Audit Export** - trust.js:735-796 → Full audit format in export
5. **Game Stats Persistence** - game.js:694-705 → Persist to local file
6. **User Profile Persistence** - shared-ui.js:57-75 → Save/load profile
7. **Full User Data Export** - shared-ui.js:648-675 → Include all data
8. **Dynamic Export Preview** - export.js:245-297 → Live preview updates
9. **Keyboard: I = Import** - scanner shortcut
10. **Keyboard: G = Gallery** - scanner shortcut
11. **getScanById lookup** - For viewing specific history items

### DESKTOP ADAPT (3 items)

1. **Drag and Drop for Images** - scanner.js:549-575
2. **PDF Export** - Use desktop PDF library instead of browser print
3. **Share Report** - Use system share sheet if available

### SKIP (6 items)

1. Camera access (MediaDevices API)
2. Camera torch toggle
3. QR scanning from video frames
4. Mobile sidebar toggle
5. Service worker
6. Camera keyboard shortcut

---

## Next Steps

Process items one-by-one in the order listed above, following PARITY_LOG.md for tracking.
