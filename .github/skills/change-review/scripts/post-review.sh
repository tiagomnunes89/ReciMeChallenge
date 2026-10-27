#!/usr/bin/env bash
# Posts inline comments on a PR from an NDJSON file
# (one line = one JSON: {"path":"...","line":N,"side":"RIGHT","body":"..."}).
# One POST per comment. Uses neither gh, jq, nor python.
#
# Usage:
#   ./post-review.sh <pr-number> <file.ndjson> [--dry-run]
#
# --dry-run lists what would be posted, without sending anything.
# Token: $GITHUB_TOKEN or `git credential fill`. Never printed.
set -euo pipefail
PR="${1:-}"; FILE="${2:-}"; MODE="${3:-}"
if [[ -z "$PR" || -z "$FILE" ]]; then echo "usage: $0 <pr> <file.ndjson> [--dry-run]" >&2; exit 1; fi
[[ -f "$FILE" ]] || { echo "error: file $FILE does not exist" >&2; exit 1; }

REPO_ROOT="$(git rev-parse --show-toplevel)"
URL=$(git remote get-url origin)
HOST=$(echo "$URL" | sed -E 's#^git@([^:]+):.*#\1#; s#^ssh://git@([^/]+)/.*#\1#; s#^https?://([^/]+)/.*#\1#')
REPO=$(echo "$URL" | sed -E 's#^git@[^:]+:##; s#^ssh://git@[^/]+/##; s#^https?://[^/]+/##; s#\.git$##')
if [[ "$HOST" == "github.com" ]]; then API="https://api.github.com"; else API="https://${HOST}/api/v3"; fi

TOTAL=$(grep -c . "$FILE" || true)
if [[ "$MODE" == "--dry-run" ]]; then
  echo "[dry-run] $TOTAL comment(s) for $REPO#$PR:"
  n=0
  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -z "$line" ]] && continue
    n=$((n + 1))
    p=$(echo "$line" | sed -E 's/.*"path": *"([^"]*)".*/\1/')
    l=$(echo "$line" | sed -E 's/.*"line": *([0-9]+).*/\1/')
    echo "  $n. $p:$l"
  done < "$FILE"
  exit 0
fi

TOKEN="${GITHUB_TOKEN:-}"
if [[ -z "$TOKEN" ]]; then
  TOKEN=$(printf 'protocol=https\nhost=%s\n\n' "$HOST" | git credential fill 2>/dev/null | grep '^p' | cut -d= -f2- | tail -n 1 || true)
fi
[[ -n "$TOKEN" ]] || { echo "error: no token (export GITHUB_TOKEN)" >&2; exit 1; }

# commit_id is required for inline comments: use the PR's HEAD.
PR_JSON="$REPO_ROOT/.review-tmp/pr_${PR}.json"
if [[ -f "$PR_JSON" ]]; then
  SHA=$(tr -d '\n' < "$PR_JSON" | tr '{' '\n' | grep -A8 '"head"' | grep -m1 '"sha"' | sed -E 's/.*"sha": *"([0-9a-f]+)".*/\1/')
else
  SHA=$(curl -sf -H "Authorization: token $TOKEN" "$API/repos/$REPO/pulls/$PR" | tr -d '\n' | tr '{' '\n' | grep -A8 '"head"' | grep -m1 '"sha"' | sed -E 's/.*"sha": *"([0-9a-f]+)".*/\1/')
fi
[[ -n "$SHA" ]] || { echo "error: could not determine the PR commit" >&2; exit 1; }

OK=0; FAIL=0; TMP="$(mktemp)"
trap 'rm -f "$TMP"; unset TOKEN' EXIT
while IFS= read -r line || [[ -n "$line" ]]; do
  [[ -z "$line" ]] && continue
  # add commit_id to the line's JSON (strip the trailing brace and reopen).
  printf '%s' "${line%\}}" > "$TMP"
  printf ',"commit_id":"%s"}' "$SHA" >> "$TMP"
  CODE=$(curl -s -o /dev/null -w '%{http_code}' -X POST \
    -H "Authorization: token $TOKEN" -H "Content-Type: application/json; charset=utf-8" \
    --data-binary @"$TMP" "$API/repos/$REPO/pulls/$PR/comments")
  if [[ "$CODE" == "201" ]]; then OK=$((OK + 1)); else FAIL=$((FAIL + 1)); echo "failed (HTTP $CODE): $line" >&2; fi
done < "$FILE"
echo "posted: $OK  failures: $FAIL  (total $TOTAL)"
[[ "$FAIL" -eq 0 ]]
