# iOS Integration Guide

This guide proves that iOS uses shared Kotlin code, not a parallel Swift implementation.

---

## Quick Verification (5 minutes)

```bash
# 1. Build KMP framework for iOS Simulator
./gradlew :common:linkDebugFrameworkIosSimulatorArm64

# 2. Open in Xcode
open iosApp/MehrGuard.xcodeproj

# 3. Select iPhone 16 Pro simulator
# 4. Run app (Cmd+R)
# 5. Scan any QR code with URL
# 6. Observe: Swift UI calls Kotlin PhishingEngine
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         iOS App                                  │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────┐    ┌─────────────────────────────────┐ │
│  │     SwiftUI Views   │    │      AVFoundation Camera        │ │
│  │   (Presentation)    │    │   (Native QR Scanning)          │ │
│  └──────────┬──────────┘    └──────────────┬──────────────────┘ │
│             │                               │                    │
│             └───────────────┬───────────────┘                    │
│                             ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    KMPBridge.swift                          │ │
│  │                                                             │ │
│  │  import common  // ← Kotlin Framework                       │ │
│  │                                                             │ │
│  │  class PhishingAnalyzer {                                   │ │
│  │      private let engine = PhishingEngine()  // ← KOTLIN    │ │
│  │                                                             │ │
│  │      func analyze(url: String) -> RiskAssessment {          │ │
│  │          return engine.analyzeBlocking(url: url)            │ │
│  │      }                   ▲                                  │ │
│  │  }                       │                                  │ │
│  └──────────────────────────┼──────────────────────────────────┘ │
│                             │                                    │
├─────────────────────────────┼────────────────────────────────────┤
│                             ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                  Kotlin Multiplatform                       │ │
│  │                                                             │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │ │
│  │  │ PhishingEngine  │  │ HeuristicsEngine│  │ EnsembleModel│ │ │
│  │  │   (100% Kotlin) │  │   (100% Kotlin) │  │ (100% Kotlin)│ │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────┘ │ │
│  │                                                             │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │ │
│  │  │  BrandDetector  │  │    TldScorer    │  │FeatureExtrac│ │ │
│  │  │   (100% Kotlin) │  │   (100% Kotlin) │  │ (100% Kotlin)│ │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────┘ │ │
│  │                                                             │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## Code Walkthrough

### 1. KMPBridge.swift

Location: `iosApp/MehrGuard/KMPBridge.swift`

```swift
import Foundation
import common  // ← This is the Kotlin framework!

/// Bridge between SwiftUI and Kotlin PhishingEngine
class PhishingAnalyzer: ObservableObject {
    
    // Kotlin PhishingEngine instance
    private let engine = PhishingEngine()  // ← This is Kotlin code
    
    /// Analyze URL using Kotlin detection engine
    /// - Parameter url: URL extracted from QR code
    /// - Returns: RiskAssessment (Kotlin data class)
    func analyze(url: String) -> RiskAssessment {
        // Calls Kotlin function directly!
        return engine.analyzeBlocking(url: url)
    }
}
```

### 2. ContentView.swift

```swift
struct ContentView: View {
    @StateObject private var analyzer = PhishingAnalyzer()
    
    func onQRCodeScanned(url: String) {
        // SwiftUI calls Kotlin engine
        let result = analyzer.analyze(url: url)
        
        // result.verdict is Kotlin Verdict sealed class
        switch result.verdict {
        case .safe:
            showSafeBanner()
        case .suspicious:
            showWarning()
        case .malicious:
            showDangerAlert()
        default:
            break
        }
    }
}
```

---

## What's Shared vs Native

### 100% Shared (Kotlin)

| Component | File | LOC |
|-----------|------|-----|
| PhishingEngine | `common/.../PhishingEngine.kt` | 418 |
| HeuristicsEngine | `common/.../HeuristicsEngine.kt` | 650+ |
| BrandDetector | `common/.../BrandDetector.kt` | 400+ |
| EnsembleModel | `common/.../EnsembleModel.kt` | 380 |
| FeatureExtractor | `common/.../FeatureExtractor.kt` | 200+ |
| DynamicBrandDiscovery | `common/.../DynamicBrandDiscovery.kt` | 300+ |
| **Total Shared** | | **~7,400 LOC** |

### Native (Swift)

| Component | Reason |
|-----------|--------|
| AVFoundation Camera | Apple's camera API is native-only |
| SwiftUI Views | iOS design conventions |
| Haptic Feedback | UIImpactFeedbackGenerator |
| Keychain Storage | iOS security model |
| **Total Native** | **~6,500 LOC** |

### Ratio

- **53% Kotlin** (detection logic)
- **47% Swift** (camera + UI)

---

## Debugging Integration

### Step 1: Enable Kotlin Logging

In `iosApp/MehrGuard/AppDelegate.swift`:

```swift
import common

@main
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, 
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        // Enable Kotlin debug logging
        PlatformKt.enableDebugLogging()
        return true
    }
}
```

### Step 2: Set Breakpoint

1. Open `KMPBridge.swift` in Xcode
2. Click line number next to `engine.analyzeBlocking(url: url)`
3. Run app and scan QR code
4. Breakpoint hits → proves Kotlin code is executing

### Step 3: Inspect Kotlin Objects

In LLDB debugger:

```
(lldb) po result
▿ RiskAssessment
  - score : 75
  - verdict : Verdict.MALICIOUS
  - flags : ["Brand impersonation detected: paypal"]
  - confidence : 0.85
```

---

## Build Outputs

### Framework Location

After building:

```
common/build/bin/iosSimulatorArm64/debugFramework/common.framework
```

### Framework Contents

```bash
$ ls -la common.framework/
├── Headers/
│   └── common.h          # Objective-C bridging header
├── Modules/
│   └── module.modulemap  # Swift module map
├── common                 # Native binary
└── Info.plist
```

### Verify Kotlin Symbols

```bash
$ nm common.framework/common | grep PhishingEngine
_kniprop_com_mehrguard_core_PhishingEngine_analyze
_kniprop_com_mehrguard_core_PhishingEngine_analyzeBlocking
```

---

## FAQ

### Q: Is this just a wrapper around a REST API?

**No.** There is no network call. The Kotlin code compiles to native ARM64 binary that runs directly on device.

### Q: Why not use Compose Multiplatform for iOS UI?

We demonstrate hybrid approach:
- `SharedResultCard` uses Compose (proof of concept)
- Main UI uses SwiftUI for native feel

See `iosApp/MehrGuard/ComposeInterop.swift` for the hybrid integration.

### Q: How do I verify 53% code sharing?

```bash
# Count Kotlin LOC in common/
$ find common/src/commonMain -name "*.kt" | xargs wc -l
# → ~7,400 lines

# Count Swift LOC in iosApp/
$ find iosApp -name "*.swift" | xargs wc -l
# → ~6,500 lines
```

---

## See Also

- [README.md](../README.md) - Full project documentation
- [ARCHITECTURE.md](../docs/ARCHITECTURE.md) - Overall architecture
- [ComposeInterop.swift](../iosApp/MehrGuard/ComposeInterop.swift) - Compose + SwiftUI hybrid
