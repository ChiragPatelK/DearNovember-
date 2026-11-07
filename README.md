# Dear November 0.2.0

A promise to become someone I'm proud of. Offline-first Android app (Kotlin, Jetpack Compose, Material 3, Room, WorkManager). No account, no cloud, no analytics.

## STATUS: written but NOT compiled
This source was written in an environment with no Android SDK and no network, so it has never been built or run.
Expect a few compile errors on the first build; fix them in Android Studio, then run the unit tests.

## Build the APK
1. Install Android Studio (Ladybug or newer) with JDK 17 and SDK Platform 35.
2. File > Open this folder. Let Gradle sync (it downloads the wrapper and libraries).
3. Run unit tests: `./gradlew testDebugUnitTest` (scoring, streaks, calendar, reminders).
4. Build: `./gradlew assembleDebug`
5. APK: `app/build/outputs/apk/debug/app-debug.apk` (rename to DearNovember.apk). Copy to the phone and install.
   It is debug-signed: fine for your own phone, not a Play Store release. For a release key, add your own signingConfig.

## What is implemented
- Home: day, chapter, quote, mission, score ring, streak, six-pillar shortcuts, grouped checklist (Sunday swaps in recovery)
- 17 tasks = exactly 100 points (30/20/15/15/10/10). Saved transactionally; one row per (day, task), so no duplicate scoring
- Journey: six pillars + all 24 chapters
- Physical: weekly plan, exercise library (editable), set logging, PRs, history, protein/water/meals with custom foods (estimates), push-up baseline, Day 1/Day 24 photos
- Academics: 6 subjects, editable topics, study logs, 4 editable assignments due 30 Nov
- Mind: mood, 5-question check-in, daily mission, meditation
- Speaking: 4-step routine, random topics, word of the day, vocabulary notebook, history
- Discipline: wake schedule, sleep log, manual Instagram log, focus timer, habit streaks
- Intellect: chess log, Rubik's progression (7 stages), solve times, personal best
- Journal and Letters to Her: create, edit, delete; private, in backups
- Progress: totals, average, best day, streaks, 24-day chart, calendar with day editor, category and consistency bars, Day 1 vs Day 24
- Backup: daily automatic local backup (WorkManager), retention that never deletes the only copy, manual export (Storage Access Framework), restore with checksum + integrity validation, confirmation, automatic safety copy
- Reminders (off by default), app lock (biometric or device credential), light/night, romantic/cinematic themes, archive-and-restart

## Not implemented (honest list)
- Audio recording for speaking practice
- Vico/third-party charts (charts are drawn with Compose Canvas)
- Hilt and DataStore (manual DI; settings live in Room so they are inside backups)
- Habit and HabitCompletion tables exist but are unused (streaks derive from task completions)
- Anime artwork: the look is gradient-based; no illustrated assets are bundled
- Automatic Instagram usage monitoring (manual only, by design)

## Streak rules
A day counts at 60+. One missed day pauses the streak; two in a row reset it. An unfinished today never breaks it.

## Backup format (v1)
ZIP: manifest.json (formatVersion, createdAt, dbVersion, dbSha256), database/dear_november.db, photos/*.
