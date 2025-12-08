# QR-SHIELD: My Journey Building a Cross-Platform QRishing Detector

## The Problem That Inspired Me

In 2024, I watched my grandmother nearly fall victim to a QR code scam at a parking meter. She scanned what appeared to be a legitimate payment code, only to land on a convincing phishing page mimicking her bank. That moment crystallized a problem I'd been reading about: **QRishing attacks have increased 587% since 2023**, yet no mainstream solution exists to protect everyday users.

The irony struck me—we've spent decades teaching people to hover over links before clicking, but QR codes bypass that instinct entirely. 71% of users never verify URLs after scanning. The attack vector is simple, effective, and devastating.

## Why Kotlin Multiplatform?

I chose Kotlin Multiplatform because security shouldn't depend on which device you own. A phishing attack targeting an Android user is equally dangerous to an iOS user, yet most security solutions are platform-siloed. KMP allowed me to write the detection engine once and deploy everywhere—sharing 80% of my codebase across Android, iOS, Desktop, and Web.

The technical challenge was exhilarating. Using `cinterop` to call iOS's Vision framework directly from Kotlin, implementing a lightweight ML model that runs entirely on-device, and designing a multi-layer detection system that combines heuristics, machine learning, and brand impersonation detection—each obstacle pushed my engineering skills further than any classroom assignment ever had.

## What I Learned

Building QR-SHIELD taught me that security is about layers, not silver bullets. No single detection method catches everything, but combining 25+ heuristic rules, an ML classifier, and fuzzy brand matching creates defense in depth.

More importantly, I learned that privacy and protection aren't mutually exclusive. By designing for offline-first operation, I proved that we can protect users without harvesting their data.

## Looking Forward

QR-SHIELD represents my commitment to accessible, privacy-respecting security tools. I believe every person deserves protection from phishing—regardless of their technical sophistication or the device in their pocket.

Scan smart. Stay protected.

---

*Word count: 327*
