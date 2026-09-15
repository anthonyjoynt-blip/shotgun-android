package com.shotgun.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shotgun.app.data.Avatars
import com.shotgun.app.model.GameDef
import com.shotgun.app.model.Player
import com.shotgun.app.state.SessionViewModel
import com.shotgun.app.ui.theme.Coral
import com.shotgun.app.ui.theme.Ink
import com.shotgun.app.ui.theme.InkSoft
import com.shotgun.app.ui.theme.Line
import com.shotgun.app.ui.theme.PlayerColors

@Composable
fun PlayerSetupScreen(
    game: GameDef,
    session: SessionViewModel,
    onStart: () -> Unit
) {
    // Suggest the first avatar nobody on the roster has yet — it's near the
    // start of the row, so the highlight is visible, and riders don't double up.
    fun freshAvatar() = Avatars.all.firstOrNull { a -> session.roster.none { it.avatar == a } } ?: Avatars.all.random()

    var newName by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf(freshAvatar()) }

    fun addRider() {
        if (newName.isBlank()) return
        session.addPlayer(newName, PlayerColors[session.roster.size % PlayerColors.size], avatar)
        newName = ""
        avatar = freshAvatar()
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(game.name, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        Text("Who's riding?", style = MaterialTheme.typography.headlineMedium)

        if (session.roster.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text("tap to ride · hold to forget", fontSize = 11.sp, color = InkSoft)
            Spacer(Modifier.height(10.dp))
            FlowRowSimple {
                session.roster.forEach { rider ->
                    val riding = session.isRiding(rider.id)
                    RosterChip(
                        rider = rider,
                        riding = riding,
                        onToggle = { session.setRiding(rider, !riding) },
                        onForget = { session.removeFromRoster(rider.id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("NEW RIDER", fontSize = 11.sp, color = InkSoft, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        AvatarPicker(selected = avatar, onPick = { avatar = it })
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("Name") },
            leadingIcon = { Text(avatar, fontSize = 22.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { addRider() }),
            trailingIcon = {
                TextButton(onClick = { addRider() }, enabled = newName.isNotBlank()) {
                    Text("ADD", fontWeight = FontWeight.Bold)
                }
            },
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

/** A roster member: filled in their colour when riding this trip, greyed when sitting out. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RosterChip(rider: Player, riding: Boolean, onToggle: () -> Unit, onForget: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (riding) Color(rider.colorHex) else Line.copy(alpha = 0.5f))
            .combinedClickable(onClick = onToggle, onLongClick = onForget)
            .padding(start = 10.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rider.avatar, fontSize = 18.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            rider.name,
            color = if (riding) Color.White else InkSoft,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AvatarPicker(selected: String, onPick: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        Avatars.all.forEach { emoji ->
            val on = emoji == selected
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (on) Coral.copy(alpha = 0.18f) else Line.copy(alpha = 0.4f))
                    .then(if (on) Modifier.border(2.dp, Coral, CircleShape) else Modifier)
                    .clickable { onPick(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
        }
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
