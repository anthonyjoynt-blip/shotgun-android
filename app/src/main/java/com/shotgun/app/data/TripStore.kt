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

    data class Snapshot(val players: List<Player>, val history: List<ScoreEvent>)

    suspend fun load(): Snapshot {
        val prefs = store.data.first()
        return Snapshot(
            players = decode(prefs[PLAYERS]),
            history = decode(prefs[HISTORY])
        )
    }

    suspend fun save(players: List<Player>, history: List<ScoreEvent>) {
        store.edit { prefs ->
            prefs[PLAYERS] = json.encodeToString(players)
            prefs[HISTORY] = json.encodeToString(history)
        }
    }

    suspend fun clear() {
        store.edit { it.clear() }
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
        val PLAYERS = stringPreferencesKey("players")
        val HISTORY = stringPreferencesKey("history")
    }
}
