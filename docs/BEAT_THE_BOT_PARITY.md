# Beat The Bot Parity Specification

> **Version**: 1.0.0  
> **Last Updated**: December 24, 2025  
> **Author**: Gemini 3 Pro (Cross-Platform Product Engineer)

## Overview

This document defines the cross-platform parity requirements for the "Beat The Bot" training feature across all 4 apps: Android, iOS, Desktop, and Web. It serves as the single source of truth for ensuring consistent user experience.

---

## 1. Screens & Entry Points

| Platform | Navigation Path | Entry Point Label | Route/Screen |
|----------|-----------------|-------------------|--------------|
| **Android** | Tab Bar → Training | "Beat the Bot" | `BeatTheBotScreen` |
| **iOS** | Tab Bar → Training | "Beat the Bot" | `BeatTheBotView` |
| **Desktop** | Sidebar → Beat the Bot | "Beat the Bot" | `TrainingScreen` |
| **Web** | Sidebar → Beat the Bot | "Beat the Bot" | `game.html` |

### Parity Action
- ✅ Desktop renamed "Training" to "Beat the Bot" in sidebar (all 18 languages)

---

## 2. State Model

### 2.1 Signal Definition

A **Signal** is a string identifier representing a detected threat indicator.

**Standard Signal Types:**
- `SUSPICIOUS_DOMAIN` - Domain structure anomalies
- `TYPOSQUATTING` - Misspelled brand names
- `HOMOGRAPH_ATTACK` - Character substitution (e.g., 0 for O)
- `RISKY_TLD` - Suspicious TLDs (.tk, .ml, .ga)
- `INSECURE_PROTOCOL` - HTTP without HTTPS
- `SUBDOMAIN_ABUSE` - Misleading subdomain structure
- `URL_SHORTENER` - Shortened URLs hiding destination
- `URGENCY_TACTIC` - Urgency language in message
- `BRAND_IMPERSONATION` - Fake brand presence

### 2.2 Active Set Logic

```kotlin
val active = detectedSignals.toSet()
```

Signals are considered "active" when:
1. The current challenge is identified as phishing
2. Analysis has been performed (after user decision)
3. The signals persist until the next round starts

### 2.3 Highlight Duration

- **Pulse Animation**: Continuous while active (2-second cycle)
- **Ripple Animation**: Triggered on activation, persists while active
- **Badges**: Visible until next round

### 2.4 Dedupe/Ordering Rules

- Signals are deduplicated using Set operations
- Display order: alphabetical by signal name
- Maximum display: 5 badges (overflow handled gracefully)

### 2.5 Empty/Idle Behavior

When `detectedSignals.isEmpty()`:
- Brain nodes pulse blue (safe color)
- No ripple effects
- No explanation badges
- Accessibility text: "No threats detected"

---

## 3. UI Components

### 3.1 Brain Visual Requirements

#### Node Grid Style
- **Node Count**: 80 nodes
- **Distribution**: Circular (rejection sampling, radius ≤ 1)
- **Base Node Radius**: 3dp/3pt/3px
- **Active Node Radius**: 6dp/6pt/6px
- **Layout Area**: 200dp height, full width

#### Cluster Mapping

```kotlin
// BrainMap: Map<Signal, List<NodeId>>
fun mapSignalsToNodes(signals: List<String>, nodeCount: Int): Set<Int>
```

- Each signal hashes to a "center" node index
- 8 neighboring nodes are added to the cluster
- Deterministic: same signals always light same nodes

#### Pulse Animation Rules

- **Cycle Duration**: 2000ms (2 seconds)
- **Phase**: `0 → 2π` using `sin(phase)`
- **Scale Factor**: `1.0 + 0.3 * sin(phase * 2 + nodePhaseOffset)`
- **Floating Offset**: `sin(phase + nodePhaseOffset) * 5`

#### Ripple Animation Rules

- **Radius**: `nodeRadius * 2.5`
- **Opacity**: 0.2 (20%)
- **Color**: Same as node color (danger red when active)

#### Explanation Badges Rules

- **Style**: Compact pill with border
- **Color**: Danger red text on 10% red background
- **Border**: 1px solid at 30% opacity
- **Typography**: Small caps, 0.75rem/12sp
- **Text Transform**: Replace underscores with spaces

### 3.2 Accessible Text Under Graphic

**Always present** (visually hidden for screen readers):

```text
// Idle
"AI Neural Net: No threats detected. Brain pattern is calm and blue."

// Active
"AI Neural Net: Active alert. Detected signals: {signals}. Brain pattern is pulsing red."
```

---

## 4. Copy & Severity

### 4.1 Badge Labels

| Signal | Display Label |
|--------|---------------|
| `SUSPICIOUS_DOMAIN` | "SUSPICIOUS DOMAIN" |
| `TYPOSQUATTING` | "TYPOSQUATTING" |
| `HOMOGRAPH_ATTACK` | "HOMOGRAPH ATTACK" |
| `RISKY_TLD` | "RISKY TLD" |
| `INSECURE_PROTOCOL` | "INSECURE PROTOCOL" |
| `SUBDOMAIN_ABUSE` | "SUBDOMAIN ABUSE" |
| `URL_SHORTENER` | "URL SHORTENER" |
| `URGENCY_TACTIC` | "URGENCY TACTIC" |
| `BRAND_IMPERSONATION` | "BRAND IMPERSONATION" |

### 4.2 Severity Naming (Optional)

If severity indicators are used:
- **High**: Red icon, danger color
- **Medium**: Orange/Warning icon
- **Low**: Yellow icon
- **Info**: Blue icon

---

## 5. Accessibility Rules

### 5.1 Semantics Equivalents

| Platform | Implementation |
|----------|----------------|
| Android | `Modifier.semantics { contentDescription = ... }` |
| iOS | `accessibilityLabel(_:)` |
| Desktop | `Modifier.semantics { contentDescription = ... }` |
| Web | `aria-label`, `role="status"`, `aria-live="polite"` |

### 5.2 Reduced Motion Behavior

When `prefers-reduced-motion: reduce`:
- Disable pulse animation
- Disable ripple animation
- Keep static node display
- Keep explanation text fully visible

### 5.3 Keyboard Navigation (Desktop/Web)

| Key | Action |
|-----|--------|
| `P` or `1` | Mark as Phishing |
| `L` or `2` | Mark as Legitimate |
| `Enter` | Next Round / Close Modal |
| `Escape` | Return to Dashboard (game over) |

---

## 6. Acceptance Tests

### Cross-App Acceptance Checklist

| # | Test | Pass Criteria |
|---|------|---------------|
| 1 | Same triggers | Phishing challenges light up brain on decision |
| 2 | Same visual response | Red pulsing nodes + badges appear |
| 3 | Same text output | Signal names displayed identically |
| 4 | Same idle state | Blue calm nodes when no threats |
| 5 | Accessibility | Screen reader announces signal count |
| 6 | Reduced motion | Animation stops, text remains |
| 7 | Keyboard shortcuts | P/L/Enter work on Desktop/Web |

---

## 7. Implementation Status

| Platform | Brain Visual | Badges | Accessibility | Keyboard | Status |
|----------|--------------|--------|---------------|----------|--------|
| Android | ✅ `CommonBrainVisualizer` | ✅ | ✅ | N/A | ✅ Complete |
| Desktop | ✅ `CommonBrainVisualizer` | ✅ | ✅ | ✅ | ✅ Complete |
| iOS | ✅ `BrainVisualizer.swift` | ✅ | ✅ | N/A | ✅ Complete |
| Web | ✅ `visualizer.js` | ✅ | ✅ | ✅ | ✅ Complete |

---

## 8. Files Changed

### Android
- `androidApp/.../BeatTheBotScreen.kt` - Uses `CommonBrainVisualizer`

### iOS
- `iosApp/MehrGuard/UI/Components/BrainVisualizer.swift` - **NEW**
- `iosApp/MehrGuard/UI/Training/BeatTheBotView.swift` - Integrated visualizer

### Desktop
- `desktopApp/.../TrainingScreen.kt` - Uses `CommonBrainVisualizer`

### Web
- `webApp/.../visualizer.js` - **NEW** Brain visualizer implementation
- `webApp/.../game.html` - Added canvas and badges section
- `webApp/.../game.js` - Integrated visualizer
- `webApp/.../game.css` - Added visualizer styles

### Common (KMP)
- `common/.../CommonBrainVisualizer.kt` - Shared Compose implementation

---

## 9. Validation Commands

```bash
# Android
./gradlew :androidApp:assembleDebug

# Desktop
./gradlew :desktopApp:run

# iOS
open iosApp/MehrGuard.xcodeproj
# Build and run on simulator

# Web
./gradlew :webApp:jsBrowserDevelopmentRun
# Navigate to game.html
```

---

## 10. Judge-Mode Success Criteria

1. ✅ **Visual Consistency**: Side-by-side demo shows same brain visual behavior
2. ✅ **Tangible Signals**: Neural clusters light up, not just text logs
3. ✅ **Clear Explanation**: Badges explain what was detected
4. ✅ **Memorable on Video**: Pulsing animation is eye-catching
5. ✅ **Screen Recording Ready**: Works without narration
