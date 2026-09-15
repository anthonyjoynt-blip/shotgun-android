package com.shotgun.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shotgun.app.R

// Both bundled under the SIL Open Font License — see FONT_LICENSES.txt at the project root.

/** Display face: condensed, caps-only, single weight — so no Bold requests on it. */
val BebasNeue = FontFamily(
    Font(R.font.bebas_neue_regular, FontWeight.Normal)
)

/** Body face. Three weights cover every fontWeight the screens ask for. */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

// Bebas sits about a third narrower than Inter at the same size, so the
// display sizes run larger than the system-font placeholders they replace.
val ShotgunTypography = Typography(
    headlineLarge = TextStyle(fontFamily = BebasNeue, fontSize = 40.sp, letterSpacing = 1.sp),
    headlineMedium = TextStyle(fontFamily = BebasNeue, fontSize = 32.sp, letterSpacing = 0.8.sp),
    titleLarge = TextStyle(fontFamily = BebasNeue, fontSize = 26.sp, letterSpacing = 0.8.sp),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
)
