package com.shotgun.app.state

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shotgun.app.data.TripStore
import com.shotgun.app.model.Player
import com.shotgun.app.model.ScoreEvent
import kotlinx.coroutines.launch

/**
 * Scoped to the activity via viewModel() so it survives navigation between
 * screens for the length of one trip. The scoreboard is always derived by
 * summing ScoreEvents rather than stored directly, so every game just
 * appends events and never needs to know about anyone else's running total.
 *
 * The trip is also written to disk (TripStore) after every change, so closing
 * the app mid-drive doesn't lose the riders or the scores.
 */
class SessionViewModel(app: Application) : AndroidViewModel(app) {

    private val store = TripStore(app)

    val players = mutableStateListOf<Player>()
    val history = mutableStateListOf<ScoreEvent>()
    val activeGameId = mutableStateOf<String?>(null)

    /** False until the saved trip has been read; writes before then would clobber it. */
    private var loaded = false

    init {
        viewModelScope.launch {
            val saved = store.load()
            players.addAll(saved.players)
            history.addAll(saved.history)
            loaded = true
        }
    }

    fun addPlayer(name: String, colorHex: Long) {
        if (name.isBlank()) return
        players.add(Player(name = name.trim(), colorHex = colorHex))
        persist()
    }

    fun removePlayer(id: String) {
        players.removeAll { it.id == id }
        persist()
    }

    fun addScore(playerId: String, gameId: String, points: Int, note: String? = null) {
        history.add(ScoreEvent(playerId = playerId, gameId = gameId, points = points, note = note))
        persist()
    }

    /** Drop this player's most recent event in one game — a mis-tap undo, not a score edit. */
    fun undoLastScore(playerId: String, gameId: String) {
        val last = history.lastOrNull { it.playerId == playerId && it.gameId == gameId } ?: return
        history.remove(last)
        persist()
    }

    /** Wipe riders and scores, on screen and on disk. */
    fun newTrip() {
        players.clear()
        history.clear()
        viewModelScope.launch { store.clear() }
    }

    /** Player's points within a single game, e.g. the running count in a tally game. */
    fun tally(playerId: String, gameId: String): Int =
        history.filter { it.playerId == playerId && it.gameId == gameId }.sumOf { it.points }

    /** Player -> total points across the whole trip, highest first. */
    fun scoreboard(): List<Pair<Player, Int>> {
        return players
            .map { player -> player to history.filter { it.playerId == player.id }.sumOf { it.points } }
            .sortedByDescending { it.second }
    }

    private fun persist() {
        if (!loaded) return
        val p = players.toList()
        val h = history.toList()
        viewModelScope.launch { store.save(p, h) }
    }
}
