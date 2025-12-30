# Agent Update Notes

This file tracks significant changes made during development sessions.

---

# 🤖 MESSAGES FOR ALL AGENTS - READ FIRST!

## ⚠️ CRITICAL: Version Management

**Current App Version: `1.20.4`** (as of December 30, 2025)

### 🔴 After Making ANY Improvements, YOU MUST Update Version Numbers:

| Platform | File | Field |
|----------|------|-------|
| **Android** | `androidApp/build.gradle.kts` | `versionCode` (increment by 1) + `versionName` |
| **iOS** | `iosApp/QRShield.xcodeproj/project.pbxproj` | `MARKETING_VERSION` (appears 2 times - update BOTH!) |
| **Desktop** | `desktopApp/.../screens/DashboardScreen.kt` | `KeyValueRow` version value (line ~520) |
| **CHANGELOG** | `CHANGELOG.md` | Add new entry at TOP with version number |

### Version Format
- Use semantic versioning: `MAJOR.MINOR.PATCH` (e.g., `1.17.24`)
- Increment PATCH for bug fixes
- Increment MINOR for new features  
- Increment MAJOR for breaking changes

---

## 🔴 MANDATORY: Documentation After EVERY Edit

> **After EVERY task and edit, you MUST update `agent.md` and `CHANGELOG.md` accordingly.**
> 
> This is NON-NEGOTIABLE. No exceptions. Your task is NOT complete until documentation is updated.

See the full workflow: `.agent/workflows/documentation.md` or use `/documentation`

---

## 🚨 MANDATORY: Error Resolution Protocol

> **If you encounter ANY error (build, test, runtime):**
> 1. **SEARCH THE INTERNET** immediately for the error message + current year. Data freshness is critical.
> 2. **FOLLOW** the workflow: `.agent/workflows/resolve-error.md` (Use `/resolve-error`)
> 3. **DOCUMENT** the fix in `agent.md` so the "collective memory" learns.

---

## 📋 Pre-Commit Checklist

Before finishing your session, ensure you complete ALL steps:

| # | Required | Task |
|---|----------|------|
| 1 | 🔴 **MANDATORY** | Update `agent.md` with session entry |
| 2 | 🔴 **MANDATORY** | Update `CHANGELOG.md` with changes |
| 3 | 🟡 If version bump | Update Android `build.gradle.kts` |
| 4 | 🟡 If version bump | Update iOS `project.pbxproj` (2 places!) |
| 5 | 🟡 If version bump | Update Desktop version in DashboardScreen |
| 6 | 🟢 If applicable | Run basic tests |
| 7 | 🟢 Final step | Commit and push changes |

---

## 🌍 Internationalization (i18n) Guidelines

This app supports **16 languages**. When adding new UI strings:

| Platform | How to Add Strings |
|----------|-------------------|
| **Android** | Add to `androidApp/src/main/res/values/strings.xml` AND all `values-<lang>/strings.xml` files |
| **iOS** | Add to `iosApp/QRShield/en.lproj/Localizable.strings` AND all other `.lproj` folders |
| **Desktop** | Add to `desktopApp/.../i18n/DesktopStrings.kt` AND all language-specific files |
| **Web** | Add to `webApp/.../i18n/WebStrings.kt` AND all language-specific files |

### Supported Languages
`en, ar, de, es, fr, hi, id, it, ja, ko, pt, ru, th, tr, vi, zh`

**⚠️ NEVER add hardcoded strings to UI - always use localization keys!**

---

## 🏗️ Project Architecture Quick Reference

```
qrshield/
├── androidApp/     # Android (Jetpack Compose)
├── iosApp/         # iOS (SwiftUI)
├── desktopApp/     # Desktop (Compose Desktop)
├── webApp/         # Web (Kotlin/JS + Kotlin/Wasm)
├── common/         # Shared KMP code (PhishingEngine, etc.)
├── models/         # ML models and brand database
├── data/           # Test data and update files
└── docs/           # Documentation
```

### Key Shared Components (in `common/`)
- `PhishingEngine` - Core threat detection logic
- `HeuristicAnalyzer` - URL analysis heuristics
- `BrandDatabase` - Known brand patterns
- All platforms share the same detection logic!

### Source Set Hierarchy (Kotlin 2.3.0)
```
common/src/
├── commonMain/     # Shared across ALL platforms
├── webMain/        # Shared between jsMain and wasmJsMain
├── jsMain/         # JavaScript-specific
├── wasmJsMain/     # WebAssembly-specific
├── androidMain/    # Android-specific
├── desktopMain/    # Desktop JVM-specific
└── iosMain/        # iOS-specific (shared by all iOS targets)
```

---

## 🔧 Common Tasks Quick Reference

### Building
```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS - use Xcode

# Desktop
./gradlew :desktopApp:run

# Web (JavaScript)
./gradlew :webApp:jsBrowserDevelopmentRun

# Web (WebAssembly) - NEW!
./gradlew :webApp:wasmJsBrowserDevelopmentWebpack
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :common:test
./gradlew :androidApp:testDebugUnitTest
```

---

## 📝 How to Document Your Session

Add a new section at the top of the session history below with:

```markdown
# 📱 [Date] (Session XX) - Brief Title

### Summary
One-line summary of what you did.

## ✅ Changes Made

### Files Updated
| File | Change |
|------|--------|
| `path/to/file` | Description of change |

## Notes
Any important notes for future agents.

---
```

---

## 🔧 Quick Tips

1. **🔴 Update docs after EVERY edit** - agent.md + CHANGELOG.md are MANDATORY
2. **Read CHANGELOG.md first** - Understand recent changes before making new ones
3. **Check for existing patterns** - Don't reinvent; follow existing code style
4. **Test on all platforms if possible** - Changes to `common/` affect everything
5. **Keep localization in sync** - If you add a string, add it to ALL 16 languages
6. **Update docs if you change architecture** - Keep `docs/` folder current

---

# SESSION HISTORY

---

# 📁 December 30, 2025 (Session 10k+58) - Documentation Merge & Cleanup

### Summary
Merged backup changelog, copied competition rules, and fixed incorrect agent attribution.

## ✅ Changes Made

| Task | Status | Details |
|------|--------|---------|
| **Copy Competition Rules** | ✅ | Copied to `docs/KOTLIN_CONTEST_OFFICIAL_RULES.txt` |
| **Fix Gemini→Raouf** | ✅ | 4 entries in backup fixed from "Gemini:" to "Raouf:" |
| **Merge Backup CHANGELOG** | ✅ | Merged 9857 lines (1.19.x through 1.0.0) into CHANGELOG.md |

## 📁 Files Modified

| File | Change |
|------|--------|
| `docs/KOTLIN_CONTEST_OFFICIAL_RULES.txt` | NEW - KotlinConf contest official rules |
| `CHANGELOG_backup_20251229.md` | Fixed "### Gemini:" to "### Raouf:" (4 occurrences) |
| `CHANGELOG.md` | Appended backup content (now 10,116 lines with complete history) |
| `.agent/agent.md` | Added this session entry |

## Notes
- All agent entries now correctly attributed to "Raouf:"
- CHANGELOG.md now contains complete version history from 1.20.15 back to 1.0.0
- Competition rules document saved for reference

---

# 🔍 December 30, 2025 (Session 10k+57) - Desktop App File-by-File Audit

### Summary
Comprehensive audit of Desktop app to verify all scan wiring is real (not decorative), identify issues, and create judge-proof demo input pack.

## ✅ Audit Results

| Category | Status | Evidence |
|----------|--------|----------|
| **Scan Wiring** | ✅ REAL | `AppViewModel.analyzeUrl()` → `PhishingEngine.analyze()` |
| **QR Decoding** | ✅ REAL | `DesktopQrScanner` uses ZXing `MultiFormatReader` |
| **History Recording** | ✅ REAL | `HistoryRepository` + SQLDelight persistence |
| **TODO/FIXME stubs** | ✅ NONE | 0 occurrences found |

## 📁 Files Audited

| File | Purpose | Issues Found |
|------|---------|---------------|
| `Main.kt` | Entry point, window config, keyboard shortcuts | ✅ None |
| `AppViewModel.kt` | State management, scan orchestration | ✅ None |
| `LiveScanScreen.kt` | QR scan UI, drag/drop, file picker | ✅ None |
| `ResultSafeScreen.kt` | Safe verdict display | ✅ None |
| `DesktopQrScanner.kt` | ZXing QR decode implementation | ✅ None |
| `PhishingEngine.kt` | Shared core detection logic | ✅ None |

## 🔗 Scan Pipeline Trace

```
1. User pastes URL (Cmd+V) or uploads image (Cmd+I)
2. AppViewModel.analyzeUrl(url, source) called
3. InputValidator.validateUrl() sanitizes input
4. DesktopScanState.Analyzing(url) - UI shows spinner
5. PhishingEngine.analyze(url) - REAL shared engine
   ├── HeuristicsEngine.analyze() - 25+ rules
   ├── BrandDetector.detect() - 500+ brands
   ├── TldScorer.score() - TLD risk
   └── EnsembleModel.predict() - ML inference
6. RiskAssessment returned with score, verdict, flags
7. VerdictEngine.enrich() adds explanation
8. Navigate to ResultSafe/Suspicious/Dangerous screen
9. HistoryManager.recordScan() persists to SQLDelight DB
10. Notification added to panel
```

## 📦 Judge Demo Input Pack Created

**File:** `desktopApp/.agent/JUDGE_DEMO_INPUT_PACK.md`

| Input | URL | Expected Verdict |
|-------|-----|------------------|
| 1 (Benign) | `https://www.google.com/search` | ✅ SAFE |
| 2 (Phishing) | `http://secure-login.paypa1-verify.com/update/credentials` | 🔴 MALICIOUS |
| 3 (Suspicious) | `https://bit.ly/secure-doc-verify` | ⚠️ SUSPICIOUS |

## ✅ Build Verification

```bash
./gradlew :desktopApp:compileKotlinDesktop :common:desktopTest
# BUILD SUCCESSFUL in 10s ✅
# 13 actionable tasks: 4 executed, 3 from cache, 6 up-to-date
```

---

# 🎨 December 30, 2025 (Session 10k+56) - Desktop App UI Polish

### Summary
Fixed 4 visual UI issues in the Desktop app: Threat Database button styling, empty square divider near filter buttons, search field polish, and added Reset button to Beat the Bot game.

... (rest of file preserved) ...

---

Raouf: 2025-12-30 16:38 AEDT

Scope: Update AGENT.md and repository changelog files to record the recent edits made by the agent.

Summary: Appended a Raouf template entry to AGENT.md and to the repository CHANGELOG.md under an "Unreleased" heading, noting date (Australia/Sydney), scope, files changed, verification steps performed, and follow-ups.

Files changed:
- AGENT.md
- CHANGELOG.md

Verification:
- Verified AGENT.md and CHANGELOG.md were updated and saved in repository path.

Follow-ups:
- None at this time.
