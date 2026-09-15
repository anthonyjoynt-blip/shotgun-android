package com.shotgun.app.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * How a game turns raw play into points. New games are added by defining
 * a GameDef with one of these types (plus a screen that knows how to run
 * that type) rather than writing new scoring plumbing each time.
 */
enum class GameType { POINTS, TALLY, BINGO, ELIMINATION, GUESS20 }

// Player and ScoreEvent are what a trip is made of, so they're what gets
// persisted (see data/TripStore.kt). GameDef is static catalog data, not saved.
@Serializable
data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorHex: Long
)

data class GameDef(
    val id: String,
    val name: String,
    val type: GameType,
    val description: String,
    val emoji: String,
    val minPlayers: Int = 1,
    val maxPlayers: Int = 8
)

@Serializable
data class ScoreEvent(
    val id: String = UUID.randomUUID().toString(),
    val playerId: String,
    val gameId: String,
    val points: Int,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
