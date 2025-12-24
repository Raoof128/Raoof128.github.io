---
description: Documentation requirements after each code change
---

# Documentation Workflow

## 🔴 MANDATORY RULE - READ FIRST!

> **After EVERY task and edit, you MUST update `agent.md` and `CHANGELOG.md` accordingly.**
> 
> This is NON-NEGOTIABLE. No exceptions. Do this BEFORE considering your task complete.

---

## ⚠️ CRITICAL: Other Agents Are Working
**ALWAYS only commit YOUR changes.** Other agents may be working on the same codebase simultaneously. 
- Use `git add <specific-files>` - NEVER use `git add .`
- Only stage files YOU modified
- Check `git status` before committing

---

## 📋 After EVERY Code Change Checklist

After completing any code modification (features, bug fixes, refactors), you MUST do ALL of these:

### ✅ Step 1: Update `.agent/agent.md`

**Location:** `/Users/raoof.r12/Desktop/Raouf/K/qrshield/.agent/agent.md`

Add a detailed session entry including:
- **Summary**: Brief description of changes
- **Components**: List of new/modified files
- **Code Snippets**: Relevant code examples
- **Tables**: Feature mappings, color schemes, route callbacks, etc.
- **Verification**: Build status and test results

### ✅ Step 2: Update `CHANGELOG.md`

**Location:** `/Users/raoof.r12/Desktop/Raouf/K/qrshield/CHANGELOG.md`

Add a new version entry with:
- Version number (increment appropriately)
- Date
- Category header (e.g., "### 🎨 UI Changes", "### 🔧 Bug Fixes")
- Bullet points of specific changes
- Code snippets where helpful
- Build verification status

### ✅ Step 3: Update Version Numbers

If your changes warrant a version bump, update ALL of these:

| Platform | File | Field |
|----------|------|-------|
| **Android** | `androidApp/build.gradle.kts` | `versionCode` + `versionName` |
| **iOS** | `iosApp/QRShield.xcodeproj/project.pbxproj` | `MARKETING_VERSION` (2 places) |
| **Desktop** | `desktopApp/.../screens/DashboardScreen.kt` | Version in KeyValueRow (~line 520) |
| **agent.md** | `.agent/agent.md` | Header version number |

### ✅ Step 4: Commit Only Your Changes

When committing:
- Use `git add` to stage ONLY the files you modified
- Do NOT stage unrelated changes (other agents may be working too)
- Use descriptive commit messages following conventional commits format

---

## 📝 Template: agent.md Entry

```markdown
# 🏷️ [Date] (Session XX) - Brief Title

### Summary
One-line summary of what you did.

## ✅ Changes Made

### Files Updated
| File | Change |
|------|--------|
| `path/to/file` | Description of change |

### New Files Created
| File | Purpose |
|------|---------|
| `path/to/file` | What it does |

## 🔧 Technical Details

Explain important implementation details here.

## ✅ Build Verification

```bash
./gradlew :module:task
# OUTPUT: BUILD SUCCESSFUL
```

---
```

## 📝 Template: CHANGELOG.md Entry

```markdown
## [X.Y.Z] - YYYY-MM-DD

### 🏷️ Category Header

**Feature/Fix Name** (`affected/files.kt`)
- Bullet point of change
- Another change
- Code example if helpful

#### Files Modified
| File | Change |
|------|--------|
| `file.kt` | What changed |
```

---

## ⚡ Quick Reference

```bash
# Files to update after EVERY change:
1. .agent/agent.md           # Session documentation
2. CHANGELOG.md              # Version history
3. androidApp/build.gradle.kts    # If version bump
4. iosApp/.../project.pbxproj     # If version bump (2 places!)
5. desktopApp/.../DashboardScreen.kt  # If version bump
```

---

**Remember: Documentation is NOT optional. Update agent.md and CHANGELOG after EVERY edit.**


