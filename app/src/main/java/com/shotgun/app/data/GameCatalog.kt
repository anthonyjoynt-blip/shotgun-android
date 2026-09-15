package com.shotgun.app.data

import com.shotgun.app.model.GameDef
import com.shotgun.app.model.GameType

/**
 * Every game the app offers. Adding a new game (e.g. "Song Snippet Guess")
 * is one more entry here, keyed to an existing GameType, unless it needs
 * a genuinely new play pattern — in which case add a GameType + one screen.
 */
object GameCatalog {
    val games = listOf(
        GameDef(
            id = "guess_it",
            name = "Guess It",
            type = GameType.GUESS20,
            description = "20 yes/no questions",
            emoji = "\uD83D\uDC3E",
            minPlayers = 2
        ),
        GameDef(
            id = "alphabet_hunt",
            name = "Alphabet Hunt",
            type = GameType.SEQUENCE,
            description = "Spot A\u2192Z on signs",
            emoji = "\uD83D\uDD24"
        ),
        GameDef(
            id = "punch_tally",
            name = "Punch Tally",
            type = GameType.TALLY,
            description = "Tap what you spot",
            emoji = "\uD83D\uDE99"
        ),
        GameDef(
            id = "plate_bingo",
            name = "Plate Bingo",
            type = GameType.BINGO,
            description = "First to a line wins",
            emoji = "\uD83C\uDD7F\uFE0F"
        ),
        GameDef(
            id = "categories",
            name = "Categories",
            type = GameType.ELIMINATION,
            description = "Name one in 5 seconds",
            emoji = "\uD83D\uDDC2\uFE0F",
            minPlayers = 2
        ),
        GameDef(
            id = "two_truths",
            name = "Two Truths, One Lie",
            type = GameType.POINTS,
            description = "Guess the fake",
            emoji = "\uD83E\uDD25",
            minPlayers = 3
        )
    )

    fun byId(id: String): GameDef? = games.find { it.id == id }
}
