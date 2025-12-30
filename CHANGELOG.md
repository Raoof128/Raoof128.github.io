# Changelog

## Unreleased

---

All notable changes to QR-SHIELD will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [1.20.11] - 2025-12-30

### Raouf: Hotkey Fix & Version Sync (2025-12-30 17:06 AEDT)

**Scope:** Fix keyboard hotkeys interfering with text input, sync hardcoded versions

**Issues Fixed:**

| # | Issue | Fix |
|---|-------|-----|
| 1 | **G key hijacking URL input** | Changed letter shortcuts (S, D, H, T, G) to require Cmd/Ctrl modifier |
| 2 | **Version mismatch** | Updated desktopApp packageVersion from 1.0.0 to 1.2.0 (matches project) |

**Files Modified:**

| File | Change |
|------|--------|
| `Main.kt` | Removed simple letter shortcuts; now require Cmd/Ctrl+S/D/H/T/G for navigation |
| `desktopApp/build.gradle.kts` | Updated packageVersion: 1.0.0 → 1.2.0 |

**Version Cross-Check Summary:**

| Location | Version | Status |
|----------|---------|--------|
| Root build.gradle.kts | 1.2.0 | ✅ Project version |
| desktopApp/build.gradle.kts | 1.2.0 | ✅ Fixed |
| SecureBundleLoader.BUILTIN_VERSION | 2025.12.29 | ✅ Threat DB |
| ThreatIntelLookup default | 2025.12.29 | ✅ Matches |
| DashboardScreen.kt | 2025.12.29 | ✅ Matches |

**Build Verification:**
```bash
./gradlew :desktopApp:compileKotlinDesktop
# BUILD SUCCESSFUL ✅
```

---

## [1.20.10] - 2025-12-30

### Raouf: Desktop App UI Polish - Round 5 (2025-12-30 16:35 AEDT)

**Scope:** Fix Enter key in URL input, remove Sandbox button from yellow result page

**Issues Fixed:**

| # | Issue | Fix |
|---|-------|-----|
| 1 | **Enter key in URL input** | Changed from onKeyEvent to KeyboardActions (proper text field approach) |
| 2 | **Remove Sandbox button** | Removed from ResultSuspiciousScreen (yellow/warning result page) |

**Files Modified:**

| File | Change |
|------|--------|
| `DashboardScreen.kt` | Used KeyboardActions with onGo for Enter key handling |
| `ResultSuspiciousScreen.kt` | Removed "Open in Sandbox" button |

**Build Verification:**
```bash
./gradlew :desktopApp:compileKotlinDesktop
# BUILD SUCCESSFUL ✅
```

---

## [1.20.9] - 2025-12-30

### Raouf: Desktop App UI Polish - Round 4 (2025-12-30 16:26 AEDT)

**Scope:** Add Help button to sidebar, fix Export CSV popup, wire up onHelpClick across all screens

... (remaining changelog content unchanged) ...
