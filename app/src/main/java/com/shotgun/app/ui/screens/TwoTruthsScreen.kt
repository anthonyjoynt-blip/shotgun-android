package com.shotgun.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.shotgun.app.ui.theme.CardBg
import com.shotgun.app.ui.theme.Coral
import com.shotgun.app.ui.theme.Gold
import com.shotgun.app.ui.theme.Ink
import com.shotgun.app.ui.theme.InkSoft
import com.shotgun.app.ui.theme.Line
import com.shotgun.app.ui.theme.Teal

private val STATEMENTS = listOf(1, 2, 3)

/**
 * The Two Truths, One Lie screen (GameType.POINTS). One rider tells three
 * things, everyone else picks which is the lie, the teller reveals. Spotting
 * the lie is +1; the teller gets +1 per rider fooled, so a good lie is worth
 * more than a lucky guess. The statements themselves are spoken, never typed.
 */
@Composable
fun TwoTruthsScreen(
    game: GameDef,
    session: SessionViewModel,
    onFinished: () -> Unit
) {
    var teller by remember { mutableStateOf<Player?>(null) }

    val picked = teller
    if (picked == null) {
        RiderPicker(
            title = game.name,
            prompt = "Who's telling this round?",
            players = session.players,
            onPick = { teller = it }
        )
        return
    }

    TwoTruthsRound(
        game = game,
        teller = picked,
        guessers = session.players.filter { it.id != picked.id },
        onScore = { playerId, points, note -> session.addScore(playerId, game.id, points, note) },
        onFinished = onFinished
    )
}

@Composable
private fun TwoTruthsRound(
    game: GameDef,
    teller: Player,
    guessers: List<Player>,
    onScore: (playerId: String, points: Int, note: String) -> Unit,
    onFinished: () -> Unit
) {
    // rider id -> which statement they think is the lie (1..3)
    val guesses = remember { mutableStateMapOf<String, Int>() }
    var locked by remember { mutableStateOf(false) }
    var lie by remember { mutableStateOf<Int?>(null) }

    fun reveal(actualLie: Int) {
        // Score once, here, rather than in composition — recomposition must not re-award.
        var fooled = 0
        guessers.forEach { rider ->
            if (guesses[rider.id] == actualLie) {
                onScore(rider.id, 1, "spotted ${teller.name}'s lie")
            } else {
                fooled++
            }
        }
        if (fooled > 0) onScore(teller.id, fooled, "fooled $fooled of ${guessers.size}")
        lie = actualLie
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text(
            "${teller.name} says three things — two true, one lie",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft
        )

        Spacer(Modifier.height(14.dp))

        val revealed = lie
        when {
            revealed != null -> ResultPhase(teller, guessers, guesses, revealed, onFinished)
            locked -> RevealPhase(teller, onReveal = { reveal(it) })
            else -> GuessPhase(
                teller = teller,
                guessers = guessers,
                guesses = guesses,
                onGuess = { rider, n -> guesses[rider.id] = n },
                onLock = { locked = true }
            )
        }
    }
}

@Composable
private fun ColumnScope.GuessPhase(
    teller: Player,
    guessers: List<Player>,
    guesses: Map<String, Int>,
    onGuess: (Player, Int) -> Unit,
    onLock: () -> Unit
) {
    Banner("${teller.name} is telling — everyone else picks the lie")

    Spacer(Modifier.height(14.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
        items(guessers, key = { it.id }) { rider ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(rider.avatar, fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Text(rider.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                STATEMENTS.forEach { n ->
                    val chosen = guesses[rider.id] == n
                    Box(
                        Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (chosen) Color(rider.colorHex) else Line.copy(alpha = 0.6f))
                            .clickable { onGuess(rider, n) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            n.toString(),
                            color = if (chosen) Color.White else Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    val everyoneIn = guessers.all { guesses.containsKey(it.id) }
    Button(
        onClick = onLock,
        enabled = everyoneIn,
        colors = ButtonDefaults.buttonColors(containerColor = Ink),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(if (everyoneIn) "LOCK IN GUESSES" else "WAITING FOR EVERYONE", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ColumnScope.RevealPhase(teller: Player, onReveal: (Int) -> Unit) {
    Banner("${teller.name} — which one was the lie?")

    Spacer(Modifier.height(14.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        STATEMENTS.forEach { n ->
            Box(
                Modifier
                    .weight(1f)
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Coral)
                    .clickable { onReveal(n) },
                contentAlignment = Alignment.Center
            ) {
                Text("#$n", color = Color.White, fontFamily = BebasNeue, fontSize = 40.sp)
            }
        }
    }

    Spacer(Modifier.weight(1f))
}

@Composable
private fun ColumnScope.ResultPhase(
    teller: Player,
    guessers: List<Player>,
    guesses: Map<String, Int>,
    lie: Int,
    onFinished: () -> Unit
) {
    val fooled = guessers.count { guesses[it.id] != lie }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Ink)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("THE LIE WAS", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text("#$lie", color = Gold, fontFamily = BebasNeue, fontSize = 56.sp)
        Text(
            when (fooled) {
                0 -> "nobody fell for it"
                guessers.size -> "${teller.name} fooled everyone · +$fooled"
                else -> "${teller.name} fooled $fooled · +$fooled"
            },
            color = Color.White,
            fontSize = 13.sp
        )
    }

    Spacer(Modifier.height(12.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
        items(guessers, key = { it.id }) { rider ->
            val right = guesses[rider.id] == lie
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(rider.avatar, fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Text(rider.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("guessed #${guesses[rider.id]}", color = InkSoft, fontSize = 13.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (right) "✓ +1" else "✗",
                    color = if (right) Teal else Coral,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    Button(
        onClick = onFinished,
        colors = ButtonDefaults.buttonColors(containerColor = Ink),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text("SEE SCOREBOARD", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Banner(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Ink)
            .padding(12.dp)
    ) {
        Text(text, color = Gold, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
