# Changelog

All notable changes to QR-SHIELD will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project structure with Kotlin Multiplatform
- PhishingEngine with 25+ security heuristics
- BrandDetector with 500+ brand database
- TldScorer for domain risk assessment
- HomographDetector for Unicode attack detection
- LogisticRegressionModel for ML-based scoring
- SharedViewModel for cross-platform UI state
- Android app with Compose UI
- Desktop app with Compose for Desktop
- SQLDelight schema for scan history
- Comprehensive unit test suite

### Changed
- N/A (initial release)

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- All URL analysis performed locally (offline-first)
- No data transmitted to external servers
- AES-256 encryption for local storage

## [1.0.0] - TBD

### Added
- First stable release
- Android, iOS, Desktop, and Web support
- Full phishing detection engine
- Real-time camera QR scanning
- Gallery image import
- Clipboard URL analysis
- Scan history with persistence
- Dark and light themes
- Accessibility support

---

[Unreleased]: https://github.com/yourusername/qrshield/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/yourusername/qrshield/releases/tag/v1.0.0
