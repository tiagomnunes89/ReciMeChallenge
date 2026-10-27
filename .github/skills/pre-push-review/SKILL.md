---
name: pre-push-review
description: >-
  Reviews local changes that have not been pushed yet (before git push) for
  serious pattern, behavior, security, performance, API-contract, and CI
  issues.
  Use before every push that touches a ViewModel, Repository, Mapper,
  UseCase, DTO, navigation, or build configuration.
---

# Skill: pre-push-review

## Goal
Review **your local changes** (what has not been pushed yet) before
`git push`, focusing on serious issues that the compiler and ordinary tests
may not catch:

1. **Patterns and behavior** — severe architecture/pattern violations and
   changes that can introduce regressions or alter required behavior.
2. **Null cases** — optional DTO fields, `!!`, missing `?.`.
3. **Broken API contract** — an unexpected response (missing field, malformed
   JSON, unknown enum, 404) must reach the UI as an error state with
   *Retry*, never as a blank screen or an endless "loading".
4. **Security** — serious vulnerabilities such as exposed secrets or
   sensitive data, injection, unsafe parsing, or broken trust boundaries.
5. **Performance** — severe regressions such as blocking the main thread,
   unbounded work/memory, repeated network/database work, or paging failures.
6. **CI rules** — checks defined in `.github/workflows/` must remain
   satisfied by the local changes.

Different from `change-review` (comments on an open PR via the API), this one
runs **locally**, with no PR and no token.

## How to use
```
/pre-push-review
/pre-push-review origin/main
```
With no argument, the base is the branch's upstream (only what has not been
pushed); with no upstream, it uses `origin/main`.

## Flow
1. `scripts/generate_local_diff.sh [base]` generates `.review-tmp/push_diff.diff`
   and the list of changed files.
2. Read the workflows in `.github/workflows/` and identify the checks that
   apply to the diff.
3. Apply the rules below to the diff and to the CI checks. Report only
   findings that can affect correctness, security, performance, reliability,
   or maintainability. Ignore style-only issues and anything that cannot
   interfere with the code's behavior. If no relevant issue is found, say so
   explicitly and do not invent findings.
4. When the environment allows it, run the same validations as the workflow:
   `git diff --check`, `./gradlew detekt`,
   `./gradlew testDebugUnitTest :app:koverHtmlReportDebug`, and
   `./gradlew connectedDebugAndroidTest` with an emulator/device
   available. A failure must be reported with the command and the relevant
   output; do not treat a failure as approval.
5. Show the findings by severity **before** `git push`.
6. Only fix after confirmation and, after any fix, repeat the affected CI
   validations and `./gradlew testDebugUnitTest`.
7. Delete `.review-tmp/` at the end.

## Review rules
### 1. Patterns and business behavior
- Enforce the repository architecture and established patterns: dependency
  direction, UDF/`StateFlow`, Hilt bindings, typed navigation, repository
  boundaries, and domain independence from Android.
- Flag severe pattern violations when they can cause incorrect behavior,
  lifecycle leaks, broken error handling, fragile coupling, or regressions;
  ignore harmless stylistic variation and patterns that cannot affect runtime
  behavior or maintainability.
- Every new or changed `if`/`when` covers the same cases as before.
- A `when` over a `sealed interface`/enum remains exhaustive; question a new
  catch-all `else`.
- Search filters keep AND semantics (vegetarian, servings, include/
  exclude ingredients, instructions text).
- Enum coming from JSON (`DietaryAttribute`, `data/mapper/RecipeMapper.kt`): an
  unknown value is ignored explicitly and never brings down the list.

### 2. Null cases
- A new `!!` in code that deals with a DTO/API is blocking.
- Optional DTO field used without `?.`/`?:`.
- `toDomain()` that assumes a non-null field without a fallback.

### 3. Broken contract → error state
- Every call that can fail reaches the UI as an error state with *Retry*:
  repositories throw, the ViewModel catches and emits `UiState.Error(@StringRes)`
  via `toUserMessageRes()`; the paged home list keeps
  `LoadResult.Error` → `LoadState.Error` → `retry()`. The state never gets
  stuck in `Loading`/empty.
- A new `catch` rethrows `CancellationException` and never swallows the error.
- A new screen with an async call lacking this path = **high severity**.
- Expected errors have their own exception (`RecipeNotFoundException`,
  mapped from HTTP 404 in `DefaultRecipeRepository`).

### 4. Serious security issues
- Check changed code for exploitable vulnerabilities, especially exposed
  credentials or sensitive data, injection, unsafe parsing or deep-link
  handling, and authentication/authorization bypasses.
- Report only issues with a credible attack path or a materially weakened
  security boundary.

### 5. Serious performance issues
- Check for blocking I/O or expensive work on the main thread, unbounded
  allocations, accidental request/database loops, broken caching or paging,
  and lifecycle/recomposition work that can cause visible degradation.
- Report only regressions likely to affect device responsiveness, memory,
  battery, or network usage.

### 6. CI rules
- Check all workflows relevant to the changed files; don't consider only
  the unit tests.
- `git diff --check` must not find whitespace or patch errors.
- Detekt must pass with `./gradlew detekt`.
- The unit tests must pass and the coverage report must generate with
  `./gradlew testDebugUnitTest :app:koverHtmlReportDebug`; broken tests are
  findings and block approval.
- If an emulator/device is available, the instrumented tests must pass with
  `./gradlew connectedDebugAndroidTest`; if not, record the validation as
  not run, never as passed.
- Changes to the workflow, Gradle, coverage rules, or lint configuration
  require re-running the directly affected validation, even if the diff
  does not touch production code.
- Layer rules inside `:app`: `ui` → `domain`; `data` → `domain`; `domain`
  never imports `data`, `ui` or Compose; `:designsystem` never imports `:app`
  or domain models.

## What NOT to do
- Don't comment on formatting/style/naming.
- Don't run `git push` automatically.
- Don't dismiss a "blank screen" finding because it looks rare.
