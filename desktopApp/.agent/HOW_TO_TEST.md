# Desktop UI Polish - Manual Test Guide

## Quick Start

```bash
cd /Users/raoof.r12/Desktop/Raouf/K/qrshield
./gradlew :desktopApp:run
```

---

## Test Cases

### ✅ Task 1: Paranoia Icon Appears

**Screen:** Trust Centre (click "Trust Centre" in sidebar)

**Steps:**
1. Navigate to Trust Centre
2. Locate the "Heuristic Sensitivity" section
3. Look at the three option tiles: Low, Balanced, Paranoia

**Expected:**
- ✓ All 3 tiles have visible icons in circular containers
- ✓ Low: shield icon (green when selected)
- ✓ Balanced: verified_user icon (blue when selected)
- ✓ Paranoia: warning icon (orange when selected)
- ✓ Icons are same size (32dp circles)
- ✓ Clicking any tile updates the selection and mode badge

---

### ✅ Task 2: Toggle Cards Equal Sizes

**Screen:** Trust Centre

**Steps:**
1. Navigate to Trust Centre
2. Scroll to view the three toggle cards:
   - "Strict Offline Mode"
   - "Anonymous Telemetry"
   - "Auto-copy Safe Links"

**Expected:**
- ✓ All three cards have identical height (72dp)
- ✓ All cards have same padding and border styling
- ✓ Toggle switches are same size (52×28dp track, 22dp thumb)
- ✓ Pointer changes to hand cursor when hovering
- ✓ Clicking toggles the switch state

---

### ✅ Task 3: View Audit Log Button Position

**Screen:** Trust Centre

**Steps:**
1. Navigate to Trust Centre
2. Locate the "Strict Offline Guarantee" card at the top
3. Look at the "View Audit Log" button position

**Expected:**
- ✓ Button is in the top-right corner of the card header area
- ✓ Button is NOT vertically centered mid-card
- ✓ Status badge ("AIR-GAPPED STATUS: ACTIVE") is on the left
- ✓ Title and description text are below the header row
- ✓ Pointer changes to hand cursor when hovering button
- ✓ Clicking button navigates to Reports/Export screen

---

### ✅ Task 4: Export CSV Horizontal

**Screen:** Scan History (click "History" in sidebar)

**Steps:**
1. Navigate to Scan History
2. Look at the header controls row containing:
   - "Last 7 Days" filter
   - "Clear History" button
   - "Export CSV" button

**Expected:**
- ✓ "Export CSV" button text renders horizontally (left-to-right)
- ✓ Text is NOT vertical
- ✓ Icon (download) appears to the left of text
- ✓ Button has blue primary background
- ✓ No text clipping or overflow

---

### ✅ Task 5: Upload QR Code Panel Alignment

**Screen:** Scan Monitor (click "Scan Monitor" in sidebar)

**Steps:**
1. Navigate to Scan Monitor
2. Look at the "Upload QR Code" panel in the center

**Expected:**
- ✓ Upload icon is centered horizontally
- ✓ Title text ("Upload QR Code") is centered
- ✓ Description text is centered
- ✓ "Upload Image" button is centered
- ✓ All elements align to same vertical axis
- ✓ Spacing is consistent between elements

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

| File | Lines | Task |
|------|-------|------|
| `TrustCentreScreen.kt` | 131-186, 281, 371-388 | 1, 2, 3 |
| `ScanHistoryScreen.kt` | 255-282 | 4 |
| `LiveScanScreen.kt` | 300-346 | 5 |

---

## Version

- **App Version:** 1.17.69
- **Date:** 2025-12-27
