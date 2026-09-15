# Shotgun — Android

A Jetpack Compose app of road-trip games. Riders join once per trip, every game
appends score events, and the scoreboard is always the sum of those events.

## What's here

- **Data model** (`model/Models.kt`): `Player`, `GameDef`, `ScoreEvent`.
  Scores are never stored directly — the scoreboard is always the sum of
  `ScoreEvent`s, so any game just needs to append events.
- **Game catalog** (`data/GameCatalog.kt`): the six games, declared as data.
- **Roster and trip** (`state/SessionViewModel.kt`): the roster is everyone
  who has ever ridden, each with an emoji avatar (`data/Avatars.kt`); the
  trip's riders are a subset toggled on the setup screen (tap to ride, hold
  to forget). "New trip" clears riders and scores but keeps the roster.
- **Persistence** (`data/TripStore.kt`): roster, riders and the score log are
  saved to Preferences DataStore as JSON after every change, so closing the
  app mid-drive loses nothing.
- **Session state** (`state/SessionViewModel.kt`): players + score log for
  the current trip, shared across screens via Jetpack Navigation.
- **Screens**: Game Library → Player Setup → *game* → Scoreboard.
  `NavGraph.kt` dispatches on `GameType`, one screen per type:
  - **Guess It** (`GUESS20`) — pick a guesser, 20 yes/no questions, fewer
    questions = more points.
  - **Punch Tally** (`TALLY`) — one card per rider, tap to add one, − to undo.
  - **Alphabet Hunt** (`SEQUENCE`) — each rider hunts A→Z in order; the card
    shows the next letter they need.
  - **Categories** (`ELIMINATION`) — pick a category, five-second turns,
    the clock eliminates on its own, last rider standing wins.
  - **Two Truths, One Lie** (`POINTS`) — pick a teller, everyone else votes
    on the lie; +1 for spotting it, +1 to the teller per rider fooled.
  - **Plate Bingo** (`BINGO`) — a shuffled 3×3 card of plate spots per rider;
    first line wins a bonus, everyone banks a point per square.

  Round-in-progress state (a bingo card, an elimination order) lives in the
  screen; only the resulting `ScoreEvent`s are persisted.
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

- Long rounds (Plate Bingo, Alphabet Hunt) lose their in-progress state if
  the app is killed mid-round; Alphabet Hunt survives because its progress
  *is* its score events, Plate Bingo doesn't.
