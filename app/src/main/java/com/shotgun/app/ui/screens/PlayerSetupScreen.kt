package com.shotgun.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shotgun.app.model.GameDef
import com.shotgun.app.state.SessionViewModel
import com.shotgun.app.ui.theme.Coral
import com.shotgun.app.ui.theme.InkSoft
import com.shotgun.app.ui.theme.Line
import com.shotgun.app.ui.theme.PlayerColors

@Composable
fun PlayerSetupScreen(
    game: GameDef,
    session: SessionViewModel,
    onStart: () -> Unit
) {
    var newName by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        Text("Who's riding?", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        FlowRowSimple {
            session.players.forEach { player ->
                PlayerChip(
                    name = player.name,
                    colorHex = player.colorHex,
                    onRemove = { session.removePlayer(player.id) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("Add player") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                val color = PlayerColors[session.players.size % PlayerColors.size]
                session.addPlayer(newName, color)
                newName = ""
            }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStart,
            enabled = session.players.size >= game.minPlayers,
            colors = ButtonDefaults.buttonColors(containerColor = Coral),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("START GAME", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PlayerChip(name: String, colorHex: Long, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Line.copy(alpha = 0.5f))
            .padding(start = 8.dp, end = 10.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Box(
            Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(androidx.compose.ui.graphics.Color(colorHex))
        )
        Spacer(Modifier.width(6.dp))
        Text(name, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(6.dp))
        Text("\u2715", modifier = Modifier.clickable(onClick = onRemove))
    }
}

/** Minimal wrapping row so chips flow onto multiple lines without a third-party lib. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowSimple(content: @Composable () -> Unit) {
    // FlowRow is still behind ExperimentalLayoutApi on the Compose BOM this
    // project builds against, hence the opt-in above.
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}
