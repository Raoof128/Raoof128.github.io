# QR-SHIELD: My Journey Building a Cross-Platform QRishing Detector

> **"Security should never require PhD-level expertise to use."**

---

## About Me

I'm a Computer Science student from Sydney, Australia, passionate about cybersecurity and mobile development. With 3+ years of Kotlin experience and active participation in CTF competitions, I've developed a deep understanding of how attackers think—and how to stop them.

**My Background:**
- 3+ years Kotlin experience, 2 years iOS/Swift
- Active CTF competitor (taught me attacker psychology)
- Open source contributor to KMP libraries
- Mentor for younger coding students

---

## The Problem

In early 2025, I watched my grandmother nearly fall victim to a QR code scam at a parking meter. She scanned what appeared to be a legitimate payment code, landing on `paypa1-secure.tk`—a near-perfect PayPal phishing page. I grabbed her phone just in time.

That moment crystallized the problem: **QRishing attacks have increased 587% since 2023**, yet no mainstream solution exists. 71% of users never verify URLs after scanning. We taught people to hover over links, but QR codes bypass that instinct entirely.

---

## The Solution

**QR-SHIELD** is a Kotlin Multiplatform security application that detects QR phishing entirely **offline**—no URL ever leaves your device.

### Why Offline Matters

Cloud scanners like Google Safe Browsing require network access and log every URL you scan—exposing which banks you use, doctors you visit, and lawyers you consult. QR-SHIELD's privacy isn't a feature; it's the architecture.

### Technical Highlights

| Feature | Implementation |
|---------|----------------|
| **Ensemble ML** | 3-model architecture (Logistic Regression + Gradient Boosting + Decision Rules) |
| **Dynamic Brand Discovery** | Pattern-based detection for unknown brands beyond 500+ static entries |
| **25+ Heuristics** | Homograph attacks, suspicious TLDs, typosquatting, redirect patterns |
| **5 Platforms** | Android, iOS, Desktop, Web (JS + Wasm) with 80%+ shared code |
| **Adversarial Defense** | RTL override, double encoding, zero-width character detection |

---

## Why Kotlin Multiplatform

KMP allowed me to write the detection engine **once** and deploy everywhere. When I fix a phishing pattern, all platforms get the fix immediately. No drift. No reimplementation.

**Code Sharing:**
- 100% shared: Detection engine, ML scoring, brand detection
- 80% overall: Including repository, models, text generation
- Platform-specific: Camera APIs (unavoidable), UI rendering

---

## What I Learned

Building QR-SHIELD taught me that **privacy and protection coexist**. It taught me that security is about raising attack costs, not achieving perfection. And it showed me the power of Kotlin Multiplatform for real-world applications.

The ML model failed 3 times before working. The ComBank false positive crisis led to the entire BrandDetector module. Each failure made the final product stronger.

---

## Technologies Used

- **Kotlin 2.3.0** with Coroutines/Flow
- **Compose Multiplatform** (Android, Desktop, iOS hybrid)
- **SwiftUI** (native iOS camera/UI)
- **Kotlin/JS** (Web target)
- **Detekt + Ktlint** (zero-tolerance lint policy)
- **89% test coverage**, 1,248+ tests, property-based testing

---

## Impact

- **Apache 2.0 open source** — fully auditable
- **100% offline** — zero data collection
- **87.1% F1 score** on phishing detection
- **<5ms analysis** — 10x faster than targets
- **Published Red Team Corpus** — 60+ adversarial test cases for the community

---

## Why I Should Win

I didn't just build an app—I built something that protects people like my grandmother from attacks they can't see coming. The privacy-first architecture is a deliberate ethical choice. The ensemble ML demonstrates technical depth. The 5-platform KMP deployment proves engineering excellence.

**What Munich means to me:** A chance to share this work, learn from the best Kotlin developers worldwide, and bring that knowledge back to mentor the next generation.

**Scan smart. Stay protected.** 🛡️

---

*Word count: ~550*

- **GitHub:** [github.com/Raoof128](https://github.com/Raoof128)
- **Project:** [QR-SHIELD](https://github.com/Raoof128/Raoof128.github.io)
- **Live Demo:** [raoof128.github.io](https://raoof128.github.io/)
