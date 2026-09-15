package com.shotgun.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shotgun.app.model.GameDef
import com.shotgun.app.model.Player
import com.shotgun.app.state.SessionViewModel
import com.shotgun.app.ui.theme.BebasNeue
import com.shotgun.app.ui.theme.CardBg
import com.shotgun.app.ui.theme.Gold
import com.shotgun.app.ui.theme.Ink
import com.shotgun.app.ui.theme.InkSoft
import com.shotgun.app.ui.theme.Line

private const val SIZE = 3
private const val BINGO_BONUS = 5

private val SPOTS = listOf(
    "Ontario plate", "Quebec plate", "Alberta plate", "BC plate", "US plate",
    "Out-of-province plate", "Vanity plate", "Plate that spells a word",
    "Plate with a 7", "Plate ending in 0", "Double letters", "Three of the same digit",
    "Temporary paper plate", "Motorcycle plate", "Trailer plate", "Commercial plate",
    "Dealer frame", "Green EV plate", "Plate with your initial", "Plate with a 1 and a 9"
)

// Every row, column and diagonal of the SIZE x SIZE card, as index sets.
private val LINES: List<Set<Int>> = buildList {
    for (r in 0 until SIZE) add((0 until SIZE).map { r * SIZE + it }.toSet())
    for (c in 0 until SIZE) add((0 until SIZE).map { it * SIZE + c }.toSet())
    add((0 until SIZE).map { it * SIZE + it }.toSet())
    add((0 until SIZE).map { it * SIZE + (SIZE - 1 - it) }.toSet())
}

/**
 * The GameType.BINGO screen. Each rider gets their own shuffled 3x3 card of
 * plate spots; the first completed line ends the round. Points are written
 * once, when the round ends: one per marked square for everyone, plus a
 * bonus for the bingo. Cards and marks live in the composable — like a
 * Categories round, a half-played card isn't persisted, only its points.
 */
@Composable
fun PlateBingoScreen(
    game: GameDef,
    session: SessionViewModel,
    onFinished: () -> Unit
) {
    val riders = session.players
    val cards = remember { riders.associate { it.id to SPOTS.shuffled().take(SIZE * SIZE) } }
    val marks = remember { mutableStateMapOf<String, Set<Int>>() }
    var selected by remember { mutableIntStateOf(0) }
    var winner by remember { mutableStateOf<Player?>(null) }
    var scored by remember { mutableStateOf(false) }

    fun finish(bingo: Player?) {
        if (scored) return
        scored = true
        riders.forEach { rider ->
            val n = marks[rider.id]?.size ?: 0
            if (n > 0) session.addScore(rider.id, game.id, n, note = "$n squares spotted")
        }
        if (bingo != null) session.addScore(bingo.id, game.id, BINGO_BONUS, note = "bingo!")
        winner = bingo
    }

    fun toggle(rider: Player, index: Int) {
        val current = marks[rider.id] ?: emptySet()
        val next = if (index in current) current - index else current + index
        marks[rider.id] = next
        if (LINES.any { it.all(next::contains) }) finish(rider)
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text(
            "Tap a square when you spot it — first line wins",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft
        )

        Spacer(Modifier.height(14.dp))

        val champion = winner
        if (champion != null) {
            BingoCard(champion, marks[champion.id] ?: emptySet())
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onFinished,
                colors = ButtonDefaults.buttonColors(containerColor = Ink),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("SEE SCOREBOARD", fontWeight = FontWeight.Bold)
            }
            return@Column
        }

        RiderTabs(riders = riders, selected = selected, onSelect = { selected = it })

        Spacer(Modifier.height(12.dp))

        val rider = riders[selected]
        Card(
            rider = rider,
            spots = cards.getValue(rider.id),
            marked = marks[rider.id] ?: emptySet(),
            onToggle = { toggle(rider, it) }
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { finish(null); onFinished() },
            colors = ButtonDefaults.buttonColors(containerColor = Ink),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("END ROUND", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RiderTabs(riders: List<Player>, selected: Int, onSelect: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        riders.forEachIndexed { i, rider ->
            val on = i == selected
            Text(
                rider.name,
                color = if (on) Color.White else Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (on) Color(rider.colorHex) else Line.copy(alpha = 0.5f))
                    .clickable { onSelect(i) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun Card(rider: Player, spots: List<String>, marked: Set<Int>, onToggle: (Int) -> Unit) {
    val colour = Color(rider.colorHex)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (r in 0 until SIZE) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (c in 0 until SIZE) {
                    val i = r * SIZE + c
                    val on = i in marked
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (on) colour else CardBg)
                            .clickable { onToggle(i) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            spots[i],
                            color = if (on) Color.White else Ink,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BingoCard(player: Player, marked: Set<Int>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Ink)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("BINGO!", color = Gold, fontFamily = BebasNeue, fontSize = 56.sp)
        Text(player.name.uppercase(), color = Color.White, fontFamily = BebasNeue, fontSize = 32.sp)
        Text(
            "${marked.size} squares + $BINGO_BONUS bonus",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 13.sp
        )
    }
}
