package com.shotgun.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shotgun.app.model.GameDef
import com.shotgun.app.model.Player
import com.shotgun.app.state.SessionViewModel
import com.shotgun.app.ui.theme.CardBg
import com.shotgun.app.ui.theme.Coral
import com.shotgun.app.ui.theme.Gold
import com.shotgun.app.ui.theme.Ink
import com.shotgun.app.ui.theme.InkSoft
import com.shotgun.app.ui.theme.Teal

@Composable
fun GuessItScreen(
    game: GameDef,
    session: SessionViewModel,
    onFinished: () -> Unit
) {
    // No guesser until someone is picked; the round can't start without one.
    var guesser by remember { mutableStateOf<Player?>(null) }
    var category by remember { mutableStateOf<String?>(null) }
    var questionsAsked by remember { mutableIntStateOf(0) }

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
        category = chosen,
        questionsAsked = questionsAsked,
        onQuestion = { questionsAsked++ },
        onGuessed = {
            // Fewer questions used = more points, minimum of 1.
            val points = (20 - questionsAsked).coerceAtLeast(1)
            session.addScore(
                playerId = picked.id,
                gameId = game.id,
                points = points,
                note = "guessed correctly in $questionsAsked questions"
            )
            onFinished()
        }
    )
}

private val CATEGORIES = listOf(
    "Animal", "Food", "Place", "Person", "Movie or show",
    "Thing in the car", "Sport", "Anything goes"
)

/** The answerers pick what kind of thing they've thought of; the guesser gets told the category only. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPicker(game: GameDef, guesser: Player, onPick: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text(
            "Everyone but ${guesser.name}: think of something, then pick what kind of thing it is",
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

@Composable
private fun GuessItRound(
    game: GameDef,
    guesser: Player,
    category: String,
    questionsAsked: Int,
    onQuestion: () -> Unit,
    onGuessed: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name.uppercase(), style = MaterialTheme.typography.titleLarge)
        Text("Category: $category", style = MaterialTheme.typography.bodyMedium, color = InkSoft)

        Spacer(Modifier.height(14.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Ink)
                .padding(12.dp)
        ) {
            Text(
                buildString { append(guesser.name); append(" is guessing \u2014 everyone else answers") },
                color = Gold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
            Text(
                questionsAsked.toString().padStart(2, '0'),
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Coral
            )
            Spacer(Modifier.width(6.dp))
            Text("of 20 questions used", color = InkSoft, modifier = Modifier.padding(bottom = 6.dp))
        }

        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onQuestion,
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                modifier = Modifier.weight(1f).height(56.dp)
            ) { Text("YES", fontWeight = FontWeight.Bold) }

            Button(
                onClick = onQuestion,
                colors = ButtonDefaults.buttonColors(containerColor = Coral),
                modifier = Modifier.weight(1f).height(56.dp)
            ) { Text("NO", fontWeight = FontWeight.Bold) }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onGuessed,
            enabled = questionsAsked > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("${guesser.name} wants to guess now")
        }
    }
}
