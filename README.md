# Shotgun — Android

A Jetpack Compose app of road-trip games. Riders join once per trip, every game
appends score events, and the scoreboard is always the sum of those events.

## What's here

- **Data model** (`model/Models.kt`): `Player`, `GameDef`, `ScoreEvent`.
  Scores are never stored directly — the scoreboard is always the sum of
  `ScoreEvent`s, so any game just needs to append events.
- **Game catalog** (`data/GameCatalog.kt`): the six games, declared as data.
- **Persistence** (`data/TripStore.kt`): players and the score log are saved
  to Preferences DataStore as JSON after every change, so a trip survives the
  app being closed. "Start a new trip" on the scoreboard wipes it.
- **Session state** (`state/SessionViewModel.kt`): players + score log for
  the current trip, shared across screens via Jetpack Navigation.
- **Screens**: Game Library → Player Setup → *game* → Scoreboard.
  `NavGraph.kt` dispatches on `GameType`; playable so far:
  - **Guess It** (`GUESS20`) — pick a guesser, 20 yes/no questions, fewer
    questions = more points.
  - **Punch Tally** (`TALLY`) — one card per rider, tap to add one, − to undo.

  Alphabet Hunt, Plate Bingo, Categories and Two Truths route to
  `ComingSoonScreen` until they get a screen of their own.
- **Theme**: coral/teal/gold/navy palette (`ui/theme/Color.kt`); Bebas Neue
  for display text and Inter for body, bundled in `res/font/` (both SIL OFL —
  see `FONT_LICENSES.txt`).

## Building

Open the folder in Android Studio, or from a terminal:

    ./gradlew assembleDebug

Toolchain: Gradle 9.3 wrapper, AGP 8.13, Kotlin 2.0.20 with the Compose and
serialization plugins, compileSdk 36, minSdk 26. `local.properties` (SDK path)
is machine-specific and git-ignored; Android Studio creates it on first open.

## Adding a game

1. Add a `GameDef` to `GameCatalog` with an existing `GameType`, or add a new
   `GameType` if it's a genuinely new play pattern.
2. Write one screen that takes `(game, session, onFinished)` and calls
   `session.addScore(...)` as play happens — see `TallyScreen.kt` for the
   smallest example.
3. Add a branch for that `GameType` in `NavGraph.kt`.

## Next

- Screens for the four remaining games (Alphabet Hunt is `POINTS`, Plate
  Bingo is `BINGO`, Categories is `ELIMINATION`, Two Truths is `POINTS`).
- Guess It's category is hard-coded to "Animal".
