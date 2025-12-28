#!/usr/bin/env bash
set -euo pipefail

pick_first_existing() {
  for f in "$@"; do
    if [[ -f "$f" ]]; then echo "$f"; return 0; fi
  done
  return 1
}

# Prefer root first, then docs/, then .github/, then deep search.
pick_changelog() {
  local found=""

  # 1) Root exact
  found="$(pick_first_existing CHANGELOG.md changelog.md || true)"
  [[ -n "$found" ]] && { echo "$found"; return 0; }

  # 2) Root glob variants
  found="$(ls -1 CHANGELOG*.md 2>/dev/null | head -n 1 || true)"
  [[ -n "$found" ]] && { echo "$found"; return 0; }

  # 3) Root HISTORY/RELEASES
  found="$(pick_first_existing HISTORY.md RELEASES.md || true)"
  [[ -n "$found" ]] && { echo "$found"; return 0; }

  # 4) docs/ exact + variants
  found="$(pick_first_existing docs/CHANGELOG.md docs/changelog.md || true)"
  [[ -n "$found" ]] && { echo "$found"; return 0; }

  found="$(ls -1 docs/CHANGELOG*.md 2>/dev/null | head -n 1 || true)"
  [[ -n "$found" ]] && { echo "$found"; return 0; }

  found="$(pick_first_existing docs/HISTORY.md docs/RELEASES.md || true)"
  [[ -n "$found" ]] && { echo "$found"; return 0; }

  # 5) .github/ exact + variants
  found="$(pick_first_existing .github/CHANGELOG.md .github/changelog.md || true)"
  [[ -n "$found" ]] && { echo "$found"; return 0; }

  found="$(ls -1 .github/CHANGELOG*.md 2>/dev/null | head -n 1 || true)"
  [[ -n "$found" ]] && { echo "$found"; return 0; }

  found="$(pick_first_existing .github/HISTORY.md .github/RELEASES.md || true)"
  [[ -n "$found" ]] && { echo "$found"; return 0; }

  # 6) Full repo search (deterministic: sort)
  found="$(find . -maxdepth 6 -type f \( \
      -iname 'changelog.md' -o -iname 'changelog*.md' -o \
      -iname 'history.md' -o -iname 'releases.md' \
    \) 2>/dev/null | sort | head -n 1 || true)"
  [[ -n "$found" ]] && { echo "$found"; return 0; }

  return 1
}

AGENT_FILE="$(pick_first_existing AGENT.md agent.md docs/AGENT.md docs/agent.md .github/AGENT.md .github/agent.md || true)"
CHANGELOG_FILE="$(pick_changelog || true)"

echo "== Preflight =="
echo "AGENT: ${AGENT_FILE:-MISSING}"
echo "CHANGELOG: ${CHANGELOG_FILE:-MISSING}"

if [[ -z "${AGENT_FILE:-}" ]]; then
  echo "Missing AGENT.md/agent.md. Create one before edits."
  exit 2
fi

if [[ -z "${CHANGELOG_FILE:-}" ]]; then
  echo "No changelog found (CHANGELOG*.md / HISTORY.md / RELEASES.md). Create CHANGELOG.md before edits."
  exit 2
fi

echo ""
echo "Open and read these files now:"
echo "  - $AGENT_FILE"
echo "  - $CHANGELOG_FILE"
