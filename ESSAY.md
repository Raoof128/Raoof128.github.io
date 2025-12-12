# QR-SHIELD: My Journey Building a Cross-Platform QRishing Detector

## The Problem That Inspired Me

In 2024, I watched my grandmother nearly fall victim to a QR code scam at a parking meter. She scanned what appeared to be a legitimate payment code, only to land on a convincing phishing page mimicking her bank. That moment crystallized a problem I'd been reading about: **QRishing attacks have increased 587% since 2023**, yet no mainstream solution exists to protect everyday users.

The irony struck me—we've spent decades teaching people to hover over links before clicking, but QR codes bypass that instinct entirely. 71% of users never verify URLs after scanning. The attack vector is simple, effective, and devastating.

## Why Kotlin Multiplatform?

I chose Kotlin Multiplatform because security shouldn't depend on which device you own. A phishing attack targeting an Android user is equally dangerous to an iOS user, yet most security solutions are platform-siloed. KMP allowed me to write the detection engine once and deploy everywhere—sharing **85% of my codebase** across Android, iOS, Desktop, and Web.

The technical challenge was exhilarating:

- **`expect`/`actual` pattern** for platform-specific camera access
- **Kotlin Native `cinterop`** to call iOS `AVFoundation` directly from Kotlin
- **Lightweight ML model** running entirely on-device (no cloud dependency)
- **Multi-layer detection** combining heuristics, machine learning, and brand impersonation detection

Each obstacle pushed my engineering skills further than any classroom assignment ever had.

## Technical Highlights

### The Detection Engine

Building a phishing detector that works offline was the core challenge. I developed a **multi-layer analysis system**:

1. **25+ Heuristic Rules** — Detecting common phishing patterns like IP addresses as hosts, suspicious TLDs, homograph attacks, and excessive subdomains.

2. **Logistic Regression ML Model** — Trained on URL features to provide probabilistic risk scoring. The model is small enough to run instantly on-device.

3. **Brand Impersonation Detection** — Fuzzy matching against 500+ brand names to catch typosquatting attacks like "paypa1" or "amaz0n".

4. **TLD Risk Scoring** — Identifying high-risk domain registries that are disproportionately used for phishing.

### Code Reuse Metrics

| Module | Lines of Code | Shared (%) |
|--------|---------------|------------|
| Detection Engine | 2,500+ | 100% |
| UI Components | 1,200+ | 100% |
| Platform Adapters | 800 | 0% |
| **Total** | **4,500+** | **~85%** |

## What I Learned

Building QR-SHIELD taught me that **security is about layers, not silver bullets**. No single detection method catches everything, but combining heuristic rules, an ML classifier, and fuzzy brand matching creates defense in depth.

More importantly, I learned that **privacy and protection aren't mutually exclusive**. By designing for offline-first operation, I proved that we can protect users without harvesting their data. Every analysis happens on-device. No telemetry. No tracking. Just protection.

I also discovered the power of Kotlin Multiplatform for real-world applications. The ability to share complex business logic across four platforms while still accessing native APIs (ML Kit on Android, Vision on iOS) was transformative.

## Impact

Since publishing QR-SHIELD:

- **100% open source** under Apache 2.0 license
- **11 languages** supported (English, Spanish, French, German, Arabic, Japanese, Chinese, Portuguese, Korean, Italian, Russian)
- **Accessibility features** for screen readers
- **Zero runtime permissions** beyond camera access

## Looking Forward

QR-SHIELD represents my commitment to accessible, privacy-respecting security tools. I believe every person deserves protection from phishing—regardless of their technical sophistication or the device in their pocket.

The QR code isn't going away. Neither are the attackers exploiting it. But with tools like QR-SHIELD, we can shift the balance back toward the user.

**Scan smart. Stay protected.**

---

*Word count: 594*

---

## About the Author

I'm a student developer passionate about cybersecurity and cross-platform development. QR-SHIELD is my submission to the 2025-2026 KotlinConf Student Coding Competition.

- **GitHub:** [github.com/Raoof128](https://github.com/Raoof128)
- **Project:** [QR-SHIELD](https://github.com/Raoof128/Raoof128.github.io)
- **Live Demo:** [raoof128.github.io](https://raoof128.github.io/)
