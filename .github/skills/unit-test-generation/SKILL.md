---
name: unit-test-generation
description: >-
  Generates or completes unit tests (JUnit 4 + kotlinx-coroutines-test +
  hand-written fakes) for ViewModel, use case, repository and mapper
  following the project's conventions. Use when creating or changing a class
  without coverage, passing the path of the target file.
---

# Skill: unit-test-generation

## Goal
Generate (or complete) tests that follow exactly the style of the existing
tests (`ui/home/HomeViewModelTest`, `data/repository/DefaultRecipeRepositoryTest`,
`data/remote/mock/RecipeFilteringTest`, `data/repository/RoomFavoritesRepositoryTest`,
`ui/home/HomePagingSourceTest`), to increase coverage without
creating a divergent style.

## How to use
```
/unit-test-generation <target-file-path>
```
Example:
`/unit-test-generation app/src/main/kotlin/com/recime/challenge/data/repository/RoomFavoritesRepository.kt`

## Workflow
1. Read the target and identify dependencies, states and `when` branches.
2. Look for the existing test (`app/src/test/kotlin/.../*Test.kt`); if
   there is one, **add** only the missing cases.
3. Cover at a minimum: success, repository error, loading (if present), each
   `sealed interface` branch, empty lists and null/blank values.
4. Show the tests before writing them; write only after confirmation.
5. Validate: `./gradlew :app:testDebugUnitTest --tests "<Class>Test"`.

## Mandatory conventions
- JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`), no MockK/Mockito.
- Dependencies as hand-written **fakes**: reuse the shared ones in
  `app/src/test/kotlin/com/recime/challenge/testing/Fakes.kt` (`FakeRecipeRepository`,
  `FakeFavoritesRepository`) and `FakeRecipeApi` in `data/repository`; add a private
  fake only for a one-off case.
- ViewModel: `Dispatchers.setMain(UnconfinedTestDispatcher())` in `@Before`
  and `resetMain()` in `@After`; collect `stateIn(WhileSubscribed)` in
  `backgroundScope`; time with `advanceTimeBy`/`runCurrent`.
- Room: `createInMemoryDatabase()` (`testing/TestDatabase.kt`) in a Robolectric test with
  `@Config(sdk = [35])`; close it in `@After`.
- Backtick names describing the behavior.
- Fixtures: `previewRecipes` (`app/src/main/kotlin/.../ui/home/PreviewData.kt`).
- File mirroring the package of the target under `app/src/test/kotlin/com/recime/challenge/`,
  `Test.kt` suffix.

## What NOT to do
- Do not use libraries outside the stack; do not test private details.
- Robolectric only where Android is required: ViewModels that use `toRoute`
  (see `RecipeDetailViewModelTest`) and Compose screen tests.
- Remove tests only when the production path they cover is removed, and replace
  them with coverage for the path that now executes.
