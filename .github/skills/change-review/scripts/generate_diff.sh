#!/usr/bin/env bash
set -euo pipefail
PR="${1:-}"
if [[ -z "$PR" ]]; then echo "usage: $0 <pr-number>" >&2; exit 1; fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(git rev-parse --show-toplevel)"
OUT_DIR="$REPO_ROOT/.review-tmp"
mkdir -p "$OUT_DIR"

URL=$(git remote get-url origin 2>/dev/null) || { echo "error: no 'origin' remote" >&2; exit 1; }
HOST=$(echo "$URL" | sed -E 's#^git@([^:]+):.*#\1#; s#^ssh://git@([^/]+)/.*#\1#; s#^https?://([^/]+)/.*#\1#')
REPO=$(echo "$URL" | sed -E 's#^git@[^:]+:##; s#^ssh://git@[^/]+/##; s#^https?://[^/]+/##; s#\.git$##')
if [[ "$HOST" == "github.com" ]]; then API="https://api.github.com"; else API="https://${HOST}/api/v3"; fi

TOKEN="${GITHUB_TOKEN:-}"
if [[ -z "$TOKEN" ]]; then
  TOKEN=$(printf 'protocol=https\nhost=%s\n\n' "$HOST" | git credential fill 2>/dev/null | grep '^p' | cut -d= -f2- | tail -n 1 || true)
fi
if [[ -z "$TOKEN" ]]; then
  echo "error: could not get a token for $HOST (export GITHUB_TOKEN)" >&2; exit 1
fi
AUTH=(-H "Authorization: token $TOKEN")

echo "repository  : $REPO ($HOST)"
echo "PR          : #$PR"
curl -sf "${AUTH[@]}" "$API/repos/$REPO/pulls/$PR" -o "$OUT_DIR/pr_${PR}.json" || {
  echo "error: could not read PR #$PR in $REPO (wrong number or token without access)" >&2; exit 1; }
curl -sf "${AUTH[@]}" "$API/repos/$REPO/pulls/$PR/files?per_page=100" -o "$OUT_DIR/pr_${PR}_files.json"
curl -sf "${AUTH[@]}" -H "Accept: application/vnd.github.v3.diff" "$API/repos/$REPO/pulls/$PR" -o "$OUT_DIR/pr_${PR}.diff"
awk -f "$SCRIPT_DIR/map-diff-lines.awk" "$OUT_DIR/pr_${PR}.diff" > "$OUT_DIR/pr_${PR}_map.txt"
unset TOKEN AUTH

TITLE=$(tr -d '\n' < "$OUT_DIR/pr_${PR}.json" | sed -E 's/.*"title": *"([^"]*)".*/\1/')
FILES=$(grep -c '"filename"' "$OUT_DIR/pr_${PR}_files.json" || true)
LINES=$(wc -l < "$OUT_DIR/pr_${PR}_map.txt" | tr -d ' ')
echo
echo "title       : $TITLE"
echo "files       : $FILES"
echo "lines +     : $LINES (commentable)"
echo "output      : $OUT_DIR/pr_${PR}.{json,diff} pr_${PR}_files.json pr_${PR}_map.txt"
