# Mehr Guard iOS Surface Area Map

**Generated:** December 21, 2025  
**Build Status:** ✅ Successful (iPhone 17 Simulator, iOS 26.0)  
**Minimum iOS Version:** 17.0

---

## 1. Project Structure Overview

```
iosApp/
├── MehrGuard.xcodeproj/          # Xcode project
├── MehrGuard/                     # Main app source
│   ├── App/                      # App entry point
│   │   └── MehrGuardApp.swift     # @main SwiftUI App
│   ├── Models/                   # Data models & services
│   │   ├── HistoryStore.swift    # Scan history persistence
│   │   ├── KMPBridge.swift       # Kotlin Multiplatform bridge
│   │   ├── MockTypes.swift       # Mock types for UI development
│   │   ├── SettingsManager.swift # Centralized settings
│   │   └── UnifiedAnalysisService.swift # Dual-engine analysis
│   ├── Extensions/               # SwiftUI extensions
│   │   ├── Assets+Extension.swift # Image & VerdictIcon components
│   │   └── Color+Theme.swift     # Design system colors & Liquid Glass
│   ├── UI/                       # All UI views
│   │   ├── Components/           # Reusable components
│   │   ├── Dashboard/            # Main dashboard
│   │   ├── History/              # History & threat history
│   │   ├── Navigation/           # Menu navigation
│   │   ├── Onboarding/           # First-run experience
│   │   ├── Results/              # Scan result display
│   │   ├── Scanner/              # QR code scanner
│   │   ├── Settings/             # App settings
│   │   ├── Training/             # Beat the Bot game
│   │   ├── Trust/                # Trust Centre
│   │   └── Export/               # Report export
│   ├── Info.plist                # App configuration
│   └── Assets.xcassets/          # Image assets
├── MehrGuardWidget/               # iOS widget (optional)
├── MehrGuardUITests/              # UI tests
├── Frameworks/                   # Framework dependencies
└── scripts/                      # Build scripts
```

---

## 2. Entry Point

### `MehrGuardApp.swift`

```swift
@main
struct MehrGuardApp: App {
    @State private var hasCompletedOnboarding = UserDefaults.standard.bool(forKey: "hasCompletedOnboarding")
    @Environment(\.scenePhase) private var scenePhase
    @AppStorage("useDarkMode") private var useDarkMode = true
    @State private var shouldOpenScanner = false
    
    var body: some Scene {
        WindowGroup {
            if hasCompletedOnboarding {
                ContentView(shouldOpenScanner: $shouldOpenScanner)
            } else {
                OnboardingView(isComplete: $hasCompletedOnboarding)
            }
        }
        .onChange(of: scenePhase) { ... }  // Lifecycle handling
        .onOpenURL { url in handleDeepLink(url) }  // Deep links
    }
}
```

**Feature Checklist:**
- ✅ Onboarding flow management
- ✅ Dark/Light mode switching
- ✅ Deep link handling (`mehrguard://scan`)
- ✅ Scene phase lifecycle management
- ✅ Navigation bar & tab bar Liquid Glass configuration

---

## 3. Navigation Structure

### Primary Navigation: TabView (`ContentView`)

| Tab | View | Icon | Description |
|-----|------|------|-------------|
| 1 | `DashboardView` | `square.grid.2x2.fill` | Home dashboard with stats |
| 2 | `ScannerView` | `qrcode.viewfinder` | Camera-based QR scanning |
| 3 | `HistoryView` | `clock.fill` | Scan history list |
| 4 | `BeatTheBotView` | `gamecontroller.fill` | Phishing training game |
| 5 | `SettingsView` | `gearshape.fill` | App settings |

### Secondary Navigation: Sheets

| Sheet | Trigger | Purpose |
|-------|---------|---------|
| `MainMenuView` | Menu button | Full app navigation |
| `TrustCentreView` | Settings/Dashboard | Privacy & security settings |
| `ReportExportView` | Export buttons | Generate threat reports |
| `ScanResultView` | After analysis | Full result details |
| `ThreatHistoryView` | Settings/Dashboard | Live threat monitor |
| `HistoryDetailSheet` | History item tap | Individual scan details |
| `SandboxPreviewSheet` | "Quarantine" action | Safe URL preview |

### Deep Linking

```swift
// Supported URL schemes
mehrguard://scan  → Opens Scanner tab
```

---

## 4. State Management

### Pattern: SwiftUI Native + Observable

| Level | Pattern | Usage |
|-------|---------|-------|
| View-local | `@State` | UI toggles, form inputs |
| Persistence | `@AppStorage` | User preferences (UserDefaults) |
| Shared State | `@Observable` (iOS 17+) | ViewModels, singletons |
| Legacy | `ObservableObject` | KMPBridge compatibility |

### Key Singletons

| Singleton | Type | Purpose |
|-----------|------|---------|
| `HistoryStore.shared` | `@Observable` | Scan history persistence |
| `SettingsManager.shared` | `@Observable` | Centralized settings access |
| `ScannerViewModel.shared` | `@Observable` | Camera & scanning state |
| `UnifiedAnalysisService.shared` | `ObservableObject` | Dual-engine analysis |

### Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        MehrGuardApp                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    ContentView (TabView)                  │   │
│  │  ┌─────────┐ ┌───────────┐ ┌──────────┐ ┌─────────────┐  │   │
│  │  │Dashboard│ │  Scanner  │ │ History  │ │  Settings   │  │   │
│  │  └────┬────┘ └─────┬─────┘ └────┬─────┘ └──────┬──────┘  │   │
│  │       │            │            │              │          │   │
│  └───────┼────────────┼────────────┼──────────────┼──────────┘   │
│          │            │            │              │              │
│          ▼            ▼            ▼              ▼              │
│    ┌─────────────────────────────────────────────────────────┐   │
│    │              UnifiedAnalysisService.shared              │   │
│    │    ┌──────────────────┐    ┌──────────────────────┐     │   │
│    │    │   KMP Engine     │ OR │   Swift Fallback     │     │   │
│    │    │ (HeuristicsEngine)    │  (URL Heuristics)    │     │   │
│    │    └──────────────────┘    └──────────────────────┘     │   │
│    └─────────────────────────────────────────────────────────┘   │
│                              │                                    │
│                              ▼                                    │
│    ┌─────────────────────────────────────────────────────────┐   │
│    │                  HistoryStore.shared                     │   │
│    │                (UserDefaults persistence)                │   │
│    └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. KMP Bridging Boundary

### Location: `KMPBridge.swift` & `UnifiedAnalysisService.swift`

### Conditional Compilation Strategy

```swift
#if canImport(common)
import common

// Use real KMP HeuristicsEngine
let result = engine.analyze(url: url)
return RiskAssessmentMock.from(result)

#else
// Fallback: Show error state or use Swift heuristics
return errorResult() // or analyzeWithSwift(url)
#endif
```

### KMP Integration Points

| Swift File | KMP Dependency | Status |
|------------|----------------|--------|
| `KMPBridge.swift` | `HeuristicsEngine` | Conditional |
| `UnifiedAnalysisService.swift` | `HeuristicsEngine` | Conditional + Swift fallback |
| `ScannerViewModel.swift` | `PhishingEngine` | Conditional |
| `MockTypes.swift` | `common.Verdict` / `common.RiskAssessment` | Type bridge |

### `UnifiedAnalysisService` Features

1. **Dual-Engine Architecture:**
   - Prefers KMP `HeuristicsEngine` when `common.framework` is linked
   - Falls back to comprehensive Swift heuristics otherwise

2. **Swift Fallback Heuristics (25+ checks):**
   - Trusted domain whitelist
   - Suspicious keyword detection
   - Homograph/IDN attack detection
   - High-risk TLD identification
   - Brand impersonation detection
   - IP address in URL detection
   - Excessive hyphen detection
   - @ symbol attack detection
   - Typosquatting detection

3. **Engine State Tracking:**
   - `isKMPAvailable: Bool`
   - `lastEngineUsed: String`

---

## 6. Core Views Breakdown

### 6.1 DashboardView

**File:** `UI/Dashboard/DashboardView.swift` (800 lines)

**Features:**
- URL input field with "Analyze" button
- System health indicator
- Statistics cards (threats blocked, safe scans)
- Engine features grid
- Recent scans list
- Threat database status

**State:**
- `@State urlInput`, `isAnalyzing`, `showScanner`, `showImagePicker`
- `@AppStorage useDarkMode`

**Issue Identified:** Contains duplicate `analyzeURL()` logic that should use `UnifiedAnalysisService`.

---

### 6.2 ScannerView & ScannerViewModel

**Files:**
- `UI/Scanner/ScannerView.swift` (616 lines)
- `UI/Scanner/ScannerViewModel.swift` (642 lines)

**Features:**
- AVFoundation camera integration
- Real-time QR code detection
- Flash toggle
- Gallery image import
- Permission handling
- Result card display

**ViewModel Pattern:** `@Observable` singleton with camera session management

**Issue Identified:** `ScannerViewModel.analyzeUrl()` has direct KMP call; should use `UnifiedAnalysisService`.

---

### 6.3 HistoryView & HistoryViewModel

**File:** `UI/History/HistoryView.swift` (581 lines)

**Features:**
- Searchable history list
- Filter by verdict (All/Safe/Suspicious/Malicious)
- Sort by date or risk
- Export to clipboard
- Context menu (copy, share, delete)
- Clear all confirmation

**Data Source:** `HistoryStore.shared`

---

### 6.4 SettingsView

**File:** `UI/Settings/SettingsView.swift` (509 lines)

**Features:**
- Haptic feedback toggle
- Sound effects toggle
- Auto-scan toggle
- Save history toggle
- Liquid Glass reduced motion toggle
- Notifications toggle
- Dark mode toggle
- Clear history action
- Quick actions (Threat Monitor, Trust Centre, Export)

---

### 6.5 BeatTheBotView (Training)

**File:** `UI/Training/BeatTheBotView.swift` (755 lines)

**Features:**
- Phishing/legitimate URL game
- Timer-based challenges
- Points, streak, accuracy tracking
- Live hints
- Bot confidence indicator
- Difficulty levels (Beginner → Nightmare)

**Sample Challenges:** 18 phishing/legitimate examples built-in

---

### 6.6 TrustCentreView

**File:** `UI/Trust/TrustCentreView.swift` (767 lines)

**Features:**
- Offline guarantee banner
- Threat sensitivity slider (Low/Balanced/Paranoia)
- Privacy controls (Strict Offline, Telemetry, Auto-Copy)
- Trusted domains list (editable)
- Blocked domains list (editable)
- Privacy Policy, Licenses, Acknowledgements sheets
- Reset to defaults

---

### 6.7 ScanResultView

**File:** `UI/Results/ScanResultView.swift` (892 lines)

**Features:**
- Verdict hero section with confidence badge
- Threat tags (FlowLayout)
- Recommended actions (Block & Report, Quarantine)
- Attack breakdown (expandable cards)
- Explainable Security panel
- Scan metadata (ID, engine, latency)
- Share report
- Sandbox preview sheet

**Security Note:** "Quarantine in Sandbox" opens `SandboxPreviewSheet` for safe URL preview—**does not auto-open URLs**.

---

## 7. Design System

### Color Palette (Color+Theme.swift)

| Token | Light Mode | Dark Mode | Purpose |
|-------|------------|-----------|---------|
| `brandPrimary` | `#2563EB` | `#2563EB` | Royal Blue |
| `brandSecondary` | `#10B981` | `#10B981` | Emerald/Teal |
| `brandAccent` | `#8B5CF6` | `#8B5CF6` | Violet |
| `verdictSafe` | `#34C759` | `#34C759` | iOS Green |
| `verdictWarning` | `#FF9500` | `#FF9500` | iOS Orange |
| `verdictDanger` | `#FF3B30` | `#FF3B30` | iOS Red |
| `bgMain` | `#F2F2F7` | `#0B1120` | Background |
| `bgCard` | White (0.7) | `#1E293B` (0.7) | Card surfaces |

### Liquid Glass Modifier

```swift
.liquidGlass(cornerRadius: 16, opacity: 1.0)
```

- Uses `.ultraThinMaterial` (dark) / `.regularMaterial` (light)
- Gradient border stroke
- Adaptive shadow

### Typography

- SF Pro (system default)
- Title: `.title.weight(.bold)`
- Headline: `.headline`
- Body: `.body`
- Caption: `.caption`, `.caption2`

---

## 8. Issues Resolved (December 21, 2025)

### ✅ Critical Issues - FIXED

| Issue | Location | Solution |
|-------|----------|----------|
| ~~Duplicate analysis logic~~ | `DashboardView.analyzeURL()` | ✅ FIXED - Now uses `UnifiedAnalysisService.shared.analyze()` |
| ~~Direct KMP call~~ | `ScannerViewModel.analyzeUrl()` | ✅ FIXED - Now uses `UnifiedAnalysisService.shared.analyze()` |
| ~~Clipboard validation~~ | `MainMenuView` "Paste URL" | ✅ FIXED - Added URL scheme and host validation |
| ~~UnifiedAnalysisService not in project~~ | Xcode project | ✅ FIXED - Added to project.pbxproj |

### 🟢 Minor (Pending)

| Issue | Location | Recommendation |
|-------|----------|----------------|
| Hard-coded version strings | Multiple views | Move to a single `AppConstants` file |
| Missing accessibility labels | Some UI components | Add `accessibilityLabel` to all interactive elements |

---

## 9. Security Checklist

| Rule | Status | Evidence |
|------|--------|----------|
| Never auto-open unknown URLs | ✅ | `SandboxPreviewSheet` shows URL breakdown; no auto-open |
| "Open safely" requires confirmation | ✅ | `ScanResultView` uses confirmation dialog |
| Clipboard treated as untrusted | ⚠️ | `MainMenuView` pastes directly to dashboard—needs validation |
| No sensitive data in logs | ✅ | `#if DEBUG` guards on print statements |
| Offline analysis guarantee | ✅ | `UnifiedAnalysisService` works without network |

---

## 10. File-by-File Interactive Element Map

### UI Element → Function Mapping

| View | Element | Action | Function Called |
|------|---------|--------|-----------------|
| `DashboardView` | "Analyze" button | Analyze URL | `analyzeURL()` ⚠️ |
| `DashboardView` | "Scan QR Code" | Open scanner | `showScanner = true` ✅ |
| `DashboardView` | "Import Image" | Open gallery | `showImagePicker = true` ✅ |
| `ScannerView` | Flash toggle | Toggle flash | `viewModel.toggleFlash()` ✅ |
| `ScannerView` | Gallery button | Import image | `showGalleryPicker = true` ✅ |
| `ScannerView` | Main scan button | Toggle scanning | `viewModel.toggleScanning()` ✅ |
| `HistoryView` | History row | Show detail | `selectedItem = item` ✅ |
| `HistoryView` | Delete action | Delete item | `viewModel.delete()` → `HistoryStore.shared.delete()` ✅ |
| `HistoryView` | Clear All | Clear history | `HistoryStore.shared.clearAll()` ✅ |
| `SettingsView` | All toggles | Persist setting | `@AppStorage` + `SettingsManager` ✅ |
| `TrustCentreView` | Sensitivity slider | Update sensitivity | `@AppStorage threatSensitivity` ✅ |
| `TrustCentreView` | Trusted Domains | Edit list | `DomainListSheet` → JSON persistence ✅ |
| `BeatTheBotView` | Decision buttons | Submit answer | `makeDecision(isPhishing:)` ✅ |
| `ScanResultView` | "Block & Report" | Block URL | `blockAndReport()` ✅ |
| `ScanResultView` | "Quarantine" | Preview URL | `showSandbox = true` ✅ |

---

## 11. Session Summary (December 21, 2025)

### ✅ Completed

1. **✅ Refactored `DashboardView`** - Uses `UnifiedAnalysisService.shared.analyze(url:)`
2. **✅ Refactored `ScannerViewModel`** - Uses `UnifiedAnalysisService.shared.analyze(url:)`
3. **✅ Added URL validation** in `MainMenuView` "Paste URL" action
4. **✅ Added `UnifiedAnalysisService.swift`** to Xcode project
5. **✅ Fixed parameter order** in `UnifiedAnalysisService` RiskAssessmentMock calls
6. **✅ Build verified** - BUILD SUCCEEDED on iPhone 17 Simulator

### 🔄 Remaining Tasks

1. **Test KMP framework linking** - Link `common.framework` and verify `isKMPAvailable == true`
2. **Run UI tests** - Execute `MehrGuardUITests` to verify all interactive flows
3. **Accessibility audit** - Add missing `accessibilityLabel` and `accessibilityHint` values
4. **Version constants** - Consolidate hard-coded version strings to `AppConstants` file

---

*This document is the canonical reference for iOS surface area during the Phase 0 audit.*
