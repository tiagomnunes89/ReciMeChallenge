#!/usr/bin/env bash
# Local run of the checks in .github/workflows/ci.yml.
# Exits non-zero when a check fails, so a pre-push hook can abort the push.
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

if [ -z "${JAVA_HOME:-}" ] && [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "== CI: whitespace =="
if git rev-parse --abbrev-ref --symbolic-full-name '@{upstream}' >/dev/null 2>&1; then
  git diff --check '@{upstream}'...HEAD
elif git rev-parse --verify origin/main >/dev/null 2>&1; then
  git diff --check origin/main...HEAD
else
  echo "No upstream or origin/main; whitespace check skipped."
fi

echo "== CI: detekt, unit tests, coverage report, release bundle =="
./gradlew detekt testDebugUnitTest :app:koverHtmlReportDebug :app:bundleRelease --stacktrace --console=plain

resolve_adb() {
  if command -v adb >/dev/null 2>&1; then
    command -v adb
    return 0
  fi
  if [ -n "${ANDROID_HOME:-}" ] && [ -x "${ANDROID_HOME}/platform-tools/adb" ]; then
    echo "${ANDROID_HOME}/platform-tools/adb"
    return 0
  fi
  local sdk_dir
  sdk_dir="$(sed -n 's/^sdk.dir=//p' local.properties 2>/dev/null | head -1)"
  if [ -n "$sdk_dir" ] && [ -x "${sdk_dir}/platform-tools/adb" ]; then
    echo "${sdk_dir}/platform-tools/adb"
    return 0
  fi
  return 1
}

adb_bin="$(resolve_adb || true)"
if [ -z "$adb_bin" ]; then
  echo "== CI: instrumented tests skipped (adb not found). GitHub CI still runs them on an emulator. =="
  exit 0
fi

if ! "$adb_bin" devices | awk 'NR>1 && $2=="device" { found=1 } END { exit !found }'; then
  echo "== CI: instrumented tests skipped (no device or emulator online). GitHub CI still runs them. =="
  exit 0
fi

echo "== CI: instrumented tests =="
./gradlew connectedDebugAndroidTest --stacktrace --console=plain
