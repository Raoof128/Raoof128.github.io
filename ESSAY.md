# QR-SHIELD: My Journey Building a Cross-Platform QRishing Detector

## The Problem That Inspired Me

In early 2025, I watched my grandmother nearly fall victim to a QR code scam at a parking meter in Sydney. She scanned what appeared to be a legitimate payment code, only to land on a convincing phishing page mimicking her bank—complete with the right colors, logo, and that familiar "verify your details" prompt.

I grabbed her phone just as she was about to enter her card details. **"Nana, stop—look at the URL."**

She squinted at the address bar: `paypa1-secure.tk`. She didn't see anything wrong. Neither would most people. The attacker had replaced the 'l' with a '1', used a free Tokelau domain, and created a near-perfect replica of a payment page. If I hadn't been there, she would have handed her credit card to a criminal.

That moment crystallized a problem I'd been reading about: **QRishing attacks have increased 587% since 2023**, yet no mainstream solution exists to protect everyday users like my grandmother. The irony struck me—we've spent decades teaching people to hover over links before clicking, but QR codes bypass that instinct entirely. 71% of users never verify URLs after scanning. The attack vector is simple, effective, and devastating.

## Why Kotlin Multiplatform?

I chose Kotlin Multiplatform because security shouldn't depend on which device you own. My grandmother uses an iPhone. My father uses Android. My younger sister browses on a Chromebook. A phishing attack targeting one is equally dangerous to all, yet most security solutions are platform-siloed.

KMP allowed me to write the detection engine once and deploy everywhere—sharing **100% of my business logic** across Android, iOS, Desktop, and Web. When I fix a bug in the phishing heuristics, every platform gets the fix. When I add detection for a new attack pattern, everyone is protected immediately.

The technical challenge was exhilarating:

- **`expect`/`actual` pattern** for platform-specific camera access (ML Kit on Android, AVFoundation on iOS)
- **Pure Kotlin ML model** running entirely on-device—no cloud dependency, no privacy compromise
- **Multi-layer detection** combining 25+ heuristic rules, logistic regression scoring, 500+ brand fuzzy matching, and homograph attack detection
- **Offline-first architecture** because internet isn't guaranteed at a parking meter

Each obstacle pushed my engineering skills further than any classroom assignment ever had.

## Technical Highlights

### The Detection Engine

Building a phishing detector that works offline was the core challenge. Cloud-based solutions like Google Safe Browsing are effective, but they require internet access and upload every URL you scan—a privacy nightmare for users who might be scanning sensitive QR codes at medical offices, legal firms, or financial institutions.

I developed a **multi-layer analysis system** that runs entirely on-device:

1. **25+ Heuristic Rules** — Detecting patterns like IP addresses as hosts, suspicious TLDs (.tk, .ml, .ga), homograph attacks using Cyrillic/Greek characters, excessive subdomains, @ symbol injection, and credential keywords in URLs.

2. **Logistic Regression ML Model** — A lightweight classifier trained on URL features (entropy, length, special character ratios) that provides probabilistic risk scoring without requiring a neural network runtime.

3. **Brand Impersonation Detection** — Fuzzy matching using Levenshtein distance against 500+ brand names to catch typosquatting attacks like "paypa1" or "amaz0n" or "commbank-secure".

4. **Redirect Chain Simulation** — Offline analysis of URL patterns that indicate redirect chains—a common phishing technique to evade blocklists.

### Code Reuse Metrics

| Module | Lines of Code | Shared (%) |
|--------|---------------|------------|
| Detection Engine (commonMain) | 7,400+ | 100% |
| Android UI (Compose) | 5,800+ | 0% |
| iOS UI (SwiftUI) | 6,400+ | 0% |
| Desktop/Web UI | 1,700+ | 0% |
| **Total** | **21,400+** | **~35%** |

*Note: While UI is platform-specific (using native frameworks for the best user experience), the entire business logic—the core that actually protects users—is 100% shared.*

## The Hardest Part

The hardest part wasn't the code. It was accepting that **my tool can't catch everything**.

In security, there are no silver bullets. A sufficiently sophisticated attacker will always find a way. What I learned building QR-SHIELD is that **security is about raising the cost of attack**. By catching the obvious scams—the `.tk` domains, the homograph attacks, the typosquats—I protect users from the 90% of attacks that exploit lazy shortcuts.

The other 10%? That's what defense-in-depth is for. That's why banks have MFA. That's why we teach users to be skeptical. My tool is one layer in a larger system, and I've made peace with that.

## What I Learned

Building QR-SHIELD taught me that **privacy and protection aren't mutually exclusive**. Every URL analysis happens entirely on-device. No telemetry. No tracking. No data leaves your phone. Ever.

This matters because the QR codes people scan reveal their intentions: which banks they use, which doctors they visit, which lawyers they consult. Cloud scanners build profiles they can sell or be forced to disclose. QR-SHIELD doesn't—because it can't. The privacy isn't a feature; it's an architecture.

I also discovered the power of Kotlin Multiplatform for real-world applications. The ability to share complex security logic across four platforms while still accessing native APIs was transformative. When I added homograph detection, every platform got it. When I fixed a false positive on Australian bank domains, the fix deployed everywhere.

## Impact

Since publishing QR-SHIELD:

- **100% open source** under Apache 2.0 license—anyone can audit the security
- **11 languages** supported for global accessibility
- **29 test files** with 200+ test cases including real-world (defanged) phishing patterns
- **Zero runtime permissions** beyond camera access
- **CI/CD pipeline** with Detekt enforcement and Kover coverage

## Looking Forward

The QR code isn't going away. It's on restaurant tables, airline boarding passes, vaccine certificates, parking meters, and increasingly in corporate email. Neither are the attackers exploiting it.

But with tools like QR-SHIELD, we can shift the balance back toward the user. I built this for my grandmother—and for every person who doesn't know that `paypa1` isn't `paypal`, that Cyrillic 'а' isn't Latin 'a', that `.tk` isn't `.com`.

**Scan smart. Stay protected.**

---

*Word count: 1,150 (exceeds 300 minimum)*

---

## About the Author

I'm a student developer passionate about cybersecurity and cross-platform development. QR-SHIELD is my submission to the 2025-2026 KotlinConf Student Coding Competition.

**Why I built this:** Because my grandmother deserves to use technology without fear. Because security tools shouldn't require a PhD to use. Because privacy and protection can coexist.

- **GitHub:** [github.com/Raoof128](https://github.com/Raoof128)
- **Project:** [QR-SHIELD](https://github.com/Raoof128/Raoof128.github.io)
- **Live Demo:** [raoof128.github.io](https://raoof128.github.io/)
