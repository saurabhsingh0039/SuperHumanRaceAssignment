# SuperHumanRace: R2 System Assignment (Android)

A sustainability companion app built with **Kotlin, Jetpack Compose and MVVM**. Users log in, complete daily activities, see friends on a map, chat with a bot (Ora), compare themselves on a leaderboard, like and comment on mission posts, and review their CO₂ impact by week or month.

All data is served from mock JSON in `assets/mock_data.json` behind repository interfaces, so a real backend can replace it without touching the UI or ViewModels.

- **Repository:** `https://github.com/saurabhsingh0039/SuperHumanRaceAssignment`
- **Release APK:** `https://github.com/saurabhsingh0039/SuperHumanRaceAssignment/releases/download/v1.0/SuperHumanRace-v1.0.apk`
- **Demo login:** `ava@example.com` / `password123` (every mock user shares this password)

## Screens

| Screen | What it does |
|---|---|
| Login / Sign up | Email + password with inline validation, loading and error states. Session survives app restarts. The Apple / Google / Facebook buttons are mocked and sign in as the demo user. |
| Home | Currency chips (XP, seeds, gems), streak, today's activities (activate, then earn XP), mobility summary, shortcuts to Map and Chat. |
| Missions (Feed) | Posts with **Like** (optimistic, with rollback on failure) and **Comment**. |
| Leaderboard | League standings with competition ranking, current user highlighted. |
| Profile | **Performance** tab (stats) and **Impact** tab (report). |
| Impact report | **Weekly / Monthly** filter, daily CO₂ chart, transport-mode usage bars, totals. |
| Map | Nearby active friends on OpenStreetMap, device location, search, friend card with distance. |
| Chat (Ora) | Text chatbot with typing indicator, starter chips, retry, new chat. |
| Settings | Edit name, theme (System / Light / Dark), distance unit (km / miles), notification preference, log out. |

Every data-backed screen has **Loading, Success and Error (with Retry)** states.

## Architecture (MVVM)

```
View (Compose)  →  ViewModel (StateFlow)  →  Repository (interface)  →  Mock implementation
                                                                         ↳ assets/mock_data.json
                                                                         ↳ DataStore (session, settings)
```

- **UI layer** (`ui/<feature>/`): stateless composables take a state object and callbacks. A thin `*Route` composable wires them to a `@HiltViewModel`.
- **State**: each ViewModel exposes one `StateFlow`. Async data uses a sealed `UiState<T>` (`Loading` / `Success` / `Error`), produced by `Result<T>.toUiState()`.
- **Domain** (`domain/`): pure Kotlin objects with no Android dependencies, so they run in plain JVM tests: `KeywordResponder`, `ReportBuilder`, `Co2Calculator`, `AuthValidator`, `DistanceFormatter`, `MarkdownStripper`, `TimeAgo`, `GeoMath`.
- **Data** (`data/`): repository interfaces in `data/repository/`, mock implementations in `data/repository/mock/`, DTOs and the asset loader in `data/remote/`, DataStore storage in `data/local/`.
- **DI** (`di/`): Hilt `@Binds` modules map each interface to its implementation. Moving to a real API means adding one implementation per interface and changing one binding.
- **Navigation**: `AppRoot` switches between Splash, the auth graph and the signed-in shell based on session state. The shell has four tabs (Home, Missions, Leaderboard, Profile) plus pushed screens (Map, Chat, Settings).
- **Concurrency**: in-flight guards and job cancellation prevent out-of-order or duplicate results (Like taps, report period switches, map search, chat refresh).

## Libraries

| Purpose | Library |
|---|---|
| UI | Jetpack Compose (BOM 2024.09.00), Material 3, Material Icons Extended |
| Navigation | Navigation Compose 2.8.3 |
| DI | Hilt 2.51.1 (+ hilt-navigation-compose), KSP |
| Async | Kotlin Coroutines, StateFlow |
| Persistence | DataStore Preferences (session and settings) |
| Data | kotlinx-serialization-json (mock data from assets) |
| Map | osmdroid 6.1.x (OpenStreetMap) |
| Language | Kotlin 2.0.21, `java.time` (minSdk 26) |
| Tests | JUnit 4, kotlinx-coroutines-test |

Charts are drawn with plain Compose (no chart library). Location uses the platform `LocationManager`, so there is no Play Services dependency.

## APIs and data

There is **no network API**. `AssetMockDataSource` parses `mock_data.json` (8 users, 6 posts with comments, 41 daily activity logs, friends as offsets from the device location, chat rules, 6 activities). A 500 ms delay in the repositories simulates latency so loading states are visible. The only network use is map tile download from OpenStreetMap.

## Testing summary

43 JVM unit tests, run with `./gradlew testDebugUnitTest`.

- **Domain (22):** chatbot keyword matching, markdown stripping, distance conversion, validation, CO₂ maths, relative time, haversine distance, weekly/monthly report building.
- **ViewModels (12):** `ChatViewModel` (greeting load/error/retry, blank input, typing state, double-send guard, refresh cancels a late reply, reply failure/retry) and `SettingsViewModel` (load, validation, save, failure). They use hand-written fakes and virtual time.
- **Data layer / "API services" (9):** `MockAuthRepository` (login, case-insensitive email, bad password, sign-up, duplicate email, session restore, logout) and `MockChatRepository` (greeting, keyword replies, fallback).

Not covered: Compose UI tests and instrumented tests. Release behaviour was checked manually on an emulator.

## Build and run

1. Open in Android Studio and let Gradle sync.
2. Run the `app` configuration, or `./gradlew installDebug`.
3. Release build: create `keystore.properties` in the project root (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`; the file and the `.jks` are git-ignored), then run `./gradlew assembleRelease`. R8 minification and resource shrinking are enabled, with keep rules in `app/proguard-rules.pro`.

## Design decisions and deviations

- **OpenStreetMap instead of Google Maps.** Google Maps needs an API key backed by a billing account. osmdroid needs neither. The map sits behind a single composable (`FriendsMapView`), so a Google Maps implementation can replace it.
- **Mock backend behind interfaces** rather than Firebase, to keep the app runnable with no accounts or keys.
- **Rule-based chatbot** (keyword rules in JSON) instead of an LLM, so replies are deterministic and testable.
- **Lint release check disabled** (`lint { checkReleaseBuilds = false }`): the `lintVitalAnalyzeRelease` task crashes inside the lint engine on this toolchain (an internal error, not a reported issue).

## Known limitations

- **Mock data only.** Sign-ups, name edits, likes and comments live in memory and reset when the process is fully closed. Only the session and the settings persist (DataStore).
- **Social login buttons are mocked** and sign in as the demo user.
- **Home activity progress** is in-memory and resets on restart.
- **Name edits** update the profile, but the Leaderboard and Feed read their own mock rows and may keep the old name.
- **Notifications toggle** stores a preference only. No notifications are sent.
- **Avatars** are initials; avatar upload is not implemented.
- **CO₂ emission factors** are approximate values chosen for the mock app. Verify them against a published source before relying on the numbers.
- **Some icons and mascots are placeholders** for the reference design's artwork.
- **Map** needs internet for tiles and a location permission for "near me". Without them it shows the mock default area.
- **`targetSdk 34`.** Raise it before a real Play Store submission, which requires a newer target.
- **Build warnings:** deprecated `CancellationSignal` / `getCurrentLocation` usage in `LocationProvider.kt`.
- **Accessibility** was designed in (content descriptions, 48 dp targets, live regions, font scaling) but not audited with automated tools.
