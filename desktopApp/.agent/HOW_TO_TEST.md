# Desktop UI Layout Fixes - Manual Test Guide

## Version: 1.17.72
## Date: 2025-12-27

---

## Quick Start

```bash
cd /Users/raoof.r12/Desktop/Raouf/K/mehrguard
./gradlew :desktopApp:run
```

---

## Test Cases

### ✅ Task 1: Block Access Button - Full Text

**Screen:** Threat Analysis Report (scan a malicious URL)

**Steps:**
1. Scan a URL that triggers a "High Risk Detected" result
2. Look at the red "Block Access" button on the right

**Expected:**
- ✓ "Block Access" text is fully visible (not "Bloc...")
- ✓ Button width is 170dp
- ✓ Icon and text are horizontally aligned

---

### ✅ Task 2: Report Button - Full Text

**Screen:** Threat Analysis Report (same screen)

**Steps:**
1. Look at the "Report" button next to Block Access

**Expected:**
- ✓ "Report" text is fully visible (not "Repor...")
- ✓ Button width is 120dp
- ✓ Icon and text are horizontally aligned

---

### ✅ Task 3: Export CSV Button - Full Text

**Screen:** Scan History (click "History" in sidebar)

**Steps:**
1. Navigate to Scan History
2. Look at the blue button next to "Clear History"

**Expected:**
- ✓ "Export CSV" text is fully visible
- ✓ Download icon visible on the left
- ✓ Button width is 140dp

---

### ✅ Task 4: Domain Allowlist - Letter Avatar

**Screen:** Trust Centre (click "Trust Centre" in sidebar)

**Steps:**
1. Navigate to Trust Centre
2. Look at the Domain Allowlist section
3. Check domains like google.com, github.com

**Expected:**
- ✓ Each domain shows a letter avatar (e.g., "G" for google.com)
- ✓ NO broken/weird favicon icons
- ✓ Letters are in a rounded square box

---

## Window Size Tests

Test at these resolutions:
1. **Normal:** 1440×900
2. **Narrow:** 1024×768
3. **Maximized:** Full screen

All buttons should remain readable at all sizes.

---

## Build Verification

```bash
# Compile check
./gradlew :desktopApp:compileKotlinDesktop
# Expected: BUILD SUCCESSFUL

# Full run
./gradlew :desktopApp:run
# Expected: App launches without errors
```

---

## Files Changed

| File | Lines | Change |
|------|-------|--------|
| `ResultDangerousScreen.kt` | 160-240 | Layout restructure + button widths |
| `ScanHistoryScreen.kt` | 206-290 | Column weight + Export CSV sizing |
| `TrustCentreScreen.kt` | 572-608 | AllowItem letter avatar |
| `TrustCentreAltScreen.kt` | 136-161 | Removed profile/help icons |

---

## Summary of Button Widths

| Button | Screen | Width | Height | Font |
|--------|--------|-------|--------|------|
| Report | Threat Analysis | 120dp | 40dp | 12sp |
| Block Access | Threat Analysis | 170dp | 40dp | 12sp |
| Export CSV | Scan History | 140dp | 36dp | 12sp |
