# Changelog

## Unreleased

- Raouf: 2025-12-30 16:38 AEDT — Scope: Update AGENT.md and repository changelog files.

  Summary: Appended Raouf template entries to AGENT.md and this CHANGELOG.md to record the change. Files changed: AGENT.md, CHANGELOG.md. Verification: Files updated and saved in repository on 2025-12-30 16:38 AEDT (Australia/Sydney).

---

All notable changes to QR-SHIELD will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

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
