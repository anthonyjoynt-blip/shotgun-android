package com.shotgun.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.shotgun.app.ui.nav.ShotgunNavGraph
import com.shotgun.app.ui.theme.Bg
import com.shotgun.app.ui.theme.ShotgunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShotgunTheme {
                // Surface paints colorScheme.surface (CardBg) over any modifier
                // background, so the page colour has to go in as `color`.
                Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
                    ShotgunNavGraph()
                }
            }
        }
    }
}
