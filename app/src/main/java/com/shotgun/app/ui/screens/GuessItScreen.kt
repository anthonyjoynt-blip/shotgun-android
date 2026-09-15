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
import com.shotgun.app.ui.theme.Teal

private const val MAX_QUESTIONS = 20
private const val STUMPED_POINTS = 5

private val CATEGORIES = listOf(
    "Animal", "Food", "Place", "Person", "Movie or show",
    "Thing in the car", "Sport", "Anything goes"
)

/**
 * 20 Questions, refereed by the phone. One rider guesses; the rest of the
 * car shares a secret and answers yes/no out loud while whoever holds the
 * phone taps the answer. A guess can be made any time: right ends the round,
 * wrong costs a question. Twenty questions with no correct guess and the car
 * has stumped the guesser.
 *
 * Scoring: guesser gets (20 - questions used), minimum 1, so fast guesses
 * are worth more. A stumped guesser gives every answerer STUMPED_POINTS.
 */
@Composable
fun GuessItScreen(
    game: GameDef,
    session: SessionViewModel,
    onFinished: () -> Unit
) {
    var guesser by remember { mutableStateOf<Player?>(null) }
    var category by remember { mutableStateOf<String?>(null) }

    val picked = guesser
    if (picked == null) {
        RiderPicker(
            title = game.name,
            prompt = "Who's guessing this round?",
            players = session.players,
            onPick = { guesser = it }
        )
        return
    }

    val chosen = category
    if (chosen == null) {
        CategoryPicker(game = game, guesser = picked, onPick = { category = it })
        return
    }

    GuessItRound(
        game = game,
        guesser = picked,
        answerers = session.players.filter { it.id != picked.id },
        category = chosen,
        onScore = { playerId, points, note -> session.addScore(playerId, game.id, points, note) },
        onFinished = onFinished
    )
}

/** The rest of the car agrees on a secret, then tells the guesser only its category. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPicker(game: GameDef, guesser: Player, onPick: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text(
            "${guesser.avatar} ${guesser.name} looks away. The rest of the car quietly agrees on ONE secret thing, then picks what kind of thing it is — that's the only hint ${guesser.name} gets.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft
        )

        Spacer(Modifier.height(16.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CATEGORIES.forEach { name ->
                Text(
                    name,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBg)
                        .clickable { onPick(name) }
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                )
            }
        }
    }
}

private enum class Phase { ASKING, GUESSING, GOT_IT, STUMPED }

@Composable
private fun GuessItRound(
    game: GameDef,
    guesser: Player,
    answerers: List<Player>,
    category: String,
    onScore: (playerId: String, points: Int, note: String) -> Unit,
    onFinished: () -> Unit
) {
    // Each entry is one question: the car's answer, or a wrong guess.
    val trail = remember { mutableStateListOf<Answer>() }
    var phase by remember { mutableStateOf(Phase.ASKING) }
    val used = trail.size

    fun stumped() {
        answerers.forEach { onScore(it.id, STUMPED_POINTS, "stumped ${guesser.name} · $category") }
        phase = Phase.STUMPED
    }

    fun spend(answer: Answer) {
        trail.add(answer)
        if (trail.size >= MAX_QUESTIONS) stumped() else phase = Phase.ASKING
    }

    fun gotIt() {
        val points = (MAX_QUESTIONS - used).coerceAtLeast(1)
        onScore(guesser.id, points, "guessed it in $used · $category")
        phase = Phase.GOT_IT
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text("Category: $category", style = MaterialTheme.typography.bodyMedium, color = InkSoft)

        Spacer(Modifier.height(14.dp))

        when (phase) {
            Phase.GOT_IT -> ResultCard(
                headline = "GOT IT!",
                who = guesser,
                detail = "in $used question${if (used == 1) "" else "s"} · +${(MAX_QUESTIONS - used).coerceAtLeast(1)}"
            )
            Phase.STUMPED -> ResultCard(
                headline = "STUMPED!",
                who = null,
                detail = "the car beat ${guesser.name} · +$STUMPED_POINTS each"
            )
            else -> {
                Banner("${guesser.avatar} ${guesser.name} asks yes/no questions out loud — tap the car's answer")
                Spacer(Modifier.height(14.dp))
                Counter(used)
                Spacer(Modifier.height(10.dp))
                Trail(trail)
            }
        }

        Spacer(Modifier.weight(1f))

        when (phase) {
            Phase.ASKING -> AskingControls(
                guesser = guesser,
                onAnswer = { spend(it) },
                onGuess = { phase = Phase.GUESSING },
                onGiveUp = { stumped() }
            )
            Phase.GUESSING -> GuessingControls(
                guesser = guesser,
                onRight = { gotIt() },
                onWrong = { spend(Answer.WRONG_GUESS) }
            )
            else -> Button(
                onClick = onFinished,
                colors = ButtonDefaults.buttonColors(containerColor = Ink),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("SEE SCOREBOARD", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private enum class Answer(val label: String, val colour: Color) {
    YES("Y", Teal), NO("N", Coral), WRONG_GUESS("✗", Ink)
}

@Composable
private fun Counter(used: Int) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            used.toString().padStart(2, '0'),
            fontFamily = BebasNeue,
            fontSize = 48.sp,
            color = if (used >= MAX_QUESTIONS - 3) Coral else Ink
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "of $MAX_QUESTIONS questions used",
            color = InkSoft,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

/** One chip per question so far: Y, N, or ✗ for a wrong guess. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Trail(trail: List<Answer>) {
    if (trail.isEmpty()) {
        Text("no questions yet", color = InkSoft, fontSize = 12.sp)
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        trail.forEach { a ->
            Box(
                Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(a.colour),
                contentAlignment = Alignment.Center
            ) {
                Text(a.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AskingControls(
    guesser: Player,
    onAnswer: (Answer) -> Unit,
    onGuess: () -> Unit,
    onGiveUp: () -> Unit
) {
    Text("THE CAR'S ANSWER", fontSize = 11.sp, color = InkSoft, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { onAnswer(Answer.YES) },
            colors = ButtonDefaults.buttonColors(containerColor = Teal),
            modifier = Modifier.weight(1f).height(56.dp)
        ) { Text("YES", fontWeight = FontWeight.Bold) }

        Button(
            onClick = { onAnswer(Answer.NO) },
            colors = ButtonDefaults.buttonColors(containerColor = Coral),
            modifier = Modifier.weight(1f).height(56.dp)
        ) { Text("NO", fontWeight = FontWeight.Bold) }
    }

    Spacer(Modifier.height(10.dp))

    OutlinedButton(onClick = onGuess, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        Text("${guesser.name.uppercase()} MAKES A GUESS", fontWeight = FontWeight.Bold)
    }

    TextButton(onClick = onGiveUp, modifier = Modifier.fillMaxWidth()) {
        Text("${guesser.name} gives up", color = InkSoft, fontSize = 12.sp)
    }
}

@Composable
private fun GuessingControls(guesser: Player, onRight: () -> Unit, onWrong: () -> Unit) {
    Text(
        "${guesser.name} says a guess out loud — was it right?",
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onRight,
            colors = ButtonDefaults.buttonColors(containerColor = Teal),
            modifier = Modifier.weight(1f).height(56.dp)
        ) { Text("NAILED IT", fontWeight = FontWeight.Bold) }

        Button(
            onClick = onWrong,
            colors = ButtonDefaults.buttonColors(containerColor = Coral),
            modifier = Modifier.weight(1f).height(56.dp)
        ) { Text("NOPE", fontWeight = FontWeight.Bold) }
    }
    Spacer(Modifier.height(6.dp))
    Text("a wrong guess costs one question", color = InkSoft, fontSize = 12.sp)
}

@Composable
private fun ResultCard(headline: String, who: Player?, detail: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Ink)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(headline, color = Gold, fontFamily = BebasNeue, fontSize = 56.sp)
        if (who != null) {
            Text(who.avatar, fontSize = 40.sp)
            Text(who.name.uppercase(), color = Color.White, fontFamily = BebasNeue, fontSize = 32.sp)
        }
        Text(detail, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
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
