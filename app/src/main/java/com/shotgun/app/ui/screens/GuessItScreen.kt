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
 * Two ways to play 20 Questions, chosen per round:
 *  - ONE_GUESSER: one rider guesses; the rest of the car shares the secret
 *    and answers. Cracking it scores the guesser (20 - questions used);
 *    a stump scores every answerer STUMPED_POINTS.
 *  - CAR_GUESSES: one rider keeps the secret and answers; everyone else
 *    guesses together. Cracking it scores every guesser (20 - questions
 *    used); a stump scores the keeper STUMPED_POINTS per guesser.
 * Either way, the phone referees: whoever holds it taps the answers, a
 * guess can be made any time, and a wrong guess costs a question.
 */
private enum class Mode { ONE_GUESSER, CAR_GUESSES }

@Composable
fun GuessItScreen(
    game: GameDef,
    session: SessionViewModel,
    onFinished: () -> Unit
) {
    var mode by remember { mutableStateOf<Mode?>(null) }
    var star by remember { mutableStateOf<Player?>(null) }   // the guesser, or the keeper
    var category by remember { mutableStateOf<String?>(null) }

    val chosenMode = mode
    if (chosenMode == null) {
        ModePicker(game = game, onPick = { mode = it })
        return
    }

    val picked = star
    if (picked == null) {
        RiderPicker(
            title = game.name,
            prompt = when (chosenMode) {
                Mode.ONE_GUESSER -> "Who's guessing this round?"
                Mode.CAR_GUESSES -> "Who's keeping the secret this round?"
            },
            players = session.players,
            onPick = { star = it }
        )
        return
    }

    val chosen = category
    if (chosen == null) {
        CategoryPicker(game = game, mode = chosenMode, star = picked, onPick = { category = it })
        return
    }

    GuessItRound(
        game = game,
        mode = chosenMode,
        star = picked,
        others = session.players.filter { it.id != picked.id },
        category = chosen,
        onScore = { playerId, points, note -> session.addScore(playerId, game.id, points, note) },
        onFinished = onFinished
    )
}

@Composable
private fun ModePicker(game: GameDef, onPick: (Mode) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text("How are we playing this round?", style = MaterialTheme.typography.bodyMedium, color = InkSoft)

        Spacer(Modifier.height(16.dp))

        ModeCard(
            title = "ONE GUESSER",
            blurb = "One rider guesses. The rest of the car shares a secret and answers.",
            colour = Coral,
            onClick = { onPick(Mode.ONE_GUESSER) }
        )
        Spacer(Modifier.height(12.dp))
        ModeCard(
            title = "THE CAR GUESSES",
            blurb = "One rider keeps a secret and answers. Everyone else guesses together.",
            colour = Teal,
            onClick = { onPick(Mode.CAR_GUESSES) }
        )
    }
}

@Composable
private fun ModeCard(title: String, blurb: String, colour: Color, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colour)
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Text(title, color = Color.White, fontFamily = BebasNeue, fontSize = 30.sp)
        Text(blurb, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
    }
}

/** Whoever holds the secret picks the category — it's the only hint the guessing side gets. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPicker(game: GameDef, mode: Mode, star: Player, onPick: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text(
            when (mode) {
                Mode.ONE_GUESSER ->
                    "${star.avatar} ${star.name} looks away. The rest of the car quietly agrees on ONE secret thing, then picks what kind of thing it is — that's the only hint ${star.name} gets."
                Mode.CAR_GUESSES ->
                    "${star.avatar} ${star.name} thinks of ONE secret thing (keep it to yourself!), then picks what kind of thing it is — that's the only hint the car gets."
            },
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
    mode: Mode,
    star: Player,
    others: List<Player>,
    category: String,
    onScore: (playerId: String, points: Int, note: String) -> Unit,
    onFinished: () -> Unit
) {
    // Each entry is one question: the answer given, or a wrong guess.
    val trail = remember { mutableStateListOf<Answer>() }
    var phase by remember { mutableStateOf(Phase.ASKING) }
    val used = trail.size

    // Who asks and who answers, by mode. "the car" is the collective side.
    val guesserLabel = if (mode == Mode.ONE_GUESSER) star.name else "the car"
    val answererLabel = if (mode == Mode.ONE_GUESSER) "the car" else star.name

    fun stumped() {
        when (mode) {
            Mode.ONE_GUESSER -> others.forEach { onScore(it.id, STUMPED_POINTS, "stumped ${star.name} · $category") }
            Mode.CAR_GUESSES -> onScore(star.id, STUMPED_POINTS * others.size, "stumped the car · $category")
        }
        phase = Phase.STUMPED
    }

    fun spend(answer: Answer) {
        trail.add(answer)
        if (trail.size >= MAX_QUESTIONS) stumped() else phase = Phase.ASKING
    }

    fun gotIt() {
        val points = (MAX_QUESTIONS - used).coerceAtLeast(1)
        when (mode) {
            Mode.ONE_GUESSER -> onScore(star.id, points, "guessed it in $used · $category")
            Mode.CAR_GUESSES -> others.forEach { onScore(it.id, points, "cracked ${star.name}'s secret in $used · $category") }
        }
        phase = Phase.GOT_IT
    }

    val winPoints = (MAX_QUESTIONS - used).coerceAtLeast(1)
    val plural = if (used == 1) "" else "s"

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text("Category: $category", style = MaterialTheme.typography.bodyMedium, color = InkSoft)

        Spacer(Modifier.height(14.dp))

        when (phase) {
            Phase.GOT_IT -> when (mode) {
                Mode.ONE_GUESSER -> ResultCard("GOT IT!", star, "in $used question$plural · +$winPoints")
                Mode.CAR_GUESSES -> ResultCard("CRACKED IT!", null, "the car got ${star.name}'s secret in $used question$plural · +$winPoints each")
            }
            Phase.STUMPED -> when (mode) {
                Mode.ONE_GUESSER -> ResultCard("STUMPED!", null, "the car beat ${star.name} · +$STUMPED_POINTS each")
                Mode.CAR_GUESSES -> ResultCard("STUMPED!", star, "beat the whole car · +${STUMPED_POINTS * others.size}")
            }
            else -> {
                Banner(
                    when (mode) {
                        Mode.ONE_GUESSER -> "${star.avatar} ${star.name} asks yes/no questions out loud — tap the car's answer"
                        Mode.CAR_GUESSES -> "The car asks ${star.avatar} ${star.name} yes/no questions — tap ${star.name}'s answer"
                    }
                )
                Spacer(Modifier.height(14.dp))
                Counter(used)
                Spacer(Modifier.height(10.dp))
                Trail(trail)
            }
        }

        Spacer(Modifier.weight(1f))

        when (phase) {
            Phase.ASKING -> AskingControls(
                guesserLabel = guesserLabel,
                answererLabel = answererLabel,
                onAnswer = { spend(it) },
                onGuess = { phase = Phase.GUESSING },
                onGiveUp = { stumped() }
            )
            Phase.GUESSING -> GuessingControls(
                guesserLabel = guesserLabel,
                answererLabel = answererLabel,
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
    guesserLabel: String,
    answererLabel: String,
    onAnswer: (Answer) -> Unit,
    onGuess: () -> Unit,
    onGiveUp: () -> Unit
) {
    Text("${answererLabel.uppercase()}'S ANSWER", fontSize = 11.sp, color = InkSoft, fontWeight = FontWeight.SemiBold)
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
        Text("${guesserLabel.uppercase()} MAKES A GUESS", fontWeight = FontWeight.Bold)
    }

    TextButton(onClick = onGiveUp, modifier = Modifier.fillMaxWidth()) {
        Text("$guesserLabel gives up", color = InkSoft, fontSize = 12.sp)
    }
}

@Composable
private fun GuessingControls(
    guesserLabel: String,
    answererLabel: String,
    onRight: () -> Unit,
    onWrong: () -> Unit
) {
    Text(
        "$guesserLabel says a guess out loud — $answererLabel, was it right?",
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
