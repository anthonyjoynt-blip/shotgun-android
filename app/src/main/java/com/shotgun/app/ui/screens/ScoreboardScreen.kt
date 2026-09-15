package com.shotgun.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.shotgun.app.model.Player
import com.shotgun.app.state.SessionViewModel
import com.shotgun.app.ui.theme.CardBg
import com.shotgun.app.ui.theme.Coral
import com.shotgun.app.ui.theme.Gold
import com.shotgun.app.ui.theme.Ink
import com.shotgun.app.ui.theme.InkSoft

@Composable
fun ScoreboardScreen(session: SessionViewModel, onNextGame: () -> Unit, onNewTrip: () -> Unit) {
    val ranked = session.scoreboard()

    Column(Modifier.fillMaxSize().padding(top = 24.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("SHOTGUN", style = MaterialTheme.typography.headlineMedium)
                Text("Scoreboard \u00b7 this trip", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
            }
            // Wipes the trip, so it lives up here, a whole screen away from NEXT GAME.
            OutlinedButton(onClick = onNewTrip) {
                Text("New trip", color = InkSoft, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(Modifier.padding(horizontal = 20.dp))

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(ranked) { index, (player, score) ->
                RankRow(rank = index + 1, player = player, score = score, isLeader = index == 0)
            }
        }

        Button(
            onClick = onNextGame,
            colors = ButtonDefaults.buttonColors(containerColor = Ink),
            // padding before height, or the button's content area shrinks to 20dp
            modifier = Modifier.padding(16.dp).fillMaxWidth().height(52.dp)
        ) {
            Text("NEXT GAME", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RankRow(rank: Int, player: Player, score: Int, isLeader: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isLeader) Ink else CardBg)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            rank.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = if (isLeader) Gold else InkSoft,
            modifier = Modifier.width(24.dp)
        )
        Text(player.avatar, fontSize = 22.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            player.name + if (isLeader) " \u2b50" else "",
            fontWeight = FontWeight.Bold,
            color = if (isLeader) Color.White else Ink,
            modifier = Modifier.weight(1f)
        )
        Text(
            score.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = if (isLeader) Gold else Coral
        )
    }
}
