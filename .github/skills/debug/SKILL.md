---
name: debug
description: >-
  Local diagnosis of Android failures without depending on the IDE debugger:
  captures instrumentation failures, logcat, and Gradle reports to locate
  the root cause and propose the smallest fix.
---

# Skill: debug

## When to use

Use this alternative when the JetBrains debugger (`ide_debug_*`) is not
available in the session. It is for diagnosis only: it does not change code
or run `git push`.

## Flow

1. Read the scenario and locate the relevant code before running commands.
2. Check the device with `adb devices`.
3. Reproduce the failure with the smallest applicable command, for example:
   `./gradlew connectedDebugAndroidTest --stacktrace`.
4. Run `scripts/diagnose_android.sh [package]` to collect devices, recent
   `logcat` exceptions, XML/text results of the instrumented suites, and
   existing Gradle reports.
5. Separate code failures from configuration and environment failures.
6. Report evidence, root cause (or explicit uncertainty), and the
   recommended fix. Ask for confirmation before editing.

The script does not clear logcat, does not install APKs, and does not send
data outside the repository.
