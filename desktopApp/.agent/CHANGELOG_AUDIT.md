# Desktop App Security Audit Changelog

## [1.0.1] - 2025-12-26

### 🔒 Security Fixes

**Removed stack trace logging** (`SettingsManager.kt`)
- Replaced `e.printStackTrace()` calls with silent error handling
- Settings persistence is non-critical; errors now silently fallback to defaults
- This prevents potential information leakage through console output
- Added comprehensive KDoc documenting the security-conscious design

```diff
- } catch (e: Exception) {
-     e.printStackTrace()
- }
+ } catch (_: Exception) {
+     // Settings persistence is non-critical; return defaults on any error
+ }
```

**Verified secure defaults**
- `offlineOnlyEnabled = true` - Network access disabled by default
- `telemetryEnabled = false` - No telemetry by default for privacy
- `autoCopySafeLinksEnabled = false` - User must explicitly enable clipboard auto-copy

**Verified no hardcoded secrets**
- Scanned all 50+ Kotlin files for API keys, tokens, passwords
- Only match was test data URL (`?token=123` in test assertions) - not a secret
- All "token" references are design tokens (ColorTokens, DesignTokens)

---

### 🛠️ Code Hygiene / Deprecation Fixes

**Fixed deprecated Material Icons** (`IconText.kt`)
- Migrated 9 icons to AutoMirrored versions for RTL language support:
  - `AltRoute`, `ArrowForward`, `CallSplit`, `FactCheck`, `HelpOutline`
  - `Logout`, `ManageSearch`, `OpenInNew`, `Rule`

```diff
- "call_split" -> Icons.Rounded.CallSplit
+ "call_split" -> Icons.AutoMirrored.Rounded.CallSplit
```

**Fixed deprecated Divider component** (`ProfileDropdown.kt`)
- Replaced deprecated `Divider` with `HorizontalDivider` per Material 3 API

```diff
- import androidx.compose.material3.Divider
+ import androidx.compose.material3.HorizontalDivider

- Divider(color = colors.border, thickness = 1.dp)
+ HorizontalDivider(color = colors.border, thickness = 1.dp)
```

---

### 📝 Documentation Improvements

**Added KDoc to SettingsManager** (`SettingsManager.kt`)
- Documented security design rationale
- Explained platform-specific storage locations
- Documented Settings data class properties
- Explained error handling philosophy

---

### ✅ Test Fixes

**Fixed outdated training test** (`AppViewModelTest.kt`)
- Test `submitTrainingVerdict_updatesTrainingState` was outdated
- Game flow changed: submit shows modal, dismiss advances round
- Updated test to match actual two-step process

```diff
- assertEquals(initial.round + 1, updated.round) // Was failing
+ assertEquals(initial.round, updated.round) // Round doesn't advance until modal dismissed
+ viewModel.dismissTrainingResultModal()
+ assertEquals(initial.round + 1, updated.round) // Now correctly tests both steps
```

---

### ✅ Verification Results

| Check | Status |
|-------|--------|
| Build: `./gradlew :desktopApp:compileKotlinDesktop` | ✅ PASS |
| Tests: `./gradlew :desktopApp:desktopTest` | ✅ PASS (5/5) |
| No deprecation warnings | ✅ PASS |
| No println/debug spam | ✅ PASS |
| No hardcoded secrets | ✅ PASS |

---

### 🎯 UX/Desktop Verification

| Check | Status | Notes |
|-------|--------|-------|
| Minimum window size | ✅ | 1200x800 enforced via `window.minimumSize` |
| Default window centered | ✅ | `WindowPosition(Alignment.Center)` |
| Default window size | ✅ | 1280x850 (sensible for desktop) |
| Window resizable | ✅ | `resizable = true` |
| Clickable elements focusable | ✅ | All clickable elements have `.focusable()` |
| Esc closes dialogs | ⚠️ | Popups use `focusable = true` for dismiss |

---

### 📋 Risks / Follow-ups

**Not Changed (By Design)**

1. **Sample/demo data remains** (`SampleData.kt`, sample notifications)
   - User profile shows "Security Analyst" / "Offline Operations" 
   - Sample notifications demonstrate functionality
   - Intentional for demo/judging purposes

2. **Hardcoded version strings** (ReportsExportScreen)
   - "ENGINE v2.4", "v1.17.30", "4,281,092 Signatures"
   - These are demo placeholders for the preview

3. **Moscow/Russia geo in export preview** (ReportsExportScreen)
   - Static demo data for the PDF preview feature
   - Not configurable - purely cosmetic example

4. **No formal logging framework**
   - Project intentionally avoids adding dependencies
   - Silent error handling preferred over console spam

**Potential Future Improvements**

1. Add keyboard shortcut for Esc to return to Dashboard from any screen
2. Consider adding hand cursor on hover for clickable elements
3. Could add Ctrl+S shortcut on export screen to trigger export
