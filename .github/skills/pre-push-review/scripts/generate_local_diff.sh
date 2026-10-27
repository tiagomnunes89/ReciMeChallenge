#!/usr/bin/env bash

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"
OUT_DIR="$REPO_ROOT/.review-tmp"
mkdir -p "$OUT_DIR"

BASE_REF="${1:-}"
if [ -z "$BASE_REF" ]; then
  if git rev-parse --verify -q '@{u}' >/dev/null 2>&1; then
    BASE_REF=$(git rev-parse --abbrev-ref '@{u}')
  elif git rev-parse --verify -q origin/main >/dev/null 2>&1; then
    BASE_REF="origin/main"
  elif REF=$(git symbolic-ref -q --short refs/remotes/origin/HEAD 2>/dev/null) && [ -n "$REF" ]; then
    BASE_REF="$REF"
  else
    BASE_REF="HEAD~1"
  fi
fi

case "$BASE_REF" in
  origin/*) git fetch origin "${BASE_REF#origin/}" --quiet 2>/dev/null || true ;;
esac

echo "Base branch: $BASE_REF"
MERGE_BASE=$(git merge-base "$BASE_REF" HEAD)
echo "Merge-base : $MERGE_BASE"

{ git diff "$MERGE_BASE" HEAD; git diff HEAD; } > "$OUT_DIR/push_diff.diff"
{ git diff --name-only "$MERGE_BASE" HEAD; git diff --name-only HEAD; } | sort -u > "$OUT_DIR/push_changed_files.txt"

echo
echo "Changed files:"
cat "$OUT_DIR/push_changed_files.txt"
echo
echo "Full diff at: $OUT_DIR/push_diff.diff"
