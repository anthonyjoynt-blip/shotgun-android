package com.shotgun.app.ui.screens

import androidx.compose.foundation.background
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
import com.shotgun.app.ui.theme.Line
import com.shotgun.app.ui.theme.Teal
import kotlinx.coroutines.delay

private const val TURN_MILLIS = 5_000L

private val CATEGORIES = listOf(
    "Car brands", "Pizza toppings", "Countries in Europe", "Dog breeds",
    "Things in a kitchen", "Superheroes", "Fruits", "Sports",
    "Cartoon characters", "Fast food chains", "Board games", "Things at the beach",
    "Ice cream flavours", "Animals with four legs", "Movie villains", "Breakfast foods",
    "Musical instruments", "Things that are yellow", "Canadian cities", "Video games",
    "Candy bars", "Things in a car", "Ocean animals", "Holidays"
)

/**
 * The GameType.ELIMINATION screen. Riders take turns naming something in the
 * category; five seconds per turn, miss it and you're out, last rider standing
 * wins. Placement is scored as it happens: first out gets nothing, each later
 * elimination earns one more point than the one before, the winner gets
 * (riders - 1). All ScoreEvents, so the scoreboard needs nothing new.
 */
@Composable
fun CategoriesScreen(
    game: GameDef,
    session: SessionViewModel,
    onFinished: () -> Unit
) {
    var category by remember { mutableStateOf(CATEGORIES.random()) }
    var started by remember { mutableStateOf(false) }

    if (!started) {
        CategoryPicker(
            game = game,
            category = category,
            onShuffle = { category = CATEGORIES.filter { it != category }.random() },
            onStart = { started = true }
        )
    } else {
        CategoriesRound(game = game, session = session, category = category, onFinished = onFinished)
    }
}

@Composable
private fun CategoryPicker(game: GameDef, category: String, onShuffle: () -> Unit, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text(
            "Name one in 5 seconds or you're out. Last rider standing wins.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft
        )

        Spacer(Modifier.height(24.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardBg)
                .padding(20.dp)
        ) {
            Text("CATEGORY", fontSize = 11.sp, color = InkSoft, fontWeight = FontWeight.SemiBold)
            Text(category, style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(onClick = onShuffle, modifier = Modifier.fillMaxWidth()) {
            Text("DIFFERENT ONE", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = Coral),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("START", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoriesRound(
    game: GameDef,
    session: SessionViewModel,
    category: String,
    onFinished: () -> Unit
) {
    // Round state lives here, not in the ViewModel: a half-played round isn't
    // worth persisting, and only the resulting ScoreEvents matter afterwards.
    val alive = remember { mutableStateListOf<Player>().apply { addAll(session.players) } }
    val out = remember { mutableStateListOf<Player>() }
    var turn by remember { mutableIntStateOf(0) }
    var timerKey by remember { mutableIntStateOf(0) }
    var millisLeft by remember { mutableLongStateOf(TURN_MILLIS) }
    var winner by remember { mutableStateOf<Player?>(null) }

    fun nextTurn() {
        turn = (turn + 1) % alive.size
        timerKey++
    }

    fun eliminateCurrent() {
        val loser = alive.removeAt(turn)
        out.add(loser)
        val placing = out.size - 1   // 0 for the first rider out
        if (placing > 0) session.addScore(loser.id, game.id, placing, note = "out #${out.size} · $category")
        if (alive.size == 1) {
            val last = alive[0]
            session.addScore(last.id, game.id, out.size, note = "last one standing · $category")
            winner = last
        } else {
            // Removing at `turn` means the next rider now sits at the same index.
            turn %= alive.size
            timerKey++
        }
    }

    // One countdown per turn; the key restarts it. Wall-clock based so it
    // doesn't drift, and it eliminates on its own when it runs out.
    LaunchedEffect(timerKey, winner) {
        if (winner != null) return@LaunchedEffect
        val end = System.currentTimeMillis() + TURN_MILLIS
        while (true) {
            val left = end - System.currentTimeMillis()
            millisLeft = left.coerceAtLeast(0)
            if (left <= 0) break
            delay(50)
        }
        eliminateCurrent()
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text(category, style = MaterialTheme.typography.bodyMedium, color = InkSoft)

        Spacer(Modifier.height(16.dp))

        val champion = winner
        if (champion != null) {
            WinnerCard(champion)
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

        TurnCard(player = alive[turn], millisLeft = millisLeft)

        Spacer(Modifier.height(14.dp))

        // Who's still in, in turn order, with the eliminated dimmed at the end.
        Text("STILL IN", fontSize = 11.sp, color = InkSoft, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        RiderStrip(alive = alive, out = out)

        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { nextTurn() },
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                modifier = Modifier.weight(1f).height(56.dp)
            ) { Text("GOT ONE", fontWeight = FontWeight.Bold) }

            Button(
                onClick = { eliminateCurrent() },
                colors = ButtonDefaults.buttonColors(containerColor = Coral),
                modifier = Modifier.weight(1f).height(56.dp)
            ) { Text("OUT", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun TurnCard(player: Player, millisLeft: Long) {
    val fraction = millisLeft.toFloat() / TURN_MILLIS
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(player.colorHex))
            .padding(18.dp)
    ) {
        Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("name one — go!", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "%.1f".format(millisLeft / 1000f),
            color = Color.White,
            fontFamily = BebasNeue,
            fontSize = 72.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { fraction },
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f),
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun RiderStrip(alive: List<Player>, out: List<Player>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        alive.forEach { RiderChip(it, isOut = false) }
        out.forEach { RiderChip(it, isOut = true) }
    }
}

@Composable
private fun RiderChip(player: Player, isOut: Boolean) {
    Text(
        player.name,
        color = if (isOut) InkSoft.copy(alpha = 0.5f) else Color.White,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isOut) Line.copy(alpha = 0.5f) else Color(player.colorHex))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun WinnerCard(player: Player) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Ink)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏆", fontSize = 40.sp)
        Text(player.name.uppercase(), color = Gold, fontFamily = BebasNeue, fontSize = 40.sp)
        Text("last one standing", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
    }
}
