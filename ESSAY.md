# Mehr Guard: Privacy-First QR Phishing Detection

Mehr Guard started with a near-miss. My mother scanned a QR code on a parking meter and landed on a page that looked legitimate enough to fool anyone. As a cybersecurity and AI student at Macquarie University in Sydney, I wanted a tool that makes QR scams harder to pull off without requiring people to think like a SOC analyst.

Mehr Guard is an offline-first, privacy-preserving QR phishing detector built with Kotlin Multiplatform. I chose KMP because security logic should not fork across platforms. The same detection engine runs in commonMain and ships to five targets: Android, iOS, Desktop, Web (JS) and Web (Wasm). A suspicious URL gets the same analysis everywhere, closing platform gaps attackers love.

The shared core produces an explainable verdict (SAFE, SUSPICIOUS, MALICIOUS) using a layered scoring pipeline. First, a heuristics engine emits reason codes for 25 checks including homograph risk, IP and encoding obfuscation, and risky TLD patterns. Second, a lightweight on-device ensemble model (Logistic Regression, Gradient Boosting, and decision stumps) adds behavioural signal with zero network calls. Third, redirect simulation and brand-pattern analysis help spot lookalike login pages. The UI shows a risk score and reason codes.

Detection is only half the win. QR scams thrive on human autopilot, so Mehr Guard includes Beat the Bot, a training arena. Users see realistic QR destinations, make a call, then instantly compare against the engine's reasoning and practical tips. It turns awareness into reps: learn patterns, get faster, and build confidence without shame.

For judges and researchers, a hidden Red Team Developer Mode (enabled via a 7-tap trigger) loads 19 adversarial scenarios to stress-test the engine. Production quality matters: 16-language localisation, 1,000+ automated tests, and Konsist architecture rules keep the codebase honest and reproducible. Mehr Guard proves strong security can be private, portable, and genuinely usable for everyday users too.

---

**Author:** Mohammad Raouf Abedini  
**University:** Macquarie University, Sydney, Australia  
**Email:** raoof.r12@gmail.com  
**Word Count:** ~300 words
