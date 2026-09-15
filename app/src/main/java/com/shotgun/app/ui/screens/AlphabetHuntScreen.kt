package com.shotgun.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shotgun.app.model.GameDef
import com.shotgun.app.model.Player
import com.shotgun.app.state.SessionViewModel
import com.shotgun.app.ui.theme.BebasNeue
import com.shotgun.app.ui.theme.Ink
import com.shotgun.app.ui.theme.InkSoft

private const val LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

/**
 * The GameType.SEQUENCE screen: every rider hunts A→Z in order on signs and
 * plates. Each letter found is a +1 ScoreEvent, so a rider's position in the
 * alphabet is just their tally for this game — nothing extra to persist.
 */
@Composable
fun AlphabetHuntScreen(
    game: GameDef,
    session: SessionViewModel,
    onFinished: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text(
            "Tap when you spot your next letter — first to Z wins",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(session.players, key = { it.id }) { player ->
                val found = session.tally(player.id, game.id).coerceIn(0, LETTERS.length)
                LetterCard(
                    player = player,
                    found = found,
                    onFound = {
                        session.addScore(player.id, game.id, points = 1, note = "found ${LETTERS[found]}")
                    },
                    onUndo = { session.undoLastScore(player.id, game.id) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onFinished,
            colors = ButtonDefaults.buttonColors(containerColor = Ink),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("END ROUND", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LetterCard(player: Player, found: Int, onFound: () -> Unit, onUndo: () -> Unit) {
    val done = found >= LETTERS.length
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(player.colorHex))
            .clickable(enabled = !done, onClick = onFound)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(player.avatar, fontSize = 28.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                if (done) "made it to Z!" else "$found of ${LETTERS.length} · looking for",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp
            )
        }
        // The letter to hunt next, in the display face so it reads at a glance from the back seat.
        Text(
            if (done) "✓" else LETTERS[found].toString(),
            color = Color.White,
            fontFamily = BebasNeue,
            fontSize = 44.sp
        )
        Spacer(Modifier.width(14.dp))
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (found > 0) 0.25f else 0.1f))
                .clickable(enabled = found > 0, onClick = onUndo),
            contentAlignment = Alignment.Center
        ) {
            Text("−", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
