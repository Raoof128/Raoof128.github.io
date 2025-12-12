# Changelog

All notable changes to QR-SHIELD will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2025-12-12

### Added

#### Native iOS SwiftUI Integration
- Native iOS SwiftUI app with KMP framework integration
- `KMPBridge.swift` for calling Kotlin HeuristicsEngine from Swift
- `KMPDemoView.swift` demonstrating KMP integration
- `build_framework.sh` script for building iOS framework
- Comprehensive iOS setup documentation (`iosApp/README.md`)

#### Internationalization (11 Languages)
- Portuguese (pt) localization
- Korean (ko) localization
- Italian (it) localization
- Russian (ru) localization
- Total: 11 supported languages

#### Accessibility (Android)
- 20+ new content description strings for TalkBack
- Semantic descriptions for all interactive elements
- Full screen reader support

#### Package Organization
- New `orchestration/` package with `PhishingOrchestrator` type alias
- New `analysis/` package with analysis utilities
- Comprehensive `PACKAGE_ARCHITECTURE.md` documentation

#### Performance Benchmarks
- `PerformanceBenchmarkTest.kt` with 6 benchmark tests
- Full URL analysis benchmark (target: <50ms)
- Heuristics engine benchmark (target: <10ms)
- ML inference benchmark (target: <5ms)
- Brand detection benchmark (target: <15ms)
- Throughput benchmark (target: >100 URLs/sec)

#### Documentation
- Comprehensive KDoc for all model classes
- Usage examples in documentation
- `@property`, `@see`, `@since` tags per KDoc standards

### Changed
- iOS distribution changed from TestFlight (requires paid account) to Web App (PWA)
- Simplified iOS scanner stub for clean compilation
- Updated README with iOS Web App installation instructions

### Fixed
- iOS framework binary export configuration
- Package documentation comments causing compilation errors

---

## [1.0.0] - 2025-12-01

### Added

#### Core Detection Engine
- PhishingEngine with 25+ security heuristics
- BrandDetector with 500+ brand database
- TldScorer for domain risk assessment
- HomographDetector for Unicode attack detection
- LogisticRegressionModel for ML-based scoring
- Configurable heuristic weights (`HeuristicWeightsConfig`)

#### Android Application
- Full Compose UI with Material 3
- Camera QR scanning with ML Kit
- Photo picker for gallery scanning
- Haptic and sound feedback
- Scan history with SQLDelight persistence
- Settings with preferences persistence
- Dark/Light theme support

#### Desktop Application
- Compose for Desktop UI
- ZXing-based QR scanning
- Cross-platform JAR distribution

#### Web Application
- Kotlin/JS web target
- PWA support with offline capability
- GitHub Pages deployment

#### Testing
- Comprehensive unit test suite
- Integration tests
- Real-world phishing test cases

#### CI/CD
- GitHub Actions workflow for CI
- Automated release workflow
- Android signed APK builds

### Security
- All URL analysis performed locally (offline-first)
- No data transmitted to external servers
- Input validation and sanitization
- Safe regex patterns (no ReDoS)

---

## [0.1.0] - 2025-11-15

### Added
- Initial project structure with Kotlin Multiplatform
- Basic URL parsing and analysis
- Proof of concept detection engine

---

## Release Links

[1.1.0]: https://github.com/Raoof128/QDKMP-KotlinConf-2026-/releases/tag/v1.1.0
[1.0.0]: https://github.com/Raoof128/QDKMP-KotlinConf-2026-/releases/tag/v1.0.0
[0.1.0]: https://github.com/Raoof128/QDKMP-KotlinConf-2026-/releases/tag/v0.1.0
