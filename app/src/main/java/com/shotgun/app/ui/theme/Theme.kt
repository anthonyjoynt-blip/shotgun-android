package com.shotgun.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ShotgunColors = lightColorScheme(
    primary = Coral,
    secondary = Teal,
    tertiary = Gold,
    background = Bg,
    surface = CardBg,
    onBackground = Ink,
    onSurface = Ink
)

@Composable
fun ShotgunTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Light palette only for now — the road-signage look is designed
    // for daytime, in-car glare, not a dark mode.
    MaterialTheme(
        colorScheme = ShotgunColors,
        typography = ShotgunTypography,
        content = content
    )
}
