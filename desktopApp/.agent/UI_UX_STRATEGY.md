# Desktop App UI/UX Audit Strategy

**Module**: `desktopApp`  
**Date**: December 26, 2025  
**Auditor**: Senior UI/UX Engineer + KMP Architect  

---

## Overview

This document outlines the systematic UI/UX audit plan for the Mehr Guard desktop application built with Compose Desktop. The goal is production-readiness without redesigning or adding features.

---

## File Inventory (40 Kotlin Files)

### Group A: Entry Points + Window Shell (3 files)

| File | Purpose | Priority |
|------|---------|----------|
| `Main.kt` | Window creation, sizing, application lifecycle | HIGH |
| `AppViewModel.kt` | Central state, navigation, actions | HIGH (audit state handling) |
| `NavigationState.kt` | Screen enum definitions | LOW (review only) |

### Group B: Screens + Navigation (13 files)

| File | Purpose | Priority |
|------|---------|----------|
| `DashboardScreen.kt` | Main landing screen | HIGH |
| `LiveScanScreen.kt` | Active scanning interface | HIGH |
| `ScanHistoryScreen.kt` | Scan history list | MEDIUM |
| `ResultSafeScreen.kt` | Safe verdict result | MEDIUM |
| `ResultSuspiciousScreen.kt` | Suspicious verdict result | MEDIUM |
| `ResultDangerousScreen.kt` | Dangerous verdict result | MEDIUM |
| `ResultDangerousAltScreen.kt` | Alt dangerous result | MEDIUM |
| `TrainingScreen.kt` | Beat the Bot game | MEDIUM |
| `TrustCentreScreen.kt` | Settings panel | MEDIUM |
| `TrustCentreAltScreen.kt` | Alt settings panel | MEDIUM |
| `ReportsExportScreen.kt` | Export functionality | MEDIUM |

### Group C: Shared UI System (7 files)

| File | Purpose | Priority |
|------|---------|----------|
| `StitchTheme.kt` | Design tokens, colors, typography | HIGH |
| `AppSidebar.kt` | Navigation sidebar component | HIGH |
| `Patterns.kt` | Decorative modifier extensions | MEDIUM |
| `Interaction.kt` | Hover/press state helpers | MEDIUM |
| `IconText.kt` | Icon rendering utilities | LOW |
| `NotificationPanel.kt` | Notification popup | MEDIUM |
| `ProfileDropdown.kt` | User profile dropdown | MEDIUM |

### Group D: i18n (17 files) - OUT OF SCOPE

The 16 language files (`DesktopStrings*.kt`) are content-only and don't require UI/UX audit.

---

## Audit Rules by Group

### Group A: Entry Points + Window Shell

#### Consistency Checklist
- [ ] Window title follows brand standards
- [ ] Application icon properly loaded
- [ ] ViewModel properly scoped and disposed

#### Desktop UX Conventions
- [ ] **Window sizing**: Default 1280x850, minimum 1200x800 ✓ (already set)
- [ ] **Resizable**: true ✓ (already set)
- [ ] **Close behavior**: Proper cleanup on exit
- [ ] **Keyboard shortcuts at app level**: None required (Esc should close overlays, not app)
- [ ] **Single window instance**: No additional window spawning

#### Accessibility Checks
- [ ] Focus restoration after navigation
- [ ] No focus traps on startup

#### Performance Risks
- [ ] ViewModel singleton properly remembered
- [ ] No expensive operations in composition
- [ ] Proper coroutine scope management

#### Verification Commands
```bash
./gradlew :desktopApp:compileKotlinDesktop
./gradlew :desktopApp:run
```

---

### Group B: Screens + Navigation

#### Consistency Checklist
- [ ] All screens use `LocalStitchTokens.current` for colors
- [ ] Consistent header bar height across screens (64.dp standard)
- [ ] Consistent sidebar width (sidebar always present on main screens)
- [ ] Consistent padding: 32.dp outer, 24.dp card inner, 16.dp between sections
- [ ] Typography scale: 32sp page titles, 16sp section headers, 14sp body, 12sp captions
- [ ] Button styles: primary (filled), secondary (outline), tertiary (text-only)
- [ ] Card styling: 12dp radius, 1dp border, surface background
- [ ] Icon sizes: 18-24sp in cards, 16sp in buttons, 20sp in header icons
- [ ] Empty states have helpful message + optional action
- [ ] Error states are actionable

#### Desktop UX Conventions
- [ ] **Pointer cursor**: Hand on clickable, text cursor on text fields
- [ ] **Esc key**: Closes overlays/modals (not the app)
- [ ] **Focus order**: Tab navigates logically (nav → content → actions)
- [ ] **Scroll behavior**: Vertical scroll with no nested scroll traps
- [ ] **Clickable areas**: Sufficient hover targets (min 32x32dp)
- [ ] **Focusable on clickable**: All `.clickable()` have `.focusable()`

#### Accessibility Checks
- [ ] All buttons have sufficient contrast
- [ ] Status indicated by icon + color + text (not color alone)
- [ ] Minimum text size 12sp (readable at desktop distance)
- [ ] Interactive elements have semantic descriptions where needed

#### Performance Risks
- [ ] No recreating lambdas in loops
- [ ] Use `remember` for expensive calculations
- [ ] LazyColumn/LazyRow for long lists
- [ ] Avoid recomposition-triggering state reads in draw phases

#### Verification Commands
```bash
./gradlew :desktopApp:run
# Manual: Navigate through all screens
# Manual: Test Esc key on overlays
# Manual: Tab through interactive elements
```

---

### Group C: Shared UI System

#### Consistency Checklist
- [ ] Design tokens are used consistently (not hardcoded values)
- [ ] Modifier helpers use correct default values
- [ ] Hover states are consistent (same color/timing)
- [ ] Icon font selection is consistent

#### Desktop UX Conventions
- [ ] Sidebar highlights current screen
- [ ] Sidebar items have hover states
- [ ] Profile dropdown closes on outside click or Esc
- [ ] Notification panel closes on outside click or Esc

#### Accessibility Checks
- [ ] Sidebar items are keyboard navigable
- [ ] Popups are dismissible via keyboard

#### Performance Risks
- [ ] No allocations in modifier extensions
- [ ] InteractionSource properly remembered
- [ ] Theme CompositionLocal is stable

#### Verification Commands
```bash
./gradlew :desktopApp:compileKotlinDesktop
./gradlew :desktopApp:desktopTest
```

---

## Execution Plan (Batches)

### Batch 1: Window Shell + Core Components (4 files)
- `Main.kt` - Window config, keyboard handling at app level
- `AppViewModel.kt` - State management review (read-only unless issues found)
- `StitchTheme.kt` - Design token consistency
- `AppSidebar.kt` - Navigation + hover states

**Success Criteria**: Build passes, app launches, sidebar nav works

### Batch 2: Dashboard + Scan Flow (3 files)
- `DashboardScreen.kt` - Landing page UX
- `LiveScanScreen.kt` - Scanner UX
- `NotificationPanel.kt` - Popup behavior (Esc key, outside click)

**Success Criteria**: Build passes, can navigate Dashboard → Scan

### Batch 3: Result Screens (4 files)
- `ResultSafeScreen.kt`
- `ResultSuspiciousScreen.kt`
- `ResultDangerousScreen.kt`
- `ResultDangerousAltScreen.kt`

**Success Criteria**: Build passes, result screens display correctly

### Batch 4: History + Settings (4 files)
- `ScanHistoryScreen.kt`
- `TrustCentreScreen.kt`
- `TrustCentreAltScreen.kt`
- `ProfileDropdown.kt`

**Success Criteria**: Build passes, history shows, settings work

### Batch 5: Training + Export (3 files)
- `TrainingScreen.kt`
- `ReportsExportScreen.kt`
- Remaining review of `Patterns.kt`, `Interaction.kt`, `IconText.kt`

**Success Criteria**: Build passes, all screens functional

---

## Non-Goals (Explicit Exclusions)

1. **No new features** - Only polish existing functionality
2. **No redesign** - Keep current visual language
3. **No new dependencies** - Unless absolutely blocked
4. **No branding changes** - Keep colors/logos as-is
5. **No i18n content changes** - Only translation key fixes if broken
6. **No performance optimization beyond obvious issues**

---

## Deliverables

1. **Code changes** - Incremental commits with PR-style descriptions
2. **UI_UX_CHANGELOG.md** - All changes documented with categories
3. **Verification results** - Pass/fail for each batch

---

## Risk Tolerance

| Risk Level | Action |
|------------|--------|
| **Obvious bug** | Fix immediately |
| **Minor inconsistency** | Fix if low-risk |
| **Major refactor needed** | Document in "Follow-ups", don't change |
| **Unclear intention** | Leave as-is, document |
