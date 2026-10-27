---
name: pre-push-review
description: >-
  Reviews local changes that have not been pushed yet (before git push) on 4
  fronts: business rules, null cases, broken API contract, and CI rules.
  Use before every push that touches a ViewModel, Repository, Mapper,
  UseCase, DTO, navigation, or build configuration.
---

# Skill: pre-push-review

## Goal
Review **your local changes** (what has not been pushed yet) before
`git push`, focusing on four fronts that the compiler and tests do not
catch:

1. **Business rules** — conditions and states changed without covering all the cases.
2. **Null cases** — optional DTO fields, `!!`, missing `?.`.
3. **Broken API contract** — an unexpected response (missing field, malformed
   JSON, unknown enum, 404) must reach the UI as an error state with
   *Retry*, never as a blank screen or an endless "loading".
4. **CI rules** — checks defined in `.github/workflows/` must remain
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
3. Apply the rules below to the diff and to the CI checks.
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
### 1. Business rules
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

### 4. CI rules
- Check all workflows relevant to the changed files; don't consider only
  the unit tests.
- `git diff --check` must not find whitespace or patch errors.
- Detekt must pass with `./gradlew detekt`.
- The unit tests must pass and the coverage report must generate with
  `./gradlew testDebugUnitTest :app:koverHtmlReportDebug`;
  there is no minimum coverage threshold.
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
