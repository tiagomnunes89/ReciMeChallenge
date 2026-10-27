# Project rules — ReciMe Android Coding Challenge

Source: https://recime.notion.site/ReciMe-Android-Coding-Challenge-3fe720b31dfb81219dccc7ca14b5e8d5

## Goal

A native Android app (Kotlin + Jetpack Compose) to browse and search recipes. Each recipe has:
a title, description, number of servings, ingredients, preparation instructions and dietary attributes (e.g. vegetarian).

The data comes from a mocked API response (a local `.json` file), presented in a clean, intuitive interface with search and filters.

## Technical requirements (mandatory)

- Use Kotlin and Jetpack Compose.
- Target the latest stable Android SDK; **minSdk 26+**.
- Use Compose Previews (`@Preview`) on screens/components.
- Gradle dependencies are allowed when needed.
- Mock the API with a `.json` file (`assets/` or `res/raw/`). Currently: `app/src/debug/assets/recipes.json`, served by an OkHttp interceptor that exists only in the `debug` source set.

## Functional goals

1. **Recipe model**: define the `Recipe` model and load the recipes from the local JSON bundled in the app. Structure the code as if the data came from a real API.
2. **Screens and ViewModels**: present the recipes as a list or grid, with a clear and intuitive interface.
3. **Search**: a search endpoint/query with optional filters:
   - vegetarian
   - number of servings
   - include/exclude ingredients
   - search within the instructions content

## Deliverables

- An Android Studio (Gradle) project that builds with `./gradlew assembleDebug`.
- A `README.md` with: setup instructions, architecture overview, key design decisions, assumptions and trade-offs, known limitations.
- A public GitHub repository (or ZIP) with the URL emailed to nic@recime.app (CTO) and vinicius@recime.app (Engineering Lead).

## Evaluation criteria

- Logical and intuitive user experience.
- Use of Jetpack Compose frameworks and idioms.
- Naming conventions.
- Documentation of unclear assumptions and limits.
- Error handling and constraints where needed.
- Code style, design intuition and developer mindset.

## Stack and architecture

- Kotlin, Gradle Kotlin DSL (`.kts`), Version Catalog (`gradle/libs.versions.toml`).
- Jetpack Compose (Material 3), Hilt, Coroutines + StateFlow.
- Clean Architecture (domain / data / ui) in a single `:app` module, plus a reusable `:designsystem` module.
- JDK 17+ via `JAVA_HOME` (Android Studio's bundled JBR works). Gradle 9.6, AGP 9.4 and Kotlin 2.4; each module configures itself in its own `build.gradle.kts` (no convention plugins); Detekt is configured once in the root `build.gradle.kts`.
- Quality: Detekt (`config/detekt/detekt.yml`, zero issues), unit tests, instrumented tests and the `Quality gate` aggregate job are CI gates. Kover reports an advisory 80% line coverage target for the selected ViewModels (`.github/workflows/ci.yml`).

### Modules

| Module | Responsibility |
|---|---|
| `:app` | The application. Packages: `domain/` (models `Recipe`/`RecipeQuery`/`RecipePage`, `RecipeRepository`, `FavoritesRepository`), `data/` (`DefaultRecipeRepository`, DTO/entity → domain mappers, `data/local` Room database (recipe records and favorites), Hilt `@Binds`), `data/remote/` (`RecipeApi` Retrofit contract, DTOs, `Json`/OkHttp/Retrofit; the mocked backend — `MockApiInterceptor`, `assets/recipes.json`, the simulated 100-item catalog and `assets/images` — lives in `src/debug`), `ui/` (`home`, `detail`, `favorites`, `navigation`), plus `ReciMeApplication` and `MainActivity` |
| `:designsystem` | `ReciMeTheme` (Color, Type, Theme) and reusable components, including `ReciMeRecipeImage` (Coil). Knows nothing about the domain: components receive primitives, never `Recipe` |
| `:benchmark` | Macrobenchmarks and Baseline Profile generation against the non-debuggable `app:benchmark` build |

Why two modules: the design system has no domain dependency and is the part most likely to be reused, so the compiler enforces that boundary. Data, domain and UI stay in `:app` as packages because there is a single feature. Extract a `core` module (domain, data) only when a second feature or a build-time problem appears.

## Code conventions

- Dependencies always go through the version catalog (`libs.*`) and between modules via `projects.*`.
- `minSdk = 26`, `compileSdk = 37` (required by current AndroidX/OkHttp), `targetSdk = 36`, Java/Kotlin target 17.
- Package dependency rule inside `:app`: `ui` → `domain`; `data` → `domain`; `domain` imports neither `data` nor `ui` nor `androidx.compose`. Paging is assembled by `HomeViewModel` and `HomePagingSource`; `HomePagingSource` uses opaque cursor keys and the domain repository exchanges plain `RecipePage(recipes, nextCursor)` values. `:designsystem` never depends on `:app`.
- DTOs (`*Dto`) live in `data/remote/model` and are mapped to domain models in `data/mapper`; they never leak into `ui`.
- UI (MVVM + UDF/MVI-like): `*Route` connects the `@HiltViewModel`; `*Screen` is stateless and receives `UiState` + callbacks. ViewModels expose explicit event entry points when a screen has state-changing input (for example `HomeEvent` + `onEvent`); navigation callbacks remain route-owned.
- `UiState` is a `sealed interface` (Loading/Success/Error) and errors offer *Retry*. The paged home list instead reads `LazyPagingItems.loadState` (refresh/append) and retries with `LazyPagingItems.retry()`.
- Lists: always give `LazyGrid`/`LazyColumn` items a stable `key` and a `contentType`; pass state to children as lambdas (`() -> State`) when only a small part of the screen reads it.
- Do not add a use case that only forwards a repository call; inject the repository in the ViewModel.
- User-facing text lives in `strings.xml`; `UiState` carries `@StringRes` ids, never hardcoded text.
- Every new filter/mapping/ViewModel rule must have a unit test (`./gradlew testDebugUnitTest`). Tests live in `app/src/test/kotlin/com/recime/challenge/` mirroring the package of the code under test; shared fakes are in `testing/Fakes.kt`.
- Screen state is exposed as `StateFlow<UiState>` (`stateIn` with `WhileSubscribed(5_000)`); collect with `collectAsStateWithLifecycle()`.
- Always provide a `@Preview` for relevant new Composables, using `ReciMeTheme`.
- `RecipeRepository.searchRecipes` accepts `(query, cursor, limit)` and `RecipePage.nextCursor` is nullable; cursors are opaque strings.
- The `benchmark` app build type is non-debuggable, uses release shrinking with debug signing, and explicitly includes the debug mock source/assets. Release never includes the mock.
- The mocked backend stays at the HTTP boundary (an OkHttp `Interceptor` in `src/debug`, contributed through the Hilt `Set<Interceptor>` multibinding); never implement `RecipeApi` with a fake in production code. Tests that need the mock live in `src/testDebug`.
- Repositories throw; the ViewModel catches (`Flow.catch` or `try/catch`, always rethrowing `CancellationException`) and exposes `UiState.Error(@StringRes)` through `Throwable.toUserMessageRes()` (`ui/ErrorMessages.kt`). Expected failures have their own exception (`RecipeNotFoundException`, from an HTTP 404). Never leave a screen blank or stuck on loading.
- Classes/functions that don't need to be public should be `internal` or `private`.

## Workflow

- Validate changes with `./gradlew assembleDebug testDebugUnitTest detekt` before finishing.
- Keep `README.md` up to date when changing architecture, decisions or limitations.
- Record new assumptions and limitations in the README (an item ReciMe evaluates).
- All project documentation, code comments and commit messages are written in English.

## Agents and skills

Located in `.github/agents/` and `.github/skills/` (see `.github/skills/README.md`): local diff review before a pull request (`pre-push-review`), pull request review (`change-review`), unit test generation, and Android diagnosis from the terminal (`debug`).
