---
description: Documentation requirements after each code change
---

# Documentation Workflow

## ⚠️ CRITICAL: Other Agents Are Working
**ALWAYS only commit YOUR changes.** Other agents may be working on the same codebase simultaneously. 
- Use `git add <specific-files>` - NEVER use `git add .`
- Only stage files YOU modified
- Check `git status` before committing

## MANDATORY: After EVERY Code Change

After completing any code modification (features, bug fixes, refactors), you MUST:

### 1. Update `.agent/agent.md`
Add a detailed session entry including:
- **Summary**: Brief description of changes
- **Components**: List of new/modified files
- **Code Snippets**: Relevant code examples
- **Tables**: Feature mappings, color schemes, route callbacks, etc.
- **Verification**: Build status and test results

### 2. Update `CHANGELOG.md`
Add a new version entry with:
- Version number (increment appropriately)
- Date
- Category header (e.g., "### 🎨 UI Changes", "### 🔧 Bug Fixes")
- Bullet points of specific changes
- Code snippets where helpful
- Build verification status

### 3. Commit Only Your Changes
When committing:
- Use `git add` to stage ONLY the files you modified
- Do NOT stage unrelated changes (other agents may be working too)
- Use descriptive commit messages following conventional commits format
- Push to origin main

## Example agent.md Entry

```markdown
# 🌙 December 21, 2025 - Dark Mode Integration

### Summary
Integrated dark mode theme matching HTML patterns.

## 📦 Theme Updates

| Element | Light | Dark |
|---------|-------|------|
| Background | #f6f6f8 | #101622 |

## ✅ Build Verification
BUILD SUCCESSFUL in 18s
```

## Example CHANGELOG.md Entry

```markdown
## [1.14.0] - 2025-12-21

### 🌙 Dark Mode Integration

- Updated Theme.kt color schemes
- Added QRShieldThemeColors accessors
- Build verified ✅
```
