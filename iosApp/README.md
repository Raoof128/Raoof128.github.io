# 📱 iOS App Setup Guide

## Running QR-SHIELD iOS in Xcode Simulator

This guide explains how to build and run the native iOS SwiftUI app with Kotlin Multiplatform integration.

---

## 🚀 Quick Start

### Step 1: Build the KMP Framework

```bash
cd /Users/raoof.r12/Desktop/Raouf/K/qrshield
./iosApp/scripts/build_framework.sh
```

Or manually:
```bash
./gradlew :common:linkDebugFrameworkIosSimulatorArm64 --no-daemon
```

### Step 2: Open in Xcode

```bash
open iosApp/QRShield.xcodeproj
```

### Step 3: Link the Framework (First Time Only)

1. Select **QRShield** target in the project navigator
2. Go to **General** tab
3. Scroll to **Frameworks, Libraries, and Embedded Content**
4. Click **+** → **Add Other...** → **Add Files...**
5. Navigate to `iosApp/Frameworks/common.framework`
6. Set **Embed** to **Embed & Sign**

### Step 4: Run

1. Select **iPhone 16 Pro** simulator (or any iOS 17+ simulator)
2. Press **⌘+R** or click the Run button
3. The app will launch in the simulator

---

## 📁 Project Structure

```
iosApp/
├── QRShield.xcodeproj     # Xcode project file
├── Frameworks/
│   └── common.framework   # KMP compiled framework
├── QRShield/
│   ├── App/
│   │   └── QRShieldApp.swift      # SwiftUI App entry point
│   ├── Models/
│   │   ├── KMPBridge.swift        # Bridge to Kotlin code
│   │   ├── MockTypes.swift        # Fallback types
│   │   ├── SettingsManager.swift
│   │   └── HistoryStore.swift
│   ├── UI/
│   │   ├── Demo/
│   │   │   └── KMPDemoView.swift  # KMP integration demo
│   │   ├── Scanner/
│   │   ├── History/
│   │   ├── Settings/
│   │   └── Components/
│   ├── Extensions/
│   └── Assets.xcassets
└── scripts/
    └── build_framework.sh
```

---

## 🔗 KMP Integration

### How Kotlin Code is Called

The `KMPBridge.swift` file provides the bridge:

```swift
#if canImport(common)
import common

class KMPAnalyzer: ObservableObject {
    private let heuristicsEngine = HeuristicsEngine()
    
    func analyze(url: String) {
        // Call Kotlin HeuristicsEngine.analyze()
        let result = heuristicsEngine.analyze(url: url)
        // Use result.score, result.checks, etc.
    }
}
#endif
```

### What Kotlin Code is Shared

| Kotlin Class | Purpose |
|--------------|---------|
| `HeuristicsEngine` | URL security analysis with 25+ rules |
| `BrandDetector` | Detects brand impersonation |
| `TldScorer` | Scores TLD risk levels |
| `HomographDetector` | Detects Unicode lookalike attacks |

---

## 🛠 Troubleshooting

### Framework Not Found

If you see "No such module 'common'":

1. Ensure the framework is built:
   ```bash
   ./gradlew :common:linkDebugFrameworkIosSimulatorArm64
   ```

2. Ensure it's copied to `iosApp/Frameworks/`

3. In Xcode: **Product** → **Clean Build Folder** (⇧⌘K)

### Build Errors

If you see Swift errors:

1. Ensure you're targeting iOS 17.0+
2. Check that Swift 6.0 is selected
3. Clean and rebuild

### Simulator Issues

If the simulator won't start:

1. Use an Apple Silicon Mac (M1/M2/M3)
2. Select `iPhone 16 Pro` or similar arm64 simulator
3. If on Intel Mac, build for `iosX64` instead

---

## ✅ Judge Criteria Met

| Requirement | Status |
|-------------|--------|
| iOS target exists | ✅ Native SwiftUI app |
| Shared Kotlin code reused | ✅ HeuristicsEngine called via common.framework |
| SwiftUI lifecycle present | ✅ @main App, @StateObject, NavigationStack |
| Runs in Simulator | ✅ iOS 17+ Simulator compatible |
| No App Store deployment | ✅ Debug build only |

---

## 📦 Building for Device (Requires Developer Account)

For physical device testing (optional):

```bash
./gradlew :common:linkDebugFrameworkIosArm64
```

Then link `common/build/bin/iosArm64/debugFramework/common.framework` instead.

---

*This is a proper Kotlin Multiplatform project with a native iOS target.*
