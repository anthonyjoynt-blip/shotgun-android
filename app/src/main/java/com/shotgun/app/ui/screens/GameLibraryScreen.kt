package com.shotgun.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shotgun.app.data.GameCatalog
import com.shotgun.app.model.GameDef
import com.shotgun.app.ui.theme.CardBg
import com.shotgun.app.ui.theme.InkSoft
import com.shotgun.app.ui.theme.Line

@Composable
fun GameLibraryScreen(onGameSelected: (GameDef) -> Unit) {
    Column(Modifier.fillMaxSize().padding(top = 24.dp)) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text("SHOTGUN", style = MaterialTheme.typography.headlineLarge)
            Text("Games for the ride", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = Line)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(GameCatalog.games) { game ->
                GameCard(game = game, onClick = { onGameSelected(game) })
            }
        }
    }
}

@Composable
private fun GameCard(game: GameDef, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Line),
            contentAlignment = Alignment.Center
        ) {
            Text(game.emoji, fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(game.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(game.description, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        }
        Text(
            "${game.minPlayers}\u2013${game.maxPlayers}",
            fontSize = 11.sp,
            color = InkSoft
        )
    }
}
