---
description: Standard workflow for resolving build, compilation, or runtime errors
---

# Error Resolution Workflow

> **AGENTS:** This workflow must be followed WHENEVER you encounter an error (compilation, test failure, runtime crash, or build failure).

## 🚨 The Protocol

1.  **🛑 Stop & Analyze**
    *   Read the error message carefully.
    *   Identify the type: Syntax, Type Mismatch, Deprecation, Configuration, or Logic.
    *   Do NOT blindly try to "guess" a fix based on old knowledge.

2.  **🔍 Search the Internet (MANDATORY)**
    *   **Rule:** You MUST search the internet for the specific error message + the current year/technologies.
    *   **Why:** Frameworks like Kotlin, Gradle, and Jetpack Compose change rapidly. Your internal training data might be outdated (e.g., `kotlinOptions` vs `compilerOptions`).
    *   **Query Pattern:** `"[error message]" [technology] [version] site:stackoverflow.com OR site:github.com`

3.  **🧠 Formulate a Fix**
    *   Based on *current* search results.
    *   Prefer solutions from official documentation or high-voted recent community answers.

4.  **🛠️ Apply & Verify**
    *   Apply the fix precisely.
    *   **Re-run the failing command** immediately to verify the fix.
    *   Do not assume it works; prove it.

5.  **📝 Document (Memory)**
    *   Add a specific entry to `.agent/agent.md` titled "Error Resolution: [Error Type]".
    *   Include:
        *   The Error
        *   The Search Query used
        *   The Fix applied
    *   This ensures "all agents remember" this layout.

## ⚡ Quick Example

**Error:** `Using 'jvmTarget: String' is an error. Please migrate to the compilerOptions DSL.`

**Action:**
1.  **Search:** "gradle kotlin compilerOptions dsl jvmTarget 2025"
2.  **Learn:** `kotlinOptions` is deprecated in Kotlin 2.0+. Use `kotlin { compilerOptions { ... } }`.
3.  **Fix:** update `build.gradle.kts`.
4.  **Verify:** Run `./gradlew assembleDebug`.
