# 🛡️ QR-SHIELD

<div align="center">

```
   ██████╗ ██████╗       ███████╗██╗  ██╗██╗███████╗██╗     ██████╗ 
  ██╔═══██╗██╔══██╗      ██╔════╝██║  ██║██║██╔════╝██║     ██╔══██╗
  ██║   ██║██████╔╝█████╗███████╗███████║██║█████╗  ██║     ██║  ██║
  ██║▄▄ ██║██╔══██╗╚════╝╚════██║██╔══██║██║██╔══╝  ██║     ██║  ██║
  ╚██████╔╝██║  ██║      ███████║██║  ██║██║███████╗███████╗██████╔╝
   ╚══▀▀═╝ ╚═╝  ╚═╝      ╚══════╝╚═╝  ╚═╝╚═╝╚══════╝╚══════╝╚═════╝ 
                                                                   
            🔍 Kotlin Multiplatform QRishing Detector 🔍
```

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![KMP](https://img.shields.io/badge/Kotlin_Multiplatform-Enabled-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose](https://img.shields.io/badge/Compose_Multiplatform-1.6.0-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Desktop](https://img.shields.io/badge/Desktop-JVM-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue?style=for-the-badge)](LICENSE)
[![CI](https://img.shields.io/github/actions/workflow/status/Raoof128/QDKMP-KotlinConf-2026-/ci.yml?style=for-the-badge&logo=github&label=CI)](https://github.com/Raoof128/QDKMP-KotlinConf-2026-/actions)

**Scan QR codes. Detect phishing. Stay protected on Android and Desktop.**

[Features](#-features) • [Architecture](#-architecture) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [Contributing](#-contributing)

</div>

---

## 📋 Table of Contents

- [The Problem](#-the-problem-qrishing-is-exploding)
- [Features](#-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Quick Start](#-quick-start)
- [Risk Scoring](#-risk-scoring)
- [Documentation](#-documentation)
- [Testing](#-testing)
- [Security](#-security)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Elevator Pitch

> **"QRishing attacks have increased 587% since 2023. QR-SHIELD is the first Kotlin Multiplatform solution that provides real-time phishing detection across Android, iOS, Desktop, and Web—with a single shared codebase."**

QR-SHIELD scans QR codes from your camera or gallery, extracts embedded URLs, and uses a sophisticated multi-layer analysis engine combining **cybersecurity heuristics**, **ML-lite scoring**, and **brand impersonation detection** to protect users from QRishing (QR code phishing) attacks.

---

## 🚨 The Problem: QRishing is Exploding

```
┌─────────────────────────────────────────────────────────────────────┐
│                    QRishing Attack Landscape 2025                   │
├─────────────────────────────────────────────────────────────────────┤
│  📈 587% increase in QR-based phishing attacks since 2023          │
│  📱 71% of users don't verify URLs before scanning QR codes        │
│  🏦 Banking & financial services: #1 impersonated sector           │
│  📧 Corporate email QR codes: fastest growing attack vector        │
│  🌐 Cross-platform attacks: single QR, multiple device targets     │
└─────────────────────────────────────────────────────────────────────┘
```

**QRishing** exploits user trust in QR codes—those ubiquitous squares at restaurants, parking meters, and corporate communications. Attackers embed malicious URLs that redirect to credential harvesting sites, malware downloads, or social engineering traps.

---

## ✨ Features

### 📷 Multi-Source QR Scanning
- **Camera Scanning**: Real-time QR detection with ML Kit (Android) / Vision API (iOS)
- **Gallery Import**: Analyze QR codes from saved images
- **Clipboard Detection**: Scan URLs directly from clipboard
- **Batch Processing**: Analyze multiple QR codes simultaneously

### 🔍 Intelligent Risk Analysis
- **URL Heuristics Engine**: 25+ risk signals analyzed
- **Brand Impersonation Detection**: Fuzzy matching against 500+ brands
- **TLD Risk Scoring**: Dangerous domain registry identification
- **Homograph Attack Detection**: Unicode/punycode lookalike detection
- **Path & Query Inspection**: Suspicious parameter analysis

### 🤖 ML-Lite Phishing Scorer
- **On-device inference**: No cloud dependency
- **Logistic regression model**: Lightweight, fast, private
- **Feature extraction**: 15+ URL characteristics
- **Confidence scoring**: Probabilistic risk assessment

### 🎨 Beautiful Cross-Platform UI
- **Compose Multiplatform**: Shared UI across all platforms
- **Dark/Light themes**: System-aware theming
- **Accessibility**: Screen reader support, large text
- **Animations**: Smooth, engaging micro-interactions

### 🔒 Privacy-First Design
- **100% offline capable**: No data leaves device
- **No telemetry**: Zero tracking
- **Local history**: Encrypted local storage
- **Open source**: Full audit transparency

---

## 📊 Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           QR-SHIELD ARCHITECTURE                            │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│   Android   │  │     iOS     │  │   Desktop   │  │     Web     │
│  (ML Kit)   │  │  (Vision)   │  │   (ZXing)   │  │   (jsQR)    │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │                │
       └────────────────┴────────────────┴────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │   Compose Multiplatform │
                    │         UI Layer        │
                    └───────────┬───────────┘
                                │
                  ┌─────────────▼─────────────┐
                  │                           │
                  │     📦 COMMON MODULE      │
                  │     ─────────────────     │
                  │                           │
                  │  ┌─────────────────────┐  │
                  │  │   PhishingEngine    │  │
                  │  │   ─────────────────  │  │
                  │  │  • HeuristicsEngine │  │
                  │  │  • BrandDetector    │  │
                  │  │  • TldScorer        │  │
                  │  │  • MLModel          │  │
                  │  └─────────────────────┘  │
                  │              │            │
                  │  ┌───────────▼─────────┐  │
                  │  │    RiskScorer       │  │
                  │  │    ────────────     │  │
                  │  │  Combined Score     │  │
                  │  │  Verdict Engine     │  │
                  │  └─────────────────────┘  │
                  │                           │
                  └───────────────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │   VERDICT OUTPUT      │
                    │   ────────────────    │
                    │  SAFE | SUSPICIOUS |  │
                    │      MALICIOUS        │
                    └───────────────────────┘
```

### Data Flow

```
📱 QR Code ──► 🔍 Scanner ──► 🔗 URL ──► 🧪 Analysis ──► 📊 Score ──► ✅ Verdict
                                              │
                           ┌──────────────────┼──────────────────┐
                           ▼                  ▼                  ▼
                    Heuristics           ML Model          Brand Check
                    (25+ rules)        (15 features)      (500+ brands)
```

---

## �️ Technology Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Language** | Kotlin 2.0.0 | Cross-platform business logic |
| **UI Framework** | Compose Multiplatform 1.6.0 | Shared declarative UI |
| **Async** | Kotlin Coroutines + Flow | Reactive programming |
| **DI** | Koin Multiplatform | Dependency injection |
| **Networking** | Ktor Client | Optional URL expansion |
| **Persistence** | SQLDelight | Cross-platform database |
| **Android Camera** | ML Kit Barcode Scanning | Native QR detection |
| **iOS Camera** | AVFoundation + Vision | Native QR detection |
| **Desktop Camera** | ZXing + OpenCV | JVM QR detection |
| **Web Camera** | jsQR + MediaDevices | Browser QR detection |
| **Testing** | kotlin.test, Kotest | Unit & integration tests |

---

## �🚀 Quick Start

### Prerequisites

- **JDK 17+**
- **Android Studio Hedgehog (2023.1.1)** or IntelliJ IDEA
- **Xcode 15+** (for iOS)
- **Kotlin 2.0.0**

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/yourusername/qrshield.git
cd qrshield

# Build all platforms
./gradlew build

# Run Android
./gradlew :androidApp:installDebug

# Run Desktop
./gradlew :desktopApp:run

# Run Web (development server)
./gradlew :webApp:jsBrowserDevelopmentRun
```

### iOS Setup

```bash
cd iosApp
pod install
open QRShield.xcworkspace
# Build and run in Xcode
```

---

## 📈 Risk Scoring

### Scoring Formula

```kotlin
Final Score = (
    Heuristic Score × 0.40 +
    ML Model Score × 0.35 +
    Brand Impersonation Score × 0.15 +
    TLD Risk Score × 0.10
) × 100
```

### Verdict Thresholds

| Score Range | Verdict | Action |
|-------------|---------|--------|
| 0-30 | ✅ **SAFE** | URL appears safe to visit |
| 31-70 | ⚠️ **SUSPICIOUS** | Proceed with caution |
| 71-100 | ❌ **MALICIOUS** | Do not visit this URL |

### Heuristic Rules (25+)

| Rule | Weight | Description |
|------|--------|-------------|
| `HTTP_NOT_HTTPS` | 15 | No TLS/SSL encryption |
| `IP_ADDRESS_HOST` | 20 | IP instead of domain name |
| `EXCESSIVE_SUBDOMAINS` | 10 | More than 3 subdomain levels |
| `SUSPICIOUS_TLD` | 12 | High-risk TLDs (.tk, .ml, .ga) |
| `URL_SHORTENER` | 8 | Redirect service detected |
| `BRAND_IMPERSONATION` | 25 | Brand name in subdomain |
| `HOMOGRAPH_ATTACK` | 30 | Unicode lookalike characters |
| `CREDENTIAL_PARAMS` | 18 | Password/token in query |

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [Architecture](docs/ARCHITECTURE.md) | System architecture & design |
| [API Reference](docs/API.md) | Complete API documentation |
| [Master Pack](docs/MASTER_PACK.md) | Complete project overview |
| [UI Design System](docs/UI_DESIGN_SYSTEM.md) | Colors, typography, components |
| [Threat Model](docs/THREAT_MODEL.md) | Attack vectors & defenses |
| [Compliance](docs/COMPLIANCE.md) | ACSC, Privacy Act, ISO mapping |
| [Competition Pitch](docs/PITCH.md) | Presentation materials |
| [Demo Script](docs/DEMO_SCRIPT.md) | Video storyboard |
| [Changelog](CHANGELOG.md) | Version history |
| [Security Policy](SECURITY.md) | Vulnerability reporting |
| [Contributing](CONTRIBUTING.md) | Contribution guidelines |
| [Code of Conduct](CODE_OF_CONDUCT.md) | Community standards |

---

## 🧪 Testing

```bash
# Run all tests
./gradlew allTests

# Run common module tests only
./gradlew :common:testDebugUnitTest

# Run with coverage report
./gradlew koverReport
```

### Test Coverage

- **PhishingEngine**: URL analysis orchestration
- **HeuristicsEngine**: 25+ rule validation
- **BrandDetector**: Typosquat & homograph detection
- **TldScorer**: TLD risk classification
- **RiskScorer**: Combined score calculation

---

## 🔒 Security

| Aspect | Implementation |
|--------|----------------|
| **Data at Rest** | AES-256 encrypted local database |
| **Data in Transit** | No data leaves device (offline-first) |
| **URL Expansion** | Optional Ktor client with TLS 1.3 |
| **Secrets** | No API keys in codebase |
| **Privacy** | Zero telemetry, zero tracking |

See [SECURITY.md](SECURITY.md) for vulnerability reporting guidelines.

---

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md).

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📜 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **JetBrains** - For Kotlin and Compose Multiplatform
- **Google** - For ML Kit barcode scanning
- **Security Researchers** - For phishing heuristics research
- **Open Source Community** - For inspiration and libraries

---

<div align="center">

**Made with 💜 using Kotlin Multiplatform**

*Protecting users from QRishing attacks—one scan at a time.*

⭐ Star us on GitHub if you find this useful!

</div>
