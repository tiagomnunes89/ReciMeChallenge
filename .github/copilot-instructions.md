# Copilot instructions for this repository

This is **ReciMeChallenge**: a native Android app (Kotlin + Jetpack Compose)
for browsing, searching, and favoriting recipes, built for the ReciMe Android
Coding Challenge. The detailed requirements and architecture are in
`../AGENT.md` and `README.md`. Follow these conventions when suggesting or
generating code.

## Stack
- Kotlin 2.x, Jetpack Compose + Material 3, Gradle Kotlin DSL with Version
  Catalog (`gradle/libs.versions.toml`).
- Dependency injection: **Hilt**. Async: Coroutines + `StateFlow`.
- Network: Retrofit + kotlinx.serialization; the API is simulated by
  `MockRecipeApi` (reads `assets/recipes.json`).
- Persistence: **DataStore Preferences**, only for favorites.
- Navigation: Navigation Compose with typed routes (`@Serializable`).
- Tests: **JUnit 4**, `kotlinx-coroutines-test`, **hand-written fakes**
  (no mocking libraries) and Robolectric when the ViewModel or screen needs
  Android (`toRoute`, Compose UI tests). On-device tests live in
  `app/src/androidTest` and use the Compose test APIs.
- Quality: **Detekt** (`config/detekt/detekt.yml`, `maxIssues: 0`) and
  **Kover** (ViewModel coverage report, no threshold); both run in CI (`ci.yml`).
- minSdk 26, compileSdk 37, targetSdk 36. Build: `./gradlew assembleDebug`;
  tests: `./gradlew testDebugUnitTest`; lint: `./gradlew detekt`. Use JDK 17.

## Architecture
- Clean Architecture with two modules: `:app` (packages `domain`, `data`,
  `ui`, `common`) and `:designsystem` (theme and reusable components).
- Dependency rule: `ui` → `domain`; `data` → `domain`; `domain` does not
  import `data`, `ui` or Compose. `:designsystem` never depends on `:app`.
- Unidirectional flow: the `ViewModel` exposes `StateFlow<UiState>`; the UI
  sends events (`onXxxClick`) and never changes state directly.
- Failures: repositories throw; the ViewModel catches (rethrowing
  `CancellationException`) and exposes `UiState.Error(@StringRes)` through
  `toUserMessageRes()`, with *Retry*. The paged home list uses
  `LoadResult.Error` → `LazyPagingItems.loadState` → `retry()`. Never leave
  the screen blank or stuck on "loading".
- Repositories are interfaces in the `domain` package, bound via `@Binds`
  in `data/di`. Only add a use case when it combines several sources (today:
  `ObserveFavoriteRecipesUseCase`).

## Where things live
- `app/src/main/kotlin/com/recime/challenge/`: `domain/`, `data/`,
  `data/remote/` (Retrofit contract, DTOs, mock API), `ui/{home,detail,
  favorites,navigation}`, `common/`.
- `designsystem/`: theme and reusable components; it never imports
  `domain` models.
- `app/src/test/`: unit and Robolectric tests, mirroring the main packages.
- `app/src/androidTest/`: on-device Compose tests.
- Each module configures itself in its own `build.gradle.kts`; there are no
  convention plugins.

## Conventions
- Root package: `com.recime.challenge`.
- Public Composables receive a `Modifier` and have a `@Preview`; preview
  data lives in `PreviewData.kt`.
- Screens: `XxxRoute` (wires up the ViewModel) + `XxxScreen` (stateless).
- Clickable elements need a `contentDescription`/label and a target ≥ 48dp.
- Navigation to the detail screen uses `launchSingleTop` to avoid duplicate
  screens from a double tap.

## When generating/changing code
- Do not remove branches of a `when` over a `sealed interface`/enum without
  checking all usages; avoid an `else` that "catches everything".
- Do not use `!!`; handle optional DTO fields in the mappers.
- New code in a ViewModel/UseCase/Repository/Mapper comes with a test in the
  same style as the existing ones (see `/unit-test-generation`).
- Do not add a dependency unless necessary; use the Version Catalog.
- Do not leave commented-out code or stray TODOs.
- UI text lives in `strings.xml`. Do not hardcode user-facing strings in
  Composables.

## Commits and history
- The repository's only author/contributor is **tiagomnunes89**. Never add
  `Co-authored-by` trailers or tool identities.
- Short commit messages, in English, in the imperative mood.

## Agents and skills
`.github/agents/` and `.github/skills/` keep four workflows:
`/pre-push-review` reviews the local diff before a pull request,
`/change-review` reviews an open pull request, `/unit-test-generation`
writes tests, and `/debug` diagnoses a device failure. See
`.github/skills/README.md`.
