# Desktop App - Decorative UI Audit & Fix Task List

> **Generated**: December 24, 2025
> **Updated**: December 24, 2025 (Session 10d)
> **Purpose**: Identify all decorative/non-functional UI elements and wire them to real functionality

---

## Overview

This document tracks all UI elements in the Desktop application that are currently **decorative** (static/placeholder data) and need to be connected to **functional code**.

### Priority Legend
- 🔴 **Critical** - Core feature, completely non-functional
- 🟠 **High** - Important feature, partially functional
- 🟡 **Medium** - Secondary feature, works but with placeholder data
- 🟢 **Low** - Minor enhancement
- ✅ **COMPLETE** - Task has been implemented

---

## 1. Beat the Bot (Training Screen) ✅ COMPLETE

**Status**: Fully functional game loop with all features implemented.

### Completed Items:

| Component | Status | Notes |
|-----------|--------|-------|
| Game State | ✅ DONE | Enhanced TrainingState with all fields |
| Challenge Database | ✅ DONE | Expanded to 10 challenges |
| Round Progression | ✅ DONE | Shuffled, random order |
| Phishing/Legitimate Buttons | ✅ DONE | Full game logic |
| Result Modal | ✅ DONE | TrainingResultModal composable |
| Game Over Modal | ✅ DONE | TrainingGameOverModal composable |
| Bot Score | ✅ DONE | Bot gets 100 pts/round |
| Response Time | ✅ DONE | Tracked per decision |
| Best Streak | ✅ DONE | Tracked and displayed |
| Scoreboard | ✅ DONE | Player vs Bot in game over |
| Session ID | ✅ DONE | Generated per session |
| End Session Button | ✅ DONE | Works via keyboard or game over |
| Keyboard Shortcuts | ✅ DONE | P=Phishing, L=Legitimate, Enter=Next |

---

## 2. Profile/User Section ✅ COMPLETE

**Status**: ProfileDropdown component created and integrated.

### Completed Items:

| Component | Status | Notes |
|-----------|--------|-------|
| Profile Dropdown | ✅ DONE | ProfileDropdown.kt created |
| Quick Stats | ✅ DONE | Total, Safe, Threats from history |
| Menu Items | ✅ DONE | View Profile, Settings |
| Plan Badge | ✅ DONE | Enterprise Plan display |
| Toggle on Click | ✅ DONE | Shows dropdown vs navigating |

### Remaining Items:
- [ ] Edit profile name/initials (low priority)
- [ ] Sign out button (not applicable for desktop)

---

## 3. Notification System ✅ COMPLETE

**Status**: Full notification system with triggers on all scan results.

### Completed Items:

| Component | Status | Notes |
|-----------|--------|-------|
| Notification Panel | ✅ DONE | Functional dropdown |
| Threat Detection Trigger | ✅ DONE | MALICIOUS → notification |
| Safe Scan Trigger | ✅ DONE | SAFE → notification |
| Suspicious Trigger | ✅ DONE | SUSPICIOUS → notification |
| Wired to All Screens | ✅ DONE | 6 screens updated |

### Remaining Items:
- [ ] Notification persistence (medium priority)
- [ ] Settings save notification (low priority)

---

## 4. Result Screens (Safe/Suspicious/Dangerous) ✅ MOSTLY COMPLETE

**Status**: Dynamic data from RiskAssessment, notification icons wired.

### Completed Items:

| Component | Status | Notes |
|-----------|--------|-------|
| Notification Icon | ✅ DONE | Uses NotificationPanel |
| Technical Rows | ✅ DONE | Dynamic from assessment |
| Verdict Card | ✅ Works | From analysis |
| Confidence Score | ✅ Works | From assessment |
| Analysis Time | ✅ Works | Tracked |

### Remaining Items:
- [ ] Analysis Cards from verdictDetails (medium priority)
- [ ] Risk Meter exact calculation (low priority)

---

## 5. Dashboard Screen ✅ MOSTLY COMPLETE

**Status**: Most features functional.

### Completed Items:

| Component | Status | Notes |
|-----------|--------|-------|
| Stats Cards | ✅ Works | From historyStats |
| Notification Icon | ✅ DONE | Uses NotificationPanel |
| Profile Click | ✅ DONE | Shows ProfileDropdown |

### Remaining Items:
- [ ] Dynamic Security Score (low priority)
- [ ] Engine version display (low priority)

---

## 6. Settings/Trust Centre Screens 🟡 PARTIAL

**Current State**: Core toggles work, some advanced options decorative.

### Remaining Items:
- [ ] Sound alerts toggle (requires audio API)
- [ ] Desktop notifications (platform specific)
- [ ] Auto-block threats logic

---

## 7-9. Other Screens ✅ MOSTLY COMPLETE

**Scan History, Live Scan, Reports/Export** - All notification icons now wired.

---

## Summary

### Session 10d Completion:

| Phase | Status | Items Completed |
|-------|--------|-----------------|
| Phase 1: Core Functionality | ✅ DONE | Beat the Bot game, notifications |
| Phase 2: User Experience | ✅ DONE | Profile dropdown, all screen wiring |
| Phase 3: Polish | ✅ DONE | Dynamic data, keyboard shortcuts |

### Total Changes:
- **New Files**: 2 (TrainingResultModal, TrainingGameOverModal in TrainingScreen.kt, ProfileDropdown.kt)
- **Modified Files**: 12
- **Lines Added**: ~600
- **Features Implemented**: 15+

---

*Task list updated after Session 10d implementation.*
