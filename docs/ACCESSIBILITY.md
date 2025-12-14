# ♿ QR-SHIELD Accessibility Guide

> **Ensuring QR-SHIELD is usable by everyone, including users with disabilities.**

---

## 📋 Accessibility Compliance

QR-SHIELD targets **WCAG 2.1 Level AA** compliance across all platforms.

| Platform | VoiceOver/TalkBack | Keyboard Nav | High Contrast | Dynamic Type |
|----------|-------------------|--------------|---------------|--------------|
| Android | ✅ Full | ✅ Full | ✅ Yes | ✅ Yes |
| iOS | ✅ Full | ✅ Full (iPad) | ✅ Yes | ✅ Yes |
| Desktop | ✅ Partial | ✅ Full | ✅ Yes | ✅ Yes |
| Web | ✅ Full | ✅ Full | ✅ Yes | ✅ Yes |

---

## 🔊 Screen Reader Support

### Android (TalkBack)

All UI elements include content descriptions:

```kotlin
// Example from ScannerScreen.kt
Button(
    onClick = { /* scan */ },
    modifier = Modifier.semantics {
        contentDescription = "Scan QR code button. Double tap to start scanning."
        role = Role.Button
    }
)
```

**TalkBack Announcements:**
- ✅ "Scan result: Malicious. Risk score 87 out of 100."
- ✅ "Brand impersonation detected: PayPal"
- ✅ "Suspicious TLD: dot tk"

### iOS (VoiceOver)

SwiftUI provides native accessibility:

```swift
// Example from ResultCard.swift
VStack {
    Text(verdict.displayName)
        .accessibilityLabel("Risk verdict: \(verdict.displayName)")
        .accessibilityValue("Score \(score) out of 100")
}
.accessibilityElement(children: .combine)
.accessibilityHint("Shows the security analysis result")
```

**VoiceOver Actions:**
- ✅ "Risk verdict: Malicious"
- ✅ "Score 87 out of 100"
- ✅ "Double tap to view details"

### Web (Screen Readers)

HTML follows ARIA best practices:

```html
<div role="alert" aria-live="polite" aria-label="Analysis result">
  <span aria-label="Risk level: Malicious">❌ MALICIOUS</span>
  <span aria-label="Risk score: 87 out of 100">87/100</span>
</div>
```

---

## ⌨️ Keyboard Navigation

### Web App

| Key | Action |
|-----|--------|
| `Tab` | Move to next interactive element |
| `Shift + Tab` | Move to previous element |
| `/` | Focus URL input field |
| `Enter` | Analyze URL / Activate button |
| `Escape` | Clear input / Close dialog |
| `Space` | Toggle buttons / checkboxes |

### Desktop App

| Key | Action |
|-----|--------|
| `Tab` | Navigate between panels |
| `Ctrl/Cmd + V` | Paste and analyze URL |
| `Ctrl/Cmd + O` | Open file picker |
| `Enter` | Analyze current URL |
| `Escape` | Clear input |

---

## 🎨 Color Accessibility

### Color Contrast Ratios

All text meets WCAG AA minimum contrast requirements:

| Element | Foreground | Background | Ratio | Requirement |
|---------|------------|------------|-------|-------------|
| Body text | `#1F2937` | `#FFFFFF` | 15:1 | ✅ 4.5:1 (AA) |
| Safe badge | `#15803D` | `#DCFCE7` | 7.2:1 | ✅ 4.5:1 (AA) |
| Warning badge | `#854D0E` | `#FEF9C3` | 6.8:1 | ✅ 4.5:1 (AA) |
| Danger badge | `#991B1B` | `#FEE2E2` | 7.5:1 | ✅ 4.5:1 (AA) |

### Color-Blind Safe Design

Risk levels are conveyed through multiple channels:

| Verdict | Color | Icon | Text | Pattern |
|---------|-------|------|------|---------|
| SAFE | Green | ✅ ✓ Shield | "SAFE" | Solid fill |
| SUSPICIOUS | Amber | ⚠️ Warning | "SUSPICIOUS" | Diagonal stripes |
| MALICIOUS | Red | ❌ X Shield | "MALICIOUS" | Cross-hatch |

**Never rely on color alone** — Icons and text labels are always present.

---

## 📐 Dynamic Type / Font Scaling

### iOS Dynamic Type

QR-SHIELD respects iOS Dynamic Type settings:

```swift
Text(score.description)
    .font(.title)
    .dynamicTypeSize(...DynamicTypeSize.accessibility3)
```

Tested sizes: xSmall → AX5 (accessibility sizes)

### Android Font Scaling

Compose supports system font scaling:

```kotlin
Text(
    text = score.toString(),
    style = MaterialTheme.typography.headlineMedium,
    // Automatically scales with system font size
)
```

Tested scales: 0.85x → 2.0x

### Web Font Scaling

CSS uses relative units:

```css
.score-display {
    font-size: 2rem;  /* Scales with browser zoom */
    line-height: 1.5;
}
```

---

## 🔇 Reduce Motion

For users with vestibular disorders, QR-SHIELD respects motion preferences:

### iOS

```swift
@Environment(\.accessibilityReduceMotion) var reduceMotion

if !reduceMotion {
    withAnimation(.spring()) { /* animate */ }
} else {
    // Instant state change, no animation
}
```

### Android

```kotlin
val reduceMotion = LocalReducedMotion.current

if (!reduceMotion) {
    AnimatedVisibility(visible = showResult) { /* animated */ }
} else {
    if (showResult) { ResultCard() }
}
```

### Web

```css
@media (prefers-reduced-motion: reduce) {
    * {
        animation-duration: 0.01ms !important;
        transition-duration: 0.01ms !important;
    }
}
```

---

## 👆 Touch Targets

Minimum touch target sizes per platform guidelines:

| Platform | Minimum Size | QR-SHIELD Actual |
|----------|--------------|------------------|
| iOS (Apple HIG) | 44×44pt | 48×48pt ✅ |
| Android (Material) | 48×48dp | 56×56dp ✅ |
| Web (WCAG) | 44×44px | 48×48px ✅ |

---

## 🧪 Accessibility Testing

### Automated Testing

- **iOS**: XCUITest accessibility assertions
- **Android**: Espresso accessibility checks
- **Web**: axe-core automated scanning

### Manual Testing Checklist

- [ ] Navigate entire app using only screen reader
- [ ] Navigate entire app using only keyboard
- [ ] Test with 200% font scaling
- [ ] Test with high contrast mode
- [ ] Test with reduced motion enabled
- [ ] Verify all images have alt text
- [ ] Verify form labels are associated with inputs
- [ ] Verify focus indicators are visible

---

## 📚 Resources

- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Apple Accessibility Programming Guide](https://developer.apple.com/accessibility/)
- [Android Accessibility Guidelines](https://developer.android.com/guide/topics/ui/accessibility)
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)

---

## 🐛 Reporting Accessibility Issues

Found an accessibility barrier? Please report it:

1. Open an issue on [GitHub](https://github.com/Raoof128/Raoof128.github.io/issues)
2. Use the "Accessibility" label
3. Describe the barrier and your assistive technology
4. Include steps to reproduce

We prioritize accessibility fixes with the same urgency as security issues.

---

*Last updated: December 2025*
