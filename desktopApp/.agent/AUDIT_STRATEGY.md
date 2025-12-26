# Desktop App Security & Production Readiness Audit Strategy

## 📋 Audit Scope

**Module:** `desktopApp`  
**Date:** 2025-12-26  
**Auditor:** Security Engineer + KMP Architect  
**Objective:** Identify and fix security vulnerabilities, UX/windowing issues, and code hygiene problems.

---

## 📁 File Groups for Audit

### Group A: Entry Points & Bootstrap (5 files)

| File | Purpose |
|------|---------|
| `Main.kt` | Application entry point, window configuration |
| `AppViewModel.kt` | Core state management, business logic |
| `DesktopSettingsStore.kt` | Settings persistence interface |
| `SettingsManager.kt` | Settings file I/O and serialization |
| `NavigationState.kt` | Navigation screen definitions |

#### Why This Group Matters
- Entry points control initial security posture (window settings, initialization)
- ViewModel handles sensitive operations (URL analysis, file I/O, clipboard access)
- Settings persistence may expose sensitive user data

#### Top Security Risks
1. **Hardcoded secrets** - API keys, tokens, or sensitive URLs in code
2. **Unsafe file I/O** - Path traversal in settings file handling
3. **Insecure defaults** - Telemetry enabled by default, weak security settings
4. **Exception swallowing** - `e.printStackTrace()` hiding security issues
5. **Clipboard exposure** - Sensitive data logged or mishandled

#### UX/Desktop Checks
1. Minimum window size enforced (already set: 1200x800)
2. Sensible default window position (centered)
3. Proper cleanup on dispose

#### Verification Commands
```bash
./gradlew :desktopApp:run
./gradlew :desktopApp:desktopTest
```

---

### Group B: UI Screens (11 files)

| File | Purpose |
|------|---------|
| `screens/DashboardScreen.kt` | Main dashboard with stats and quick actions |
| `screens/LiveScanScreen.kt` | Live QR scanning interface |
| `screens/ScanHistoryScreen.kt` | Scan history list and filtering |
| `screens/TrustCentreScreen.kt` | Security settings and trust management |
| `screens/TrustCentreAltScreen.kt` | Alternative trust centre view |
| `screens/TrainingScreen.kt` | "Beat the Bot" phishing training game |
| `screens/ReportsExportScreen.kt` | Report generation and export |
| `screens/ResultSafeScreen.kt` | Safe scan result display |
| `screens/ResultSuspiciousScreen.kt` | Suspicious scan result display |
| `screens/ResultDangerousScreen.kt` | Dangerous scan result display |
| `screens/ResultDangerousAltScreen.kt` | Alternative dangerous result view |

#### Why This Group Matters
- UI screens render user-provided data (URLs, scan results)
- Export screens handle file I/O
- Training game may show phishing examples (must be clear they're examples)

#### Top Security Risks
1. **XSS-like injection** - Unescaped URL display (less of an issue in Compose, but verify)
2. **Path traversal** - Export file name manipulation
3. **Information disclosure** - Sensitive data shown in UI accidentally
4. **Debug information leaks** - Technical details exposed to users

#### UX/Desktop Checks
1. Keyboard shortcuts (Esc to close dialogs)
2. Hand cursor on clickable elements
3. Proper focus management
4. Consistent theme usage

#### Verification Commands
```bash
./gradlew :desktopApp:run
# Manual: Navigate through all screens, verify Esc closes dialogs
```

---

### Group C: UI Components (6 files)

| File | Purpose |
|------|---------|
| `ui/AppSidebar.kt` | Main navigation sidebar |
| `ui/IconText.kt` | Icon rendering utilities |
| `ui/Interaction.kt` | Hover/press state helpers |
| `ui/NotificationPanel.kt` | Notification popup panel |
| `ui/ProfileDropdown.kt` | User profile dropdown |
| `ui/Patterns.kt` | Decorative modifier extensions |

#### Why This Group Matters
- Components are reused across the app
- Interaction helpers affect accessibility
- Notification panel may display sensitive scan results

#### Top Security Risks
1. **Sensitive data in notifications** - Full URLs or scan details visible
2. **Clickjacking potential** - Popup positioning issues

#### UX/Desktop Checks
1. Consistent hover states
2. Proper focus handling (focusable elements)
3. Keyboard navigation support

---

### Group D: Theme & Styling (1 file)

| File | Purpose |
|------|---------|
| `theme/StitchTheme.kt` | Design tokens and Material theme |

#### Why This Group Matters
- Theme consistency across the app
- Accessibility considerations (color contrast)

#### Security Risks
- None - pure styling

#### UX/Desktop Checks
1. Dark/light mode consistency
2. Color contrast for accessibility

---

### Group E: Internationalization (17 files)

| File | Purpose |
|------|---------|
| `i18n/DesktopStrings.kt` | Base localization framework |
| `i18n/DesktopStringsAr.kt` | Arabic translations |
| `i18n/DesktopStringsDe.kt` | German translations |
| `i18n/DesktopStringsEs.kt` | Spanish translations |
| `i18n/DesktopStringsFr.kt` | French translations |
| `i18n/DesktopStringsHi.kt` | Hindi translations |
| `i18n/DesktopStringsIn.kt` | Indonesian translations |
| `i18n/DesktopStringsIt.kt` | Italian translations |
| `i18n/DesktopStringsJa.kt` | Japanese translations |
| `i18n/DesktopStringsKo.kt` | Korean translations |
| `i18n/DesktopStringsPt.kt` | Portuguese translations |
| `i18n/DesktopStringsRu.kt` | Russian translations |
| `i18n/DesktopStringsTh.kt` | Thai translations |
| `i18n/DesktopStringsTr.kt` | Turkish translations |
| `i18n/DesktopStringsVi.kt` | Vietnamese translations |
| `i18n/DesktopStringsZh.kt` | Chinese translations |

#### Why This Group Matters
- Localization strings may contain format strings
- RTL language support (Arabic)

#### Security Risks
1. **Format string vulnerabilities** - Improper use of `String.format()`

#### UX/Desktop Checks
1. RTL layout support
2. String truncation handling

---

### Group F: Sample/Utility Data (1 file)

| File | Purpose |
|------|---------|
| `SampleData.kt` | Sample user profile data |

#### Security Risks
- May contain hardcoded sample data that looks like secrets

---

### Group G: Tests (1 file)

| File | Purpose |
|------|---------|
| `test/.../AppViewModelTest.kt` | ViewModel unit tests |

#### Why Tests Matter
- Tests may contain example URLs or test credentials
- Test infrastructure should not be in production builds

---

## 🔒 Logging Approach

The project does **NOT** currently use a logging framework. The existing approach:
- `e.printStackTrace()` for errors in `SettingsManager.kt`
- No debug logging elsewhere

**Proposal:** Keep logging minimal. Replace `e.printStackTrace()` with silent error handling (settings failures are non-critical). Do NOT add a logging dependency.

---

## 📋 Audit Execution Plan

### Batch 1: Entry Points (5 files)
1. `Main.kt` - Window config, minimum size
2. `AppViewModel.kt` - Core logic, secrets check
3. `DesktopSettingsStore.kt` - Settings interface
4. `SettingsManager.kt` - File I/O security
5. `NavigationState.kt` - Screen definitions

### Batch 2: Core Screens (4 files)
1. `DashboardScreen.kt`
2. `LiveScanScreen.kt`
3. `ScanHistoryScreen.kt`
4. `TrustCentreScreen.kt`

### Batch 3: Remaining Screens (7 files)
1. `TrustCentreAltScreen.kt`
2. `TrainingScreen.kt`
3. `ReportsExportScreen.kt`
4. `ResultSafeScreen.kt`
5. `ResultSuspiciousScreen.kt`
6. `ResultDangerousScreen.kt`
7. `ResultDangerousAltScreen.kt`

### Batch 4: UI Components & Theme (7 files)
1. `ui/AppSidebar.kt`
2. `ui/IconText.kt`
3. `ui/Interaction.kt`
4. `ui/NotificationPanel.kt`
5. `ui/ProfileDropdown.kt`
6. `ui/Patterns.kt`
7. `theme/StitchTheme.kt`

### Batch 5: i18n & Utilities (18 files)
1. `i18n/DesktopStrings.kt`
2. All language files (spot check)
3. `SampleData.kt`

### Batch 6: Tests
1. `AppViewModelTest.kt`

---

## ✅ Success Criteria

- [ ] No hardcoded secrets
- [ ] Safe file I/O (no path traversal)
- [ ] Input validation on all user inputs
- [ ] Sensible secure defaults
- [ ] Minimum window size enforced
- [ ] Esc closes dialogs
- [ ] Hand cursor on clickable elements
- [ ] No println/debug spam
- [ ] KDoc on complex functions
- [ ] All tests pass
- [ ] App launches and runs correctly
