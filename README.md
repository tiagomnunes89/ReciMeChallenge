# ReciMe Challenge

A native Android app for discovering, searching and favoriting recipes. The project was
built for the ReciMe Android Coding Challenge, with a modular architecture
inspired by *Now in Android*, a focus on user experience and a clear separation
of responsibilities.

## Features

- Recipe catalog loaded from a local JSON file, treated as a real API.
- Text search over title and description.
- Combinable filters for vegetarian, number of servings, ingredients to
  include, ingredients to exclude and instruction content.
- 300 ms debounce and Paging 3 infinite scrolling (20 items per page, bounded memory), with loading, empty,
  success, error and per-page retry states.
- Detail screen with ingredients, instructions, servings and dietary
  attributes.
- Favorite and unfavorite recipes; favorites persist across restarts.
- Favorites screen reachable from the home navigation drawer.
- Type-safe navigation between catalog, details and favorites.
- Explicit retry for loading failures.
- Responsive Jetpack Compose UI, with reusable components,
  accessible semantics and screen previews.

## Technologies and stack

| Area | Technologies |
|---|---|
| Language and build | Kotlin 2.4, AGP 9.4 (built-in Kotlin), Gradle 9.6, Gradle Kotlin DSL, Version Catalog (dependencies, plugins and SDK levels), JDK 17+ |
| Platform | compileSdk 37, targetSdk 36, minSdk 26, AndroidX, Activity Compose |
| UI | Jetpack Compose, Material 3, Compose BOM 2026.09, Navigation Compose, Coil 3 |
| Architecture | Clean Architecture, modularization by domain, UDF/MVI-like, ViewModel and StateFlow |
| Dependency injection | Hilt and KSP |
| Async and paging | Kotlin Coroutines, `StateFlow`, `debounce`, `flatMapLatest`, Paging 3 (`Pager`, `cachedIn`) and `DispatcherProvider` |
| Data and persistence | DataStore Preferences for favorites; DTO → domain mapping |
| Network and serialization | Retrofit, OkHttp, kotlinx.serialization (Kotlin Serialization plugin) and `MockRecipeApi` |
| Testing | JUnit 4, kotlinx-coroutines-test, Paging testing, Robolectric and Compose UI testing (`createComposeRule`, `createAndroidComposeRule`) |
| Quality | Detekt 2, Kover coverage report for ViewModels and GitHub Actions |

### Modules

Two Gradle modules, with the Clean Architecture layers as packages inside `:app`:

```text
:app            The application
  domain/       Models, repository contracts and the one use case with real logic
  data/         Repositories, paging source, DTO -> domain mappers, DataStore favorites
  data/remote/  Retrofit API contract, DTOs, mocked JSON API and HTTP configuration
  ui/           Home, detail, favorites and navigation (ViewModels + Compose screens)
  common/       DispatcherProvider
:designsystem   Material 3 theme and reusable visual components
```

Dependency rules, enforced by review and by the module boundary:

- `:app` depends on `:designsystem`; `:designsystem` never depends on `:app`.
- Inside `:app`: `ui` -> `domain`; `data` -> `domain`; `domain` imports neither `data`, `ui` nor Compose.

Why only two modules: the design system does not know about the domain (components receive primitives such as
a title and an image URL, never `Recipe`) and it is the part most likely to be reused, so a module boundary
makes that rule compiler-enforced at almost no cost. The remaining layers stay as packages because there is a
single feature and no build-time problem to solve. See *Assumptions, trade-offs and limitations* for when I
would extract more modules.

## Tools and engineering

- **Android Studio Ladybug or newer** for development, previews and running the app.
- **Gradle Wrapper** for reproducible builds without depending on a global
  Gradle installation.
- **Git and GitHub** for versioning, pull requests and change review.
- **GitHub Actions** for Detekt, unit tests, coverage, instrumented
  tests on an API 35 emulator and diff validation.
- **Detekt** for static analysis and **Kover** to report ViewModel coverage (informational, no threshold).
- **Robolectric** for ViewModel scenarios that need an Android environment
  in local tests.
- **Compose UI testing** (accessible semantics) to validate the app's main
  flows, in Robolectric locally and on an emulator in CI.
- **Versioned Copilot agents and skills in `.github/`** for audits of
  accessibility, concurrency, security, routes, double taps, defects,
  test coverage, PR review, conflicts and UI texts.

## Skills demonstrated

- Modern native Android development with Kotlin and Jetpack Compose.
- Domain modeling and application of Clean Architecture in a modular project.
- Definition of stable contracts between layers and adaptation of a mocked API
  to a real backend scenario.
- Implementation of reactive search with debounce, cancellation of stale
  queries, combinable filters and pagination.
- Unidirectional state management, explicit error handling and retry.
- Dependency injection, local persistence and dispatcher isolation to
  make testing easier.
- Building accessible interfaces, reusable components, previews and
  type-safe navigation.
- Unit tests with hand-written fakes, instrumented UI tests and an automated
  ViewModel coverage report.
- Continuous integration, static analysis, change review and documentation
  of assumptions, trade-offs and limitations.

## Setup

- JDK 17+ and Android SDK 37 (`local.properties` with `sdk.dir`, or `ANDROID_HOME`).
- Build: `./gradlew assembleDebug`
- Unit tests: `./gradlew testDebugUnitTest`
- ViewModel coverage report (informational, no threshold):
  `./gradlew :app:koverHtmlReportDebug`; the HTML report is at
  `app/build/reports/kover/htmlDebug/`.
- Open the folder in Android Studio (Ladybug or newer) and run the `:app` module.

## Architecture flow and dependencies

```
:app
  ReciMeApplication (@HiltAndroidApp) + MainActivity (@AndroidEntryPoint)
  ui/home, ui/detail, ui/favorites, ui/navigation   ViewModel + Route/Screen pairs
  domain/   model/ (Recipe, RecipeQuery), RecipeRepository, FavoritesRepository, ObserveFavoriteRecipesUseCase
  data/     DefaultRecipeRepository, RecipePagingSource, RecipeMapper (DTO -> domain), DataStore favorites, Hilt @Binds
  data/remote/  RecipeApi (Retrofit), DTOs, MockRecipeApi (serves assets/recipes.json), Json/OkHttp/Retrofit
  common/   DispatcherProvider
:designsystem
  ReciMeTheme (Color, Type, Theme), Material 3 and reusable components
```

`ui` never imports `data`, so DTOs do not reach the screens.

Flow: `Screen` <- `UiState` <- `ViewModel` -> `RecipeRepository` (interface, domain) -> `DefaultRecipeRepository` (data) -> `RecipeApi` (data/remote).

## Search

`RecipeApi.searchRecipes` accepts optional filters, combined with AND: text (title/description), vegetarian, servings, ingredients to include, ingredients to exclude and text in the instructions. The UI exposes all of them.

## Scalable list

The catalog is designed for thousands of recipes. `MockRecipeApi` repeats the six seed recipes up to 2,000 entries to exercise it.

- **Server-side paging with Paging 3**: `DefaultRecipeRepository.searchRecipes` returns a `Flow<PagingData<Recipe>>` built by a `Pager` over `RecipePagingSource` (page-number keys, 20 items per page, `hasNext` from the API so no total count is required). Loading, appending and per-page errors come from `LazyPagingItems.loadState`, and retry from `LazyPagingItems.retry()`.
- **Bounded memory**: `PagingConfig.maxSize` keeps at most 10 pages in memory; pages far from the viewport are dropped and reloaded on demand (covered by `RecipePagingSourceTest`).
- **Search**: `HomeViewModel` debounces by 300 ms (the very first load and clearing the filters skip the debounce), `flatMapLatest` cancels the stale query, and `cachedIn(viewModelScope)` keeps the loaded pages across rotation and back navigation, so returning from a recipe restores the scroll position.
- **Cheap recomposition**: items use stable `key`s and a `contentType`; the card receives primitives plus one shared click lambda; the search filters are read inside the header item (`() -> SearchFilters`), so typing never recomposes the grid; domain models are declared stable in `compose_stability.conf`.
- **Images**: recipes carry an `imageUrl`; Coil downsamples to the card size and caches in memory and on disk. The mock serves the photos from `assets/images`, like a CDN would.
- **Adaptive layout**: `GridCells.Adaptive` fits as many columns as the width allows.

## Design decisions

- **Mocked API with a real contract**: `RecipeApi` is a Retrofit interface; `MockRecipeApi` implements it by reading `assets/recipes.json` and applying the filters the way a server would. To use a backend, Hilt only needs to provide `retrofit.create(RecipeApi::class.java)`.
- **DTO separate from the domain**: the mapper ignores unknown dietary attributes and a 404 becomes `RecipeNotFoundException`.
- **UDF**: `*Route` (with ViewModel) → stateless `*Screen` that receives state and callbacks. All have `@Preview` (success, loading, empty and error). MVVM with unidirectional data flow is enough here: there are no one-shot events, so a full MVI intent/reducer layer would only add ceremony.
- **Use cases only when they add logic**: forwarding use cases were removed and ViewModels use the repositories directly; `ObserveFavoriteRecipesUseCase` stays because it combines two repositories.
- **Favorites (extra, outside the challenge scope)**: heart button on the detail screen and a **Favorites** screen, opened from the navigation drawer (the home hamburger icon); `FavoritesRepository` (domain) implemented with DataStore Preferences (data), so the state survives app restarts.
- **States and errors**: `RecipeDetailUiState` and `FavoritesUiState` are `sealed interface`s (Loading/Success/Error) carrying a `@StringRes` message; repositories throw, ViewModels catch and map the exception with `toUserMessageRes()`, and every error offers *Retry*. The paged home list instead reads `LazyPagingItems.loadState`: `RecipePagingSource` returns `LoadResult.Error`, shown as an error state with `retry()`.
- **Navigation**: Navigation Compose with type-safe routes (list → details).
- **Version Catalog** (`gradle/libs.versions.toml`) and *type-safe project accessors*.

## Tests

- `RecipeFilteringTest`: filter rules of the mock API.
- `RecipePagingSourceTest`: DTO → domain mapping, page keys, load errors, forwarded filters and bounded memory on long scrolls (`TestPager`).
- `DefaultRecipeRepositoryTest`: paging stream, 404 handling and batch lookup.
- `DataStoreFavoritesRepositoryTest`: favorites persistence with a temporary DataStore.
- `ObserveFavoriteRecipesUseCaseTest`: combines favorite ids with recipes.
- `HomeViewModelTest`: debounce, immediate first load, conversion of filters into a query, servings sanitization.
- `FavoritesViewModelTest`: loading, empty favorites, repository error and retry.
- `RecipeDetailViewModelTest`: favorite state, recipe/favorite errors, retry and failure when favoriting (Robolectric).
- `HomeScreenTest`, `FavoritesScreenTest` and `RecipeDetailScreenTest`: Compose UI tests (Robolectric, no emulator) covering rendered states, search/filter callbacks, retry, navigation callbacks and favorite toggling.

## Assumptions, trade-offs and limitations

- Search runs in `MockRecipeApi`, in memory; the data does not come from a real network (the base URL is a placeholder).
- Ingredients are compared by substring (e.g. `chicken` matches `chicken breast`).
- There is no recipe cache: only favorites are persisted (DataStore).
- The local mock still filters the simulated catalog in memory; in production the backend runs filtering and pagination. Favorites query only the persisted IDs.
- The first page is not cached on disk (no Room/`RemoteMediator`); offline start-up would need it.
- The domain layer is a package, not a module, so nothing stops it from importing Android classes except review. I kept it that way because there is one feature. With a second feature or slow builds I would extract `:core:domain` (plain Kotlin/JVM) and `:core:data`/`:core:network` behind it.
- UI texts live in `strings.xml` (English, `values/`) of `:app` and `:designsystem`; there are no translations yet, only the infrastructure to add them.
- Gradle stays plain: plugins, dependencies and SDK levels come from the version catalog, each module configures itself in its own `build.gradle.kts`, and only Detekt is configured once in the root build. With two modules, convention plugins (`build-logic`) would be more indirection than they save; I would introduce them with a third module.
- Release builds are shrunk with R8 (`isMinifyEnabled`, `isShrinkResources`). Signing is read from the environment (`RELEASE_KEYSTORE_PATH`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`) and `VERSION_CODE` sets the version code, so `./gradlew :app:bundleRelease` produces a Play-ready signed `.aab`; without those variables it stays unsigned. CI builds the unsigned bundle. Publishing to the Play Console and a Baseline Profile are not set up yet.

## Instrumented tests

The app's main flows are covered in
`app/src/androidTest/kotlin/com/recime/challenge/MainActivityTest.kt`. The
scenarios use `createAndroidComposeRule<MainActivity>()` to interact with
Compose's accessible semantics (text and content descriptions). Run them on
an emulator or a connected device with:

```bash
./gradlew connectedDebugAndroidTest
```

## Continuous integration

The `Android CI` workflow runs on pushes to `main`, pull requests and
manually. It runs Detekt, the unit tests, the
instrumented tests on an Android API 35 emulator, publishes a ViewModel
coverage report (no minimum threshold, so coverage never blocks a change) and
prepares the change review context on the pull request. The HTML report is
available in the `viewmodel-coverage` artifact; the review generates the diff and the changed-lines map as an
artifact for analysis, without posting comments automatically.

The same checks run locally before a push. `.githooks/pre-push` calls
`.github/scripts/run_ci.sh` (whitespace, Detekt, unit tests, ViewModel
coverage and `:app:bundleRelease`). Instrumented tests run in that script
only when a device or emulator is already online. Enable the hook once per
clone with `git config core.hooksPath .githooks`. A failing check aborts
the push.
