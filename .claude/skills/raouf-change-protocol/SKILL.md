---
name: raouf-change-protocol
description: Before making any code changes, read AGENT.md/agent.md and changelog variants (CHANGELOG*.md, HISTORY.md, RELEASES.md). After changes, update both agent + changelog files with a "Raouf:" template entry. If blocked, search latest official developer docs and use all available tools/MCPs.
---

# Raouf Change Protocol (MANDATORY)

## Trigger
Use this skill whenever the user asks to:
- edit/refactor/implement/fix anything
- “update code”, “make changes”, “polish”, “debug”, “wire up”, “clean up”
- or any task that modifies repo files

## Non-negotiables

1) **Preflight reading comes first**
- Locate and OPEN, then read (not skim):
  - **Agent rules:** `AGENT.md` OR `agent.md` (prefer `AGENT.md` if both exist)
  - **Change log (first match wins):**
    - `CHANGELOG.md`, `changelog.md`
    - `CHANGELOG*.md` (e.g., `CHANGELOG_v2.md`, `CHANGELOG-2025.md`)
    - `HISTORY.md`
    - `RELEASES.md`
- Search order:
  1. repo root
  2. `docs/`
  3. `.github/`
  4. `app/`, `src/`
  5. anywhere else (full repo search)
- If multiple candidates exist:
  - Prefer in this order:
    1) root `CHANGELOG.md`
    2) root `CHANGELOG*.md`
    3) root `HISTORY.md`
    4) root `RELEASES.md`
    5) then the same order under `docs/` and `.github/`
  - Record which file you chose in the Preflight summary.
- If no changelog exists, create `CHANGELOG.md` using the `Raouf:` template and proceed.

2) **Explain before touching**
- After reading, output:
  - a 5–10 bullet summary of constraints from AGENT
  - a 5–10 bullet summary of recent changes from the chosen changelog
  - your planned edits (files you’ll touch + why)

3) **Edit safely**
- Make minimal, consistent changes aligned with AGENT rules.
- Prefer small commits-worth of changes (even if not committing).
- Run the project’s usual checks (lint/test/build) if available.

4) **If you hit a roadblock**
- Stop and capture:
  - exact error message
  - command/run context
  - file + line numbers
- Then **search the latest official developer documentation** relevant to the tech stack.
  - Prefer: official docs, release notes, GitHub issues from maintainers
  - Use **all available tools/MCPs** (repo search, web search, docs lookup, test runner, etc.)
- Propose 1–3 fixes with reasoning, then apply the best one.

5) **Postflight logging (MANDATORY)**
- Update BOTH:
  - the chosen changelog file
  - the chosen agent file
- Append a new entry using the “Raouf:” template:
  - include date (Australia/Sydney), scope, summary, files changed, verification, and follow-ups.

## Output format (every time)
- ✅ Preflight summary
- 🧠 Plan
- 🔧 Changes made (file-by-file)
- 🧪 Verification (commands + results)
- 🧾 Logs updated (show the appended entries verbatim)
