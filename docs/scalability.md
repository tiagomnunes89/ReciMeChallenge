# Scalability implementation notes

`RecipePage.nextCursor` and `RecipeRepository.searchRecipes` treat cursors as
opaque strings. `HomePagingSource` never parses or constructs them and always
refreshes from the top. The debug interceptor applies filters, sorts by recipe
ID, and returns the last ID as the next cursor. This makes inserts before a
cursor harmless to already loaded pages, but ordering remains a server
contract that must be validated against the production API.

The application OkHttp client deliberately does not create a disk cache during
startup. On Android emulators, the first access to `Context.cacheDir` can block
the main thread long enough to make the UI test harness time out. Offline
recovery is provided by the Room recipe store, while the retry interceptor
handles only GET, IOException and 5xx failures, closes retryable responses,
checks cancellation, and uses injectable sleep/random sources for deterministic
tests.

Compose compiler reports are opt-in:

```bash
./gradlew -PcomposeReports :app:compileDebugKotlin :designsystem:compileDebugKotlin
```

Reports are generated under each module's `build/compose-reports` and
`build/compose-metrics`. The stability configuration marks domain recipe
models stable; `RecipeGridCard` and `ReciMeRecipeCard` use stable primitive or
configured-stable parameters.

`ReciMeApplication` enables thread and VM StrictMode logging only for
debuggable builds. Application startup does not load the catalog or eagerly
create Room; the mock reads assets on the OkHttp worker when its first request
arrives.

The macrobenchmark module contains cold-start and scroll tests with
`CompilationMode.None()` and `CompilationMode.Partial()`, each using 10
iterations. The Baseline Profile generator is compile-checked but has not been
run here because no suitable device was available; it should write
`app/src/main/baseline-prof.txt` when run on an API 33+ device or emulator.
