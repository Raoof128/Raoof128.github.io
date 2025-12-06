# QR-SHIELD UI/UX Design System

## Design Principles

1. **Clarity First**: Risk information must be immediately understandable
2. **Trust Through Transparency**: Show analysis details, not just verdicts
3. **Accessible**: WCAG 2.1 AA compliant
4. **Cross-Platform Consistency**: Same experience everywhere

---

## Typography

### Font Stack

```
Primary: Inter (San Francisco on iOS, Roboto on Android fallback)
Monospace: JetBrains Mono (for URLs)
```

### Scale

| Style | Size | Weight | Use |
|-------|------|--------|-----|
| displayLarge | 40sp | Bold | Hero score display |
| headlineMedium | 20sp | SemiBold | Screen titles |
| bodyLarge | 16sp | Regular | Primary content |
| labelMedium | 12sp | Medium | Chips, captions |
| urlText | 13sp | Mono | URL display |

---

## Color Palette

### Primary Brand

- **Primary**: `#6C5CE7` (Deep Purple)
- **Secondary**: `#00CEC9` (Teal Accent)

### Semantic - Verdicts

| Verdict | Main | Light | Background |
|---------|------|-------|------------|
| Safe | `#00D68F` | `#00F5A0` | `#00D68F1A` |
| Warning | `#FFAA00` | `#FFBB33` | `#FFAA001A` |
| Danger | `#FF3D71` | `#FF6B8A` | `#FF3D711A` |

### Backgrounds

| Element | Dark Mode | Light Mode |
|---------|-----------|------------|
| Background | `#0D1117` | `#F6F8FA` |
| Surface | `#161B22` | `#FFFFFF` |
| Card | `#1C2128` | `#FFFFFF` |

---

## Components

### Risk Score Display

```
┌─────────────────────────────┐
│                             │
│          [ 72 ]             │  ← Risk score (giant)
│       SUSPICIOUS            │  ← Verdict label
│           ⚠️                │  ← Emoji indicator
│                             │
└─────────────────────────────┘

Animation: Score counts up from 0
Color: Transitions based on final score
```

### Alert Card

```
┌─────────────────────────────┐
│ ⚠️ Brand Impersonation      │
│ ─────────────────────────── │
│ This URL mimics: PayPal     │
│                             │
│ The domain "paypa1.com"     │
│ contains a typosquat of     │
│ the official brand.         │
└─────────────────────────────┘
```

### History List Item

```
┌─────────────────────────────┐
│ 🟢 google.com               │
│    Score: 8 • 2 min ago     │
├─────────────────────────────┤
│ 🟡 bit.ly/3xYz              │
│    Score: 45 • 1 hour ago   │
├─────────────────────────────┤
│ 🔴 paypa1-secure.tk         │
│    Score: 87 • Yesterday    │
└─────────────────────────────┘
```

---

## Animations

### Scan Pulse
- Pulsing ring around camera viewfinder
- Smooth 60fps
- Subtle glow effect

### Score Reveal
- Count-up from 0 to final score
- Duration: 800ms ease-out
- Color transition during count

### Verdict Badge
- Bouncy spring entrance
- Scale: 0 → 1.1 → 1.0
- Duration: 400ms

### Navigation
- Shared element transitions
- Fade through black for screen changes
- 300ms duration

---

## Spacing System

| Token | Value | Use |
|-------|-------|-----|
| xxs | 4dp | Inline spacing |
| xs | 8dp | Component padding |
| sm | 12dp | Card content padding |
| md | 16dp | Section spacing |
| lg | 24dp | Screen padding |
| xl | 32dp | Major sections |

---

## Accessibility

- Minimum touch target: 48x48dp
- Color contrast ratio: 4.5:1+
- Screen reader labels for all interactive elements
- Reduced motion support
- Large text support
