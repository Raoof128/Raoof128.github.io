# 🍎 iOS Architecture Decision: SwiftUI vs Compose Multiplatform

> A detailed explanation of why Mehr Guard uses native SwiftUI for iOS instead of Compose Multiplatform.

---

## TL;DR

**We intentionally chose SwiftUI for iOS UI while sharing 100% of business logic via KMP.**

This is a **strategic architectural decision**, not a limitation.

---

## The Decision Matrix

| Consideration | SwiftUI (Chosen) | Compose for iOS |
|---------------|------------------|-----------------|
| **Production Readiness** | ✅ Stable, 5+ years | ⚠️ Alpha/Beta |
| **App Store Approval** | ✅ No issues | ⚠️ Potential concerns |
| **iOS Native Patterns** | ✅ Full support | ⚠️ Partial |
| **Camera (AVFoundation)** | ✅ Native integration | ⚠️ Requires bridging |
| **iOS 17+ Features** | ✅ sensoryFeedback, symbolEffect | ⚠️ Not available |
| **Learning Curve** | ⚠️ Different language | ✅ Same as Android |
| **UI Code Sharing** | ❌ None | ✅ Full |

---

## Why This Matters for Security Apps

### 1. Camera Access is Critical

Mehr Guard's core functionality requires:
- Real-time camera preview
- High-performance QR detection
- Torch/flash control
- Focus and exposure

**AVFoundation is the only way to achieve this on iOS.** There is no Compose equivalent.

```swift
// Native iOS camera - works perfectly
class CameraManager: NSObject, AVCaptureMetadataOutputObjectsDelegate {
    let session = AVCaptureSession()
    
    func metadataOutput(_ output: AVCaptureMetadataOutput,
                       didOutput metadataObjects: [AVMetadataObject],
                       from connection: AVCaptureConnection) {
        // Real-time QR detection at 60fps
    }
}
```

### 2. App Store Considerations

Compose for iOS is still in alpha. Production apps using experimental frameworks may face:
- Longer review times
- Rejection for crashes
- Performance concerns

**We chose reliability over experimentation.**

### 3. Platform Expectations

iOS users expect:
- Native navigation patterns (back swipe)
- SF Symbols (not custom icons)
- Haptic feedback (sensoryFeedback modifier)
- Platform animations (matchedGeometryEffect)

SwiftUI delivers all of these. Compose for iOS does not.

---

## What IS Shared via KMP

Despite using SwiftUI for UI, **100% of the detection logic is shared Kotlin**:

```swift
// iOS Swift code calling Kotlin
import common  // KMP Framework

class ScannerViewModel: ObservableObject {
    private let engine = PhishingEngine()  // Kotlin!
    
    func analyze(url: String) -> UrlAnalysisResult {
        return engine.analyze(url: url)  // Kotlin analysis
    }
}
```

### Shared Kotlin Modules Used by iOS:

| Module | Purpose | Lines |
|--------|---------|-------|
| `PhishingEngine` | Main analysis orchestrator (suspend/blocking API) | 400 |
| `HeuristicsEngine` | 25+ security checks | 450 |
| `BrandDetector` | Brand impersonation | 500 |
| `EnsembleModel` | 3-model ML ensemble | 380 |
| `LogisticRegressionModel` | Linear ML scoring | 400 |
| `TldScorer` | TLD risk assessment | 150 |
| `FeatureExtractor` | URL feature extraction | 300 |
| `SharedResultCard` (Compose) | Compose MP result card (can embed in SwiftUI!) | 200 |
| `SharedTextGenerator` | Centralized verdict text | 300 |
| **Total Shared** | **Business Logic + UI Components** | **~5,500 LOC** |

### Hybrid Approach (v1.6.0+)

We now support embedding Compose MP components in SwiftUI via `SharedResultCardViewController`:

```swift
// iOS SwiftUI embedding Compose MP component
struct ComposeResultView: UIViewControllerRepresentable {
    let assessment: RiskAssessment
    
    func makeUIViewController(context: Context) -> UIViewController {
        return SharedResultCardViewController(assessment: assessment)
    }
}
```

This gives us the **best of both worlds**: native SwiftUI where needed, shared Compose components for complex visualizations.

---

## Code Comparison: Swift Calling Kotlin

### Before KMP (Hypothetical Pure Swift)

```swift
// Would need to rewrite ALL detection logic in Swift
class PhishingDetector {
    func analyze(url: String) -> Result {
        // Rewrite 4,700 lines of Kotlin in Swift
        // Risk: Logic drift between platforms
        // Risk: Bugs fixed once, not everywhere
    }
}
```

### With KMP (What We Actually Do)

```swift
// Swift UI only - logic is Kotlin
import common

struct ResultCard: View {
    let assessment: UrlAnalysisResult  // Kotlin type!
    
    var body: some View {
        VStack {
            Text(assessment.verdict.name)  // From Kotlin enum
            Text("Score: \(assessment.score)")  // Kotlin Int
            ForEach(assessment.flags, id: \.self) { flag in
                Text(flag)  // Kotlin List<String>
            }
        }
    }
}
```

---

## The 80/20 Rule in Practice

```
┌────────────────────────────────────────────────────────────────┐
│                    Mehr Guard Code Distribution                  │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  SHARED KOTLIN (80%)                                            │
│  ████████████████████████████████████████████████░░░░░░░░░░    │
│  • Detection Engine                                             │
│  • ML Model                                                     │
│  • Brand Database                                               │
│  • Data Models                                                  │
│  • History Repository                                           │
│                                                                 │
│  PLATFORM-SPECIFIC (20%)                                        │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░████████████          │
│  • Android: Compose UI + CameraX                                │
│  • iOS: SwiftUI + AVFoundation                                  │
│  • Desktop: Compose Desktop + ZXing                             │
│  • Web: HTML/JS + jsQR                                          │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

---

## Future Consideration: Compose for iOS

When Compose for iOS reaches stable (likely 2026+), we may:

1. Add Compose for iOS as an alternative UI layer
2. Keep SwiftUI for camera-heavy screens
3. Use Compose for settings, history, about screens

**But for a production security app in 2025, SwiftUI is the right choice.**

---

## Conclusion

> **The value of KMP is in shared business logic, not shared UI.**

We chose to:
- ✅ Share 100% of security-critical detection code
- ✅ Use native UI frameworks for best user experience
- ✅ Prioritize App Store approval and stability
- ✅ Leverage iOS 17+ features (sensoryFeedback, symbolEffect)

This is **intentional multiplatform engineering**, not a compromise.

---

*Last updated: December 2025*
