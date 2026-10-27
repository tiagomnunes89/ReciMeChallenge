#!/usr/bin/env bash
set -uo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$REPO_ROOT"
PACKAGE="${1:-}"

echo "== Devices =="
if command -v adb >/dev/null 2>&1; then
  adb devices -l
else
  echo "adb not found; device diagnosis not run."
fi

echo
echo "== Logcat (recent exceptions) =="
if command -v adb >/dev/null 2>&1; then
  adb logcat -d -v threadtime 2>/dev/null |
    grep -E 'FATAL EXCEPTION|AndroidRuntime|Process:|Instrumentation|CRASH|ANR|Exception' |
    tail -120 || echo "No relevant exception found in logcat."
else
  echo "adb not found."
fi

echo
echo "== Instrumented results =="
RESULT_FILES=$(find . -path '*/build/outputs/androidTest-results/connected/*' \
  \( -name '*.xml' -o -name '*.textproto' -o -name 'test-results.log' \) \
  -not -path '*/build/intermediates/*' 2>/dev/null | sort)
if [ -z "$RESULT_FILES" ]; then
  echo "No instrumented results found."
else
  while IFS= read -r file; do
    echo "-- $file"
    grep -E 'failure|error|shortMsg|message=|tests=|failures=|errors=' "$file" |
      head -40 || true
  done <<< "$RESULT_FILES"
fi

echo
echo "== Gradle reports =="
REPORTS=$(find . -path '*/build/reports/*' -type f \
  \( -name '*.html' -o -name '*.xml' -o -name '*.txt' \) \
  -not -path '*/build/intermediates/*' 2>/dev/null | sort)
if [ -z "$REPORTS" ]; then
  echo "No reports found."
else
  echo "$REPORTS" | sed -n '1,80p'
fi

if [ -n "$PACKAGE" ]; then
  echo
  echo "== Package process: $PACKAGE =="
  if command -v adb >/dev/null 2>&1; then
    adb shell pidof "$PACKAGE" 2>/dev/null || echo "Process not found."
  else
    echo "adb not found."
  fi
fi
