# Desktop App UI/UX Audit Changelog

**Module**: `desktopApp`  
**Date**: December 26, 2025  
**Version**: 1.17.61  

---

## Summary

This audit focused on improving desktop UX conventions, specifically around cursor behavior for interactive elements. All changes maintain the existing visual design while enhancing the user experience.

---

## Changes by Category

### 🖱️ Cursor Helpers (Desktop UX)

**New Extension Functions** (`Interaction.kt`)
- Added `Modifier.handCursor()` - Applies pointer/hand cursor to clickable elements
- Added `Modifier.textCursor()` - Applies I-beam cursor for text fields
- Added `Modifier.clickableWithCursor()` - Convenience extension combining clickable + focusable + hand cursor

### 🎨 Screens Updated with Hand Cursor

| Screen | Elements Updated |
|--------|------------------|
| `AppSidebar.kt` | Sidebar items, profile card |
| `DashboardScreen.kt` | Header icons (dark mode, notifications, settings), view history link, training link, scan history rows |
| `LiveScanScreen.kt` | Notification bell, refresh button, view all link, export log button, scan action buttons, recent scan items |
| `ScanHistoryScreen.kt` | Notification button, settings button, export CSV button, filter chips, history rows |
| `TrustCentreAltScreen.kt` | Help button, profile button, toggle switches, language chips |
| `TrainingScreen.kt` | Enlarge button, skip round link, training action buttons |
| `ProfileDropdown.kt` | Menu items |

### 🧪 Test Fixes

**`AppViewModelTest.kt`**
- Fixed flaky `submitTrainingVerdict_updatesTrainingState` test
- Issue: Test assumed first scenario was always "AusPost phishing", but scenarios are shuffled
- Fix: Updated assertions to handle shuffled scenarios correctly

---

## Verification Results

### Build Status
```bash
./gradlew :desktopApp:compileKotlinDesktop
# BUILD SUCCESSFUL
```

### Test Status
```bash
./gradlew :desktopApp:desktopTest
# 5 tests completed, 0 failed
# BUILD SUCCESSFUL
```

### Deprecation Warnings (Known, Not Fixed)
- `painterResource()` deprecation in `ScanHistoryScreen.kt` and `TrainingScreen.kt`
- These require migration to Compose Resources library (out of scope for UI/UX audit)

---

## Files Modified

| File | Lines Changed | Change Type |
|------|---------------|-------------|
| `ui/Interaction.kt` | +35 | Added cursor helper extensions |
| `ui/AppSidebar.kt` | +2 | Applied handCursor |
| `ui/ProfileDropdown.kt` | +2 | Applied handCursor, added import |
| `screens/DashboardScreen.kt` | +8 | Applied handCursor to all clickables |
| `screens/LiveScanScreen.kt` | +8 | Applied handCursor to all clickables |
| `screens/ScanHistoryScreen.kt` | +7 | Applied handCursor to all clickables |
| `screens/TrustCentreAltScreen.kt` | +6 | Applied handCursor to all clickables |
| `screens/TrainingScreen.kt` | +4 | Applied handCursor to clickables |
| `desktopTest/.../AppViewModelTest.kt` | ~5 | Fixed flaky test for shuffled scenarios |

---

## Risks / Follow-ups

### Intentionally Unchanged

1. **`painterResource()` Deprecation**  
   - Files: `ScanHistoryScreen.kt`, `TrainingScreen.kt`
   - Reason: Requires migration to Compose Resources library, which is a larger refactor
   - Recommendation: Create a separate task for Compose Resources migration

2. **Focus Ring Styling**  
   - Current state: `.focusable()` is applied but no custom focus ring styling
   - Reason: Default focus styling is acceptable; custom styling would require design decisions
   - Recommendation: Coordinate with design team for custom focus indicators

3. **Keyboard Shortcuts Documentation**  
   - TrainingScreen has keyboard shortcuts (P, L, Enter, Escape) but they're only mentioned in-screen
   - Recommendation: Consider adding a help overlay or keyboard shortcut reference

4. **PointerIcon Types**  
   - Only `Hand` and `Text` cursors are currently used
   - Other cursor types (Wait, Crosshair, etc.) could be added for specific use cases

---

## Audit Checklist Status

### ✅ Completed
- [x] Cursor helpers created
- [x] Hand cursor applied to all clickable elements
- [x] Build passes without errors
- [x] All tests pass
- [x] No regressions introduced

### 📋 Desktop UX Conventions Verified
- [x] Window sizing: 1280x850 default, 1200x800 minimum
- [x] Resizable: true
- [x] Proper close behavior with ViewModel disposal
- [x] Pointer cursor on clickable elements
- [x] Focusable on interactive elements
- [x] Keyboard shortcuts in TrainingScreen (P, L, Enter, Escape)

### ⏳ Deferred (Out of Scope)
- [ ] Custom focus ring styling
- [ ] Compose Resources migration
- [ ] Keyboard shortcut overlay/help
