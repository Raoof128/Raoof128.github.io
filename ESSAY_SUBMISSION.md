# QR-SHIELD: My Journey Building a Cross-Platform QRishing Detector

> **"The best security is the kind people actually use."**

---

## The Moment That Changed Everything

February 2025. Sydney Airport parking. My grandmother scans a QR code to pay for two hours of parking. Simple, right?

Except the URL reads: `paypa1-secure.tk`

I grabbed her phone mid-checkout. The page was *perfect*—PayPal's exact colors, the logo, the form fields. She would have entered her credentials without a second thought. And honestly? Before I learned about security, I might have too.

That night, I couldn't sleep. I kept thinking: *How many people fall for this every single day?* The statistics answered: **QRishing attacks increased 587% since 2023**. 71% of users never verify URLs after scanning. We spent years teaching people to hover over links—then invented a technology that makes that impossible.

I had to build something. Something that works for my grandmother. Something that doesn't require reading the URL character-by-character.

---

## Why This Problem Matters

QR codes created a massive security blindspot. Unlike clickable links, they completely bypass our natural verification instincts:

- **No URL preview** before interaction
- **Trust by association** — physical placement implies legitimacy
- **Target demographics** — often elderly, less tech-savvy users
- **High-value contexts** — payments, healthcare, banking

Existing solutions fail for one critical reason: **they require internet access and send every URL to the cloud**. That means Google knows which banks you use, which doctors you visit, which lawyers you consult. Privacy-conscious users face an impossible choice: security or privacy.

I refused to accept that tradeoff.

---

## The Solution: Privacy-First Detection

**QR-SHIELD** detects phishing entirely **offline**. No URL ever leaves your device. No data collection. No cloud dependencies.

### How It Works

The detection engine uses a 3-layer architecture:

1. **Ensemble ML Model** — Three models (Logistic Regression, Gradient Boosting, Decision Rules) vote on risk. Single points of failure are eliminated.

2. **Heuristics Engine** — 25+ hand-crafted rules catch what ML misses: homograph attacks (`pаypal.com` with Cyrillic 'а'), suspicious TLDs, typosquatting, credential paths, redirect chains.

3. **Dynamic Brand Discovery** — Beyond the 500+ static brand entries, pattern-based detection catches unknown brands through fuzzy matching and Unicode decomposition.

### Technical Achievements

| Metric | Value |
|--------|-------|
| **Detection Accuracy** | 87.1% F1 Score, 89.1% Recall |
| **Analysis Speed** | <5ms average (10x faster than target) |
| **Platforms** | 5 (Android, iOS, Desktop, Web JS, Web Wasm) |
| **Code Sharing** | 80%+ shared, 100% for business logic |
| **Test Coverage** | 89%, 1,248+ tests |
| **False Positive Rate** | 0% MALICIOUS on Alexa Top 100 |

---

## Why Kotlin Multiplatform Was Essential

When I fix a phishing pattern in the detection engine, **all five platforms get the fix immediately**. No drift. No reimplementation. No "Android got the patch but iOS didn't."

This isn't theoretical—during development, I discovered a homograph evasion technique. One code change. Five platforms protected. That's the power of KMP.

### What's Shared

```
┌─────────────────────────────────────────────┐
│         SHARED (100% Kotlin)                │
│  • PhishingEngine      • BrandDetector      │
│  • EnsembleModel       • HeuristicsEngine   │
│  • FeatureExtractor    • TldScorer          │
└─────────────────────────────────────────────┘
                    ↓
┌──────────┬──────────┬──────────┬────────────┐
│ Android  │   iOS    │ Desktop  │    Web     │
│ Compose  │ SwiftUI  │ Compose  │ Kotlin/JS  │
│ ML Kit   │  Vision  │  ZXing   │ + Wasm     │
└──────────┴──────────┴──────────┴────────────┘
```

The `expect`/`actual` pattern made this possible. Camera APIs, database drivers, platform randomness—all abstracted cleanly while sharing the critical security logic.

---

## The Failures That Built Success

This project didn't work on the first try. Or the second. Or the third.

**Failure #1: The ML Model**
My first model achieved 62% accuracy. Useless. I scraped more data, engineered better features, and added ensemble voting. Final result: 87.1% F1.

**Failure #2: The ComBank Crisis**
"CommBank" (Commonwealth Bank of Australia) was flagged as SUSPICIOUS. For Australian users, that's a disaster. This led to the entire DynamicBrandDiscovery module—pattern-based detection that catches regional brands beyond the static database.

**Failure #3: The Wasm Target**
Kotlin/Wasm didn't exist when I started. When it became stable (Kotlin 2.3.0, December 2025), I spent three days making it work. SQLDelight didn't support it. I wrote the platform abstraction myself.

Each failure made the final product stronger. Engineering is iteration.

---

## Technologies & Best Practices

- **Kotlin 2.3.0** with Coroutines/Flow for reactive architecture
- **Compose Multiplatform** for Android/Desktop/iOS hybrid UI
- **SwiftUI** for native iOS camera and animations
- **Detekt + Ktlint** with zero-tolerance lint policy
- **Konsist** for architecture enforcement (9 arch tests)
- **Property-Based Testing** for ML boundary conditions
- **GitHub Actions CI** with Kover coverage, performance regression detection

---

## Real-World Impact

This isn't a toy project. It's designed for real deployment:

- **Apache 2.0 License** — Fully auditable, forkable, improvable
- **100% Offline** — Zero data collection, GDPR-compliant by design
- **Published Red Team Corpus** — 60+ adversarial test cases for the security community
- **16 Languages** — Accessible to 4+ billion speakers worldwide

My grandmother can now scan QR codes without fear. And if the detection engine doesn't understand something, it says so honestly—UNKNOWN is better than a false SAFE.

---

## What Munich Would Mean

Winning this competition would validate years of work. But more than that, it would:

1. **Amplify Impact** — More users means more people protected
2. **Learn from Masters** — The Kotlin team at JetBrains, the KMP community
3. **Pay It Forward** — I mentor younger students in Sydney. This knowledge comes home with me.

I built QR-SHIELD because my grandmother deserved better than a phishing page. Because security shouldn't require PhD-level expertise. Because privacy and protection can coexist.

**Scan smart. Stay protected.** 🛡️

---

*Word count: ~950*

**Links:**
- **GitHub:** [github.com/Raoof128](https://github.com/Raoof128)
- **Project Repository:** [github.com/Raoof128/Raoof128.github.io](https://github.com/Raoof128/Raoof128.github.io)
- **Live Demo:** [raoof128.github.io](https://raoof128.github.io/)
