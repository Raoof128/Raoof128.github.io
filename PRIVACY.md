# 🔒 Privacy Policy

> **QR-SHIELD is designed with privacy as architecture, not as a feature.**

---

## 📋 Summary

| Data Type | Collected? | Stored? | Transmitted? |
|-----------|------------|---------|--------------|
| Scanned URLs | ✅ Analyzed | 📱 Local only | ❌ Never |
| Scan history | ✅ Optional | 📱 Local SQLite | ❌ Never |
| Device info | ❌ No | ❌ No | ❌ Never |
| Analytics | ❌ No | ❌ No | ❌ Never |
| Crash reports | ❌ No | ❌ No | ❌ Never |

---

## 🛡️ Data We Never Collect

**QR-SHIELD makes zero network calls during operation.** This is enforced by architecture:

1. **No HTTP client** in the analysis module
2. **No telemetry SDK** included in dependencies
3. **No crash reporting** services (Firebase, Sentry, etc.)
4. **No analytics** of any kind (Google Analytics, Mixpanel, etc.)

### Why This Matters

URLs you scan reveal sensitive information:
- Which banks you use
- Which doctors you visit
- Which lawyers you consult
- Where you park, shop, and eat
- Your travel patterns

Cloud scanners build profiles they can sell, be subpoenaed for, or leak. **QR-SHIELD cannot leak what it never collects.**

---

## 📱 Data We Store Locally

### Scan History (Optional)

| Field | Storage | Retention |
|-------|---------|-----------|
| URL | SQLDelight database | Until user clears |
| Verdict | SQLDelight database | Until user clears |
| Timestamp | SQLDelight database | Until user clears |
| Score | SQLDelight database | Until user clears |

**Location:** App-private storage on device (not accessible to other apps)

**User Control:**
- View history: In-app history screen
- Delete individual: Swipe to delete
- Clear all: Settings → Clear History

### Settings (Preferences)

| Setting | Purpose |
|---------|---------|
| Theme preference | Light/Dark mode |
| Haptic feedback | Enable/disable vibration |
| Sound feedback | Enable/disable sounds |

**Location:** Platform preferences (SharedPrefs, UserDefaults, etc.)

---

## 🔐 Data Security

### Storage

- All data stored in app-private directories
- Not accessible to other applications
- Encrypted at rest by platform (iOS Data Protection, Android Keystore)

### In Transit

**Not applicable.** QR-SHIELD makes zero network calls.

### At Rest

| Platform | Protection |
|----------|------------|
| Android | App sandbox + optional device encryption |
| iOS | Data Protection API (encrypted by default) |
| Desktop | OS-level user directory permissions |
| Web | Browser localStorage (same-origin policy) |

---

## 🚫 Third-Party Services

QR-SHIELD uses **zero** third-party services:

| Service Type | Status |
|--------------|--------|
| Cloud APIs | ❌ Not used |
| Analytics | ❌ Not used |
| Crash reporting | ❌ Not used |
| Advertising | ❌ Not used |
| Social login | ❌ Not used |
| Push notifications | ❌ Not used |

---

## 📜 Data Retention

| Data Type | Retention Period |
|-----------|------------------|
| Scan history | Until user deletes |
| Settings | Until app uninstalled |
| Cached data | None (no caching) |

**On Uninstall:** All local data is deleted by the operating system.

---

## 👤 Your Rights

Since we don't collect any data:
- **Right to access:** Your data is on your device; you can view it anytime
- **Right to delete:** Clear history from Settings, or uninstall the app
- **Right to portability:** Export scan history as JSON from the app
- **Right to opt-out:** Nothing to opt out of—we collect nothing

---

## 🧒 Children's Privacy

QR-SHIELD does not knowingly collect any information from anyone, including children under 13. Since we collect zero data, there are no special provisions needed.

---

## 📞 Contact

Questions about this privacy policy:
- **GitHub Issues:** [Report an issue](https://github.com/Raoof128/Raoof128.github.io/issues)
- **Email:** (Include in repository contact info)

---

## 📅 Changes to This Policy

Any changes to this privacy policy will be reflected in this document with an updated date.

**Last Updated:** December 18, 2025

---

## 🏛️ Summary

> **We can't leak your data because we never have it.**
>
> QR-SHIELD performs all analysis on your device. URLs never leave your phone. We have no servers, no databases, no way to know what you scan.
>
> Privacy isn't a feature we added—it's the architecture.
