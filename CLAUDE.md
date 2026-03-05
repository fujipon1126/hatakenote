# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HatakeNote（畑ノート）- Android app for home garden/rental farm management. Tracks plot layouts, planting records, work logs, crop rotation warnings, reminders, weather, and AI-assisted advice via Gemini.

**Language:** Kotlin, **Min SDK:** API 26, **Target:** Android 13+, **Package:** `com.example.hatakenote`

## Build Commands

```bash
./gradlew build                    # Full build
./gradlew :app:assembleDebug       # Debug APK
./gradlew :app:assembleRelease     # Release APK (requires signing config in local.properties)
./gradlew test                     # Run all unit tests
./gradlew :app:test                # Run app module unit tests
./gradlew :core:domain:test        # Run a specific module's tests
./gradlew :app:connectedAndroidTest  # Instrumented tests
```

Version bumping: `./gradlew :app:assembleRelease -PbumpVersion=true` (auto-increments in `version.properties`)

## Architecture

**MVVM + Clean Architecture** with multi-module structure, modeled after Now in Android.

### Module Dependency Graph

```
:app → :feature:* → :core:ui, :core:domain, :core:data
:core:data → :core:domain, :core:database, :core:network, :core:firestore
:core:database, :core:network → :core:domain
:core:domain → :core:common
```

### Layer Responsibilities

- **:core:common** — Pure Kotlin. Shared utilities, Result types. No Android dependency.
- **:core:domain** — Pure Kotlin. Domain models (`model/`), repository interfaces (`repository/`), use cases (`usecase/`).
- **:core:data** — Repository implementations, DI modules. Bridges domain with database/network/firestore.
- **:core:database** — Room DB (`HatakeDatabase`), DAOs, entities, initial master data (`InitialDataCallback`), type converters.
- **:core:firestore** — Firestore-backed repository implementations for cloud sync (Farm, Plot, Planting, Crop, WorkLog, etc.).
- **:core:network** — Retrofit/OkHttp clients for Open-Meteo weather API and Gemini AI SDK.
- **:core:ui** — Shared Compose components, theme (`HatakeNoteTheme` with green color scheme).
- **:feature:*** — Each feature has `Screen.kt`, `ViewModel.kt`, and `navigation/` with type-safe routes using `kotlinx.serialization`.
- **:app** — Entry point, Hilt setup, `HatakeNoteNavHost`, WorkManager initialization.

### Convention Plugins (build-logic)

Shared build configuration in `build-logic/convention/`:

| Plugin ID | Purpose |
|-----------|---------|
| `hatakenote.android.application` | App module |
| `hatakenote.android.library` | Android library modules |
| `hatakenote.android.feature` | Feature modules (library + compose + hilt + common deps) |
| `hatakenote.android.library.compose` | Compose setup |
| `hatakenote.android.hilt` | Hilt/KSP setup |
| `hatakenote.jvm.library` | Pure Kotlin modules (domain, common) |

### Key Tech Stack

- **DI:** Hilt (with `@HiltViewModel`, `@AndroidEntryPoint`)
- **DB:** Room with Flow-based DAOs, `kotlinx-datetime` `LocalDate` via TypeConverters
- **Navigation:** Navigation Compose with type-safe `@Serializable` route objects
- **Async:** Kotlin Coroutines + Flow / StateFlow
- **Image loading:** Coil
- **Serialization:** kotlinx.serialization
- **Background work:** WorkManager with Hilt integration

### Data Model

Core entities: `CropFamily` (crop families with rotation years) → `Crop` → `Planting` ↔ `Plot` (many-to-many via `PlantingPlotCrossRef`) → `WorkLog`, `Reminder`, `PlantingPhoto`, `FertilizerSchedule`, `RotationIncompatibility`.

WorkLog binding rule: `TILL`/`BASE_FERTILIZE` bind to `plotId`; `FERTILIZE`/`OTHER` bind to `plantingId`.

## Setup

API keys go in `local.properties` (gitignored):
```properties
GEMINI_API_KEY=...
FIREBASE_WEB_CLIENT_ID=...
RELEASE_STORE_FILE=...
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=...
RELEASE_KEY_PASSWORD=...
```

Dependency versions are managed in `gradle/libs.versions.toml`.