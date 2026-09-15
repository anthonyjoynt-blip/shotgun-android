package com.shotgun.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shotgun.app.model.Player
import com.shotgun.app.model.ScoreEvent
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.tripDataStore: DataStore<Preferences> by preferencesDataStore(name = "trip")

/**
 * Persists the current trip — players and the score log — so it survives the
 * app being closed. Two JSON blobs in Preferences DataStore is plenty for a
 * handful of riders and a few hundred events; no need for a database.
 */
class TripStore(context: Context) {

    private val store = context.applicationContext.tripDataStore
    private val json = Json { ignoreUnknownKeys = true }

    /** roster = everyone who has ever ridden; players = who's on this trip; history = this trip's score log. */
    data class Snapshot(val roster: List<Player>, val players: List<Player>, val history: List<ScoreEvent>)

    suspend fun load(): Snapshot {
        val prefs = store.data.first()
        return Snapshot(
            roster = decode(prefs[ROSTER]),
            players = decode(prefs[PLAYERS]),
            history = decode(prefs[HISTORY])
        )
    }

    suspend fun save(roster: List<Player>, players: List<Player>, history: List<ScoreEvent>) {
        store.edit { prefs ->
            prefs[ROSTER] = json.encodeToString(roster)
            prefs[PLAYERS] = json.encodeToString(players)
            prefs[HISTORY] = json.encodeToString(history)
        }
    }

    /** A blob written by an older model shape loses that trip rather than crashing the app. */
    private inline fun <reified T> decode(raw: String?): List<T> {
        if (raw.isNullOrEmpty()) return emptyList()
        return try {
            json.decodeFromString<List<T>>(raw)
        } catch (e: SerializationException) {
            emptyList()
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }

    private companion object {
        val ROSTER = stringPreferencesKey("roster")
        val PLAYERS = stringPreferencesKey("players")
        val HISTORY = stringPreferencesKey("history")
    }
}
