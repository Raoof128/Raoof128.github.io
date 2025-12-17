# QR-SHIELD: Cross-Platform QRishing Detection

> **"The UI intentionally exposes detection reasoning to avoid black-box security decisions."**

---

## The Spark: My Grandmother's Close Call

In early 2025, I watched my grandmother nearly fall victim to a QR code scam at a Sydney parking meter. She scanned what appeared to be a legitimate payment code, landing on a convincing phishing page mimicking her bank.

I grabbed her phone just as she was about to enter her card details. **"Nana, stop—look at the URL."**

She squinted at `paypa1-secure.tk`. She didn't see anything wrong. Neither would most people. The attacker had replaced 'l' with '1', used a free Tokelau domain, and created a near-perfect bank replica.

**That moment crystallized the problem:** QRishing attacks have skyrocketed, yet no mainstream solution protects everyday users like my grandmother—especially offline.

---

## The Solution: Offline-First Detection

QR-SHIELD performs all analysis **100% on-device**. No URL ever leaves your phone. The privacy isn't a feature—it's the architecture.

**Why offline?** Cloud scanners know which banks you use, which doctors you visit, which lawyers you consult. This data can be sold, subpoenaed, or leaked. QR-SHIELD can't leak what it never collects.

---

## Why Kotlin Multiplatform?

KMP let me write the detection engine once and deploy everywhere—sharing **100% of business logic** across Android, iOS, Desktop, and Web.

- **One bug fix** → All platforms protected
- **`expect`/`actual` pattern** → Native camera access (ML Kit, AVFoundation)
- **Ensemble ML** → 3 models combined for robust detection in <5ms

---

## Technical Highlights

| Feature | Implementation |
|---------|---------------|
| **25+ Heuristics** | Homographs, typosquats, @ injection, suspicious TLDs |
| **Ensemble ML** | Logistic Regression + Gradient Boosting + Decision Rules |
| **Brand Detection** | 500+ brands + Dynamic Discovery patterns |
| **<5ms Analysis** | Real-time feedback during scanning |

---

## What I Learned

Building QR-SHIELD taught me that **privacy and protection aren't mutually exclusive**. Every URL analysis happens entirely on-device. No telemetry. No tracking.

I also discovered that security is about **raising the cost of attack**. By catching 90% of scams—the `.tk` domains, the homographs, the typosquats—I protect users from lazy attackers. The other 10%? That's what defense-in-depth is for.

---

## About Me

I'm a Computer Science student from Sydney, passionate about cybersecurity and cross-platform development. I've been coding in Kotlin for 3+ years and participate in security CTF competitions.

**Why I built this:** Because my grandmother deserves to use technology without fear.

**What Munich means to me:** A chance to learn from the best Kotlin developers, share what I've built, and bring that knowledge back to the next generation.

---

*Word count: ~400*

- **GitHub:** [github.com/Raoof128](https://github.com/Raoof128)
- **Project:** [QR-SHIELD](https://github.com/Raoof128/Raoof128.github.io)
- **Live Demo:** [raoof128.github.io](https://raoof128.github.io/)
