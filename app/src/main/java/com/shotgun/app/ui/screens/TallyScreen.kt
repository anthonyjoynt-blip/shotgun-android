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
import com.shotgun.app.ui.theme.Ink
import com.shotgun.app.ui.theme.InkSoft

/**
 * The GameType.TALLY screen: every rider gets one big card in their colour and
 * taps it each time they spot the thing. Each tap is a +1 ScoreEvent, so the
 * scoreboard needs nothing special — same plumbing as Guess It.
 */
@Composable
fun TallyScreen(
    game: GameDef,
    session: SessionViewModel,
    onFinished: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text(
            "Tap your card every time you spot one",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(session.players, key = { it.id }) { player ->
                TallyCard(
                    player = player,
                    count = session.tally(player.id, game.id),
                    onTap = { session.addScore(player.id, game.id, points = 1, note = "spotted one") },
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
private fun TallyCard(player: Player, count: Int, onTap: () -> Unit, onUndo: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(player.colorHex))
            .clickable(onClick = onTap)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("tap to add one", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
        }
        Text(
            count.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 38.sp
        )
        Spacer(Modifier.width(14.dp))
        // Undo sits inside the tappable card, so it must swallow its own click.
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (count > 0) 0.25f else 0.1f))
                .clickable(enabled = count > 0, onClick = onUndo),
            contentAlignment = Alignment.Center
        ) {
            Text("−", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
