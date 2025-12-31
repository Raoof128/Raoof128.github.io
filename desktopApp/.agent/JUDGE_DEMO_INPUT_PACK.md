# 🎯 Judge Demo Input Pack

**Purpose:** Provide 3 safe sample inputs for judges to verify the Desktop scan flow works end-to-end without network dependencies.

---

## Quick Start for Judges

```bash
# 1. Clone and build
git clone https://github.com/<repo>/mehrguard
cd mehrguard
./gradlew :desktopApp:compileKotlinDesktop

# 2. Run Desktop app
./gradlew :desktopApp:run

# 3. Once app launches, test inputs below
```

---

## 🧪 Test Input Pack (3 Samples)

### Input 1: BENIGN URL (Expected verdict: ✅ SAFE)

**URL to paste:**
```
https://www.google.com/search
```

**How to test:**
1. Copy the URL above
2. Press `Cmd/Ctrl+V` in the app
3. Observe: Green "Safe to Visit" result screen

**Expected behavior:**
- Verdict: `SAFE`
- Risk Score: 0-15
- Confidence: >90%
- No brand impersonation flags

---

### Input 2: PHISHING-LIKE URL (Expected verdict: 🔴 MALICIOUS)

**URL to paste:**
```
http://secure-login.paypa1-verify.com/update/credentials
```

**How to test:**
1. Copy the URL above
2. Press `Cmd/Ctrl+V` in the app
3. Observe: Red "Danger Detected" result screen

**Expected behavior:**
- Verdict: `MALICIOUS`
- Risk Score: 70-100
- Flags detected:
  - HTTP (not HTTPS) = insecure
  - Brand impersonation: "PayPal" misspelled as "paypa1"
  - Login/credentials in path
  - Suspicious hyphenated domain structure

---

### Input 3: QR PAYLOAD TEXT (Expected verdict: ⚠️ SUSPICIOUS)

**URL to paste:**
```
https://bit.ly/secure-doc-verify
```

**How to test:**
1. Copy the URL above  
2. Press `Cmd/Ctrl+V` in the app
3. Observe: Orange/Yellow "Suspicious Activity" result screen

**Expected behavior:**
- Verdict: `SUSPICIOUS`
- Risk Score: 40-65
- Flags detected:
  - URL shortener detected (bit.ly)
  - Cannot verify destination
  - Redirect chain warning

---

## 🖼️ Image File Testing

To test QR code image scanning:

1. Press `Cmd/Ctrl+I` or click "Upload Image"
2. Select any QR code image containing a URL
3. The engine will decode the QR and analyze the embedded URL

**Note:** Use any QR generator to create test QR codes with the URLs above.

---

## 📋 All Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Cmd/Ctrl+V` | Paste URL from clipboard & analyze |
| `Cmd/Ctrl+I` | Import QR image file |
| `Cmd/Ctrl+1` | Go to Dashboard |
| `Cmd/Ctrl+2` | Go to Live Scan |
| `Cmd/Ctrl+3` | Go to Scan History |
| `Cmd/Ctrl+4` | Go to Training ("Beat the Bot") |
| `Cmd/Ctrl+,` | Open Settings |
| `Escape` | Go back from result screens |

---

## ✅ Verification Checklist for Judges

- [ ] App launches without errors
- [ ] Input 1 (google.com) → SAFE verdict
- [ ] Input 2 (paypa1-verify.com) → MALICIOUS verdict
- [ ] Input 3 (bit.ly shortener) → SUSPICIOUS verdict
- [ ] QR image import works (Cmd+I)
- [ ] History records all scans
- [ ] Export CSV works
- [ ] Training game loads and responds to keyboard (P/L keys)

---

## 🔒 Offline Guarantee

All 3 inputs above work **100% offline**:
- No network API calls
- No external database lookups
- Pure local heuristics + ML inference
- Works on airplane mode

The PhishingEngine runs entirely in-process using ZXing (QR decode) + pure Kotlin detection logic.

---

## Build Commands Reference

```bash
# Compile only (fast, ~5s)
./gradlew :desktopApp:compileKotlinDesktop

# Build + Run
./gradlew :desktopApp:run

# Run shared engine tests
./gradlew :common:desktopTest

# Create native distribution
./gradlew :desktopApp:packageDistributionForCurrentOS
```

---

**Last Updated:** December 30, 2025
**Version:** 1.20.3
