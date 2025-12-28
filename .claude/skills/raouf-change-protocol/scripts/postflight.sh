#!/usr/bin/env bash
set -euo pipefail

ENTRY_FILE=".codex/skills/raouf-change-protocol/templates/raouf-entry.md"

if [[ ! -f "$ENTRY_FILE" ]]; then
  echo "Missing template: $ENTRY_FILE"
  exit 2
fi

AGENT_FILE="${1:-AGENT.md}"
CHANGELOG_FILE="${2:-CHANGELOG.md}"

if [[ ! -f "$AGENT_FILE" ]]; then
  echo "Agent file not found: $AGENT_FILE"
  exit 2
fi

if [[ ! -f "$CHANGELOG_FILE" ]]; then
  echo "Changelog file not found: $CHANGELOG_FILE"
  exit 2
fi

echo "" >> "$AGENT_FILE"
cat "$ENTRY_FILE" >> "$AGENT_FILE"

echo "" >> "$CHANGELOG_FILE"
cat "$ENTRY_FILE" >> "$CHANGELOG_FILE"

echo "Appended Raouf template entry to:"
echo "  - $AGENT_FILE"
echo "  - $CHANGELOG_FILE"
