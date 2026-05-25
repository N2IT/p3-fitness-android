# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Set JAVA_HOME (required on this machine)
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run all unit tests
./gradlew test

# Run all instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean assembleDebug
```

Single-module project — all Gradle tasks target `:app`.

## Architecture

**MVVM with StateFlow** — single-activity Compose app, no DI framework.

- **Data layer:** Room database (`FitTrackDatabase`) with 5 entities and DAOs. Database uses destructive migration (no versioned migrations).
- **UI layer:** Jetpack Compose screens collect `StateFlow` from ViewModels via `.collectAsState()`. ViewModels are created through manual factory pattern (no Hilt/Dagger).
- **Navigation:** Jetpack Navigation Compose with string routes defined in `NavRoutes`. Route arguments use Int IDs for entity lookups. NavHost lives in `MainActivity`.
- **Networking:** OkHttp for HTTP; Claude API (Haiku 4.5) integration in `RoutineImporter` for parsing workout URLs into structured routines. API keys stored via encrypted SharedPreferences (`ApiKeyManager`).
- **Async:** All background work runs in `viewModelScope` on `Dispatchers.IO`.

## Package Layout (`com.fittrack`)

```
data/
  entity/     # Room @Entity data classes (User, WorkoutRoutine, Exercise, RoutineExercise, ExerciseLog)
  dao/        # Room @Dao interfaces
  api/        # ApiKeyManager, RoutineImporter (Claude API client)
ui/
  screen/     # One Composable per screen (LoginScreen, WorkoutScreen, etc.)
  viewmodel/  # ViewModels with StateFlow state + ViewModelFactory classes
  components/ # Shared Compose components (FitTrackComponents)
  navigation/ # NavRoutes constants
  theme/      # Material3 theme (Color, Theme, Type) — dark-first with electric blue/orange palette
```

## Database Schema

Foreign key chain: **User → WorkoutRoutine → RoutineExercise ← Exercise → ExerciseLog**. RoutineExercise is a junction table. Cascade deletes: User→Routines, Exercise→Logs.

## Key Conventions

- State sealed classes for multi-step flows (e.g., `ImportState`: Idle, Loading, Preview, Error, Success)
- Manual JSON parsing with `org.json` (no Gson/Moshi)
- Dark theme is the primary theme — custom color palette in `Color.kt` with gradient backgrounds
- Animations: fade-in/slide for lists, sweep gradient progress ring in workouts, cross-fade navigation
- compileSdk/targetSdk 35, minSdk 26, Java 17
