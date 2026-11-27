# Migration plan: ProPodcastN → Kotlin Multiplatform

The source app is [ProPodcastN](https://github.com/PrimakPavel/ProPodcastN) (Android, Jetpack Compose).
We move it here stage by stage: each stage builds and runs on Android and iOS, and its tests pass on both
before the next one starts.

Status: ✅ done · 🔄 in progress · ⏳ not started

## Stages

| # | Stage | Status | Commit |
|---|---|---|---|
| 0 | Empty KMP project: `shared` (Compose Multiplatform), `androidApp`, `iosApp` | ✅ | `4c2f39c`, `4617704`, `8094071` |
| 1 | `core/model` → `commonMain` | ✅ | `d22f381` |
| 2 | Network: Retrofit/OkHttp → Ktor, DTOs, `AppError`, API config (BuildKonfig) | ✅ | `5e6221b` |
| 3 | Data: Room KMP, DataStore, repositories, Paging | ✅ | `7f30c01` |
| 4 | DI: Hilt → Koin | ✅ | `cd7e77e` |
| 5 | Design system and resources: theme, colors, typography, `strings.xml` → Compose Resources | ✅ | `52e3242` |
| 6 | UI: navigation, screens, shared widgets, adaptive layout (3 parts, see notes) | 🔄 | |
| 7 | Platform services: player, downloads (incl. `DownloadsRepository`), connectivity, share | ⏳ | |
| 8 | Logging and monitoring: Timber → Kermit, Firebase Crashlytics and Analytics | ⏳ | |
| 9 | Home screen widget: Glance (Android), WidgetKit (iOS) | ⏳ | |
| 10 | UI tests and a full feature check against ProPodcastN | ⏳ | |

### Stage notes

**1. Model.** Same package `com.propodcast.app.core.model`. `java.time.Instant` → `kotlin.time.Instant`.
Tests on `kotlin.test` in `commonTest`.

**2. Network.** `ListenNotesApi` becomes a class over Ktor `HttpClient`. OkHttp interceptors become Ktor plugins
(API key header, quota headers, logging). Engines: OkHttp on Android, Darwin on iOS. `MockWebServer` tests
move to Ktor `MockEngine`. Base URL and API key come from `local.properties` through BuildKonfig
(mock server by default, `LISTEN_NOTES_USE_PRODUCTION=true` for the real API). `AppError` maps Ktor
`ResponseException` codes and `kotlinx.io.IOException`. The start screen shows a temporary list of best podcasts
as a network check; it goes away with the UI stage. DI wiring (the old `NetworkModule`) waits for stage 4.

**3. Data.** Room 2.8 with `BundledSQLiteDriver` and `@ConstructedBy` on both platforms; KSP generates Room code
per target; schemas (and the 1 → 2 auto-migration) live in `shared/schemas`. Database and DataStore files:
Android uses the usual app folders, iOS uses Documents. DataStore via `createWithPath` (okio).
Repositories (podcasts, favorites, preferences, API quota), the reference data cache and the paging sources moved
without Hilt annotations; `java.time.Clock` → `kotlin.time.Clock`, Timber → Kermit, `ConcurrentHashMap` → atomic
compare-and-set. The analytics interface and events moved too (Firebase stays for stage 8).
`DownloadsRepository` is tied to WorkManager and files, so it moves with downloads in stage 7.
Tests: Room tests extend `DatabaseTest`, which runs them under Robolectric on the Android host (framework SQLite)
and as plain tests on iOS (bundled SQLite). `AppStorage` holds the database and DataStores until DI (stage 4);
the start screen shows a temporary storage check.

**4. DI.** Koin 4.2 (chosen over Metro). Modules in `core/di`: `networkModule` (config, HTTP client, API, quota),
`dataModule` (DAOs, cache, repositories, `Clock`, analytics), and `platformModule` per platform (database and the
two DataStores, told apart by `DataStoreQualifiers`). Started in `ProPodcastApplication` on Android
(`androidContext`) and in the SwiftUI app's `init` on iOS (`KoinKt.doInitKoinIos()`). Composables get
dependencies with `koinInject()`; ViewModels (`koin-compose-viewmodel`) come with the UI stage.
`KoinModulesTest` resolves the whole graph with in-memory storage on both platforms.
Player, downloads and connectivity modules come with stage 7, Firebase analytics with stage 8.

**5. Design system and resources.** Theme, colors, typography and the animated color change moved to
`core/designsystem/theme`; Material You (`dynamicColorScheme`) is Android 12+ only, iOS uses the app palette.
The theme follows the Settings value (`ThemeMode`) as in ProPodcastN's `MainActivity`; `SystemAppearanceEffect`
makes system chrome follow it (bar icons via `enableEdgeToEdge` on Android, the window's
`overrideUserInterfaceStyle` on iOS, which also fixes the status bar text color).
All strings, plurals included, are in `composeResources/values/strings.xml` (`Res.string.*`, `Res.plurals.*`).
Compose Resources only formats indexed arguments, so `%d` became `%1$d` and `%%` became `%`.
The app's vector icons are in `composeResources/drawable`; vectors must use literal colors
(`@android:color/white` crashed the parser). Material icons stay on `material-icons-core` 1.7.3 from JetBrains
(no longer updated, but has iOS builds); replacing them with vectors can come later.
The Android launcher icon and window theme are in `androidApp`. The iOS app icon is still the empty placeholder.
Dates: kotlinx-datetime has no locale-aware formatting, so the quota banner date ("Resets on Dec 25, 2026")
needs a custom format or per-platform formatting (UI stage).

**6. UI.** One Compose Multiplatform UI for both platforms (Material 3 on iOS too). Done in three parts:

- 6.1 ✅ (`b8bf2ff`) Shared widgets (`core/commonwidgets`) and UI helpers (`core/ui`). Platform pieces as expect/actual:
  medium date format (`DateTimeFormatter` / `NSDateFormatter`), file size (`Formatter` / `NSByteCountFormatter`),
  share sheet (`rememberLinkSharer`: chooser / `UIActivityViewController`), notification permission (Android 13+;
  a pass-through on iOS). HTML descriptions use the HtmlConverter library on both platforms (Compose's
  `fromHtml` is Android-only). Images: Coil 3 with the Ktor network fetcher. `SnackbarEvent` takes
  `StringResource`s. `PlayerController`, `ConnectivityMonitor` and `DownloadsRepository` (now `localFilePath`)
  moved as interfaces with stub implementations in `platformServicesModule` until stage 7.
- 6.2 ✅ Navigation (JetBrains `navigation-compose`, type-safe routes as before), the app shell
  (`NavigationSuiteScaffold`: bottom bar on phones, rail on tablets and in phone landscape, from JetBrains
  Material 3 adaptive), Home, Filters, Search, Details and the download actions (`feature/downloads`, on the stub
  repository). ViewModels come from Koin (`featureModule`, `koinViewModel()`; `SavedStateHandle` from the
  navigation entry). JetBrains Material 3 1.9 still marks `TopAppBar` experimental, so `shared` opts in to
  `ExperimentalMaterial3Api`. Favorites and Settings tabs show a placeholder until 6.3; episodes go to the stub
  player. The iOS edge swipe goes back (Compose's `UIScreenEdgePanGestureRecognizer`).
  The temporary check screen and `platformName()` are gone: the app opens on Best podcasts.
- 6.3 ⏳ Favorites, Downloads, Settings, network banner, mini and full player.

Plurals: Compose Resources picks plural rules by the device language even when only English strings exist,
so a Ukrainian device showed "161 episode" (Ukrainian "one" covers 21, 31, 161…; ProPodcastN on Android has the
same bug). No library fixes it; `englishPluralStringResource` applies English rules (1 → one, else other) to the
same `<plurals>`. Use it instead of `pluralStringResource` until the strings are translated.

**7. Platform services** (common interface, separate implementation per platform):

| Service | Android | iOS |
|---|---|---|
| Player | Media3 ExoPlayer + MediaSession | `AVPlayer`, `AVAudioSession`, `MPNowPlayingInfoCenter`, `MPRemoteCommandCenter` |
| Downloads | WorkManager | background `URLSession`, `allowsCellularAccess = false` for Wi‑Fi only |
| Connectivity | `ConnectivityManager` | `NWPathMonitor` |
| Share | `Intent.ACTION_SEND` | `UIActivityViewController` |
| Dynamic color | Material You | static palette |

## Dependency map

| ProPodcastN | KMP |
|---|---|
| Compose BOM (androidx) | Compose Multiplatform (`org.jetbrains.compose.*`) |
| Navigation Compose | `org.jetbrains.androidx.navigation` |
| Lifecycle / ViewModel | `org.jetbrains.androidx.lifecycle` |
| Material 3 Adaptive Navigation Suite | `org.jetbrains.compose.material3.adaptive` |
| Material Icons Core | JetBrains `material-icons-core` 1.7.3 (app icons as vectors in `composeResources`) |
| `strings.xml` | Compose Resources (`Res.string.*`) |
| Hilt, Hilt Navigation, Hilt Work | Koin |
| Retrofit + converter | Ktor Client + ContentNegotiation |
| OkHttp interceptors | Ktor plugins |
| kotlinx.serialization | same |
| Room | same (KMP) |
| DataStore Preferences | same (KMP) |
| Paging 3 | same (KMP) |
| Coil 3 | same, `coil-network-ktor3` |
| WorkManager | Android only; iOS background `URLSession` |
| Media3 | Android only; iOS `AVPlayer` |
| Glance | Android only; iOS WidgetKit |
| Timber | Kermit (added in stage 3) |
| Firebase Crashlytics / Analytics | GitLive Firebase SDK or native SDKs behind `AnalyticsTracker` |
| `BuildConfig` | BuildKonfig |
| `java.time` | `kotlin.time.Instant`, kotlinx-datetime |
| `java.io.File` | okio / kotlinx-io |
| JUnit 4 | `kotlin.test` |
| MockWebServer | Ktor `MockEngine` |
| Robolectric | stays in Android host tests only |

## Conventions

- Package names stay as in ProPodcastN (`com.propodcast.app.*`), so code moves without import changes.
- Android application id is `com.propodcast.app` + `propodcast.applicationIdSuffix` from `gradle.properties`
  (`.kmm`), so both apps install side by side. iOS bundle id: `com.propodcast.app.kmm`.
- `local.properties`, keystores and `gradle/gradle-daemon-jvm.properties` stay out of git.
- When a stage is finished, its status and commit go into the table above in the same change.

## Commands

```bash
./gradlew :androidApp:installDebug
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

iOS app: open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme.
