package com.shotgun.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shotgun.app.model.GameDef
import com.shotgun.app.ui.theme.InkSoft

/** Shown for any GameType that has no screen yet, so nothing routes to the wrong game. */
@Composable
fun ComingSoonScreen(game: GameDef, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(game.emoji, fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(game.name, style = MaterialTheme.typography.headlineMedium)
        Text("Not built yet", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onBack) {
            Text("BACK TO LIBRARY", fontWeight = FontWeight.Bold)
        }
    }
}
