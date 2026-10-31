package com.o2dialer.app

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.core.view.WindowCompat

/** Visible product name. Change it here to rebrand every screen. */
const val APP_NAME = "O2Dialer"
const val APP_VERSION = "7.0"

// ------------------------------------------------------------------ design tokens
/** Spacing scale (4-pt grid). */
object Sp {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

/** Corner radii. Softer than stock Material, still disciplined. */
object Rd {
    val xs = 10.dp
    val s = 12.dp
    val m = 24.dp
    val l = 24.dp
    val xl = 30.dp
}

/** Semantic status colours. */
object Sem {
    val success = Color(0xFF0D966A)
    val warning = Color(0xFFD48B1E)
    val danger = Color(0xFFB42318)
    val info = Color(0xFF2457E7)
}

/** Supporting blue accent used sparingly for progress and emphasis. */
val Brass = Color(0xFF5C7DFF)
val Cream = Color(0xFFF7F9FC)
val Sage = Color(0xFF7CA7E8)

/** The dark "focus" surfaces: hero cards, live dialer, daily brief. */
val InkCard = Color(0xFF101828)
val InkCardDark = Color(0xFF14213B)

@Composable
fun inkSurface(): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) InkCardDark else InkCard

/** Primary accent from the executable AI Studio reference. */
val Brand = com.o2dialer.app.ui.theme.O2Colors.Accent

/** Kept as an alias so older references stay valid. */
val BrandIndigo = Brand

/**
 * Text / icon colour that stays readable (>= 4.5:1) on a 10-20 % tint of [c],
 * in both the light and the dark theme. Use it instead of [c] itself for any text or icon.
 */
@Composable
fun readable(c: Color): Color {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (dark) lerp(c, Color.White, 0.30f) else lerp(c, Color.Black, 0.42f)
}

private val LightC = com.o2dialer.app.ui.theme.O2Colors.Light
val NocturneDark = com.o2dialer.app.ui.theme.O2Colors.Dark

fun statusColor(s: Int): Color = when (s) {
    St.ANSWERED -> Sem.success
    St.NO_ANSWER -> Sem.warning
    St.FAILED -> Sem.danger
    St.CALLING -> Sem.info
    St.DONE -> Color(0xFF327A68)
    else -> Color(0xFF8A948C)
}

@Composable
fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val c = Store.cfg
    val dark = when (c.themeMode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
    val ctx = LocalContext.current
    UiText.init(ctx)
    val scheme = if (c.dynamicColor && Build.VERSION.SDK_INT >= 31) {
        if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    } else if (dark) NocturneDark else LightC
    // Bundled reference fonts work offline and follow the saved language.
    val arabic = c.lang == 1
    val ty = remember(c.lang) { com.o2dialer.app.ui.theme.O2Type.typography(arabic) }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val w = (view.context as? Activity)?.window
            if (w != null) {
                val ic = WindowCompat.getInsetsController(w, view)
                ic.isAppearanceLightStatusBars = !dark
                ic.isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(
        colorScheme = scheme, typography = ty,
        shapes = com.o2dialer.app.ui.theme.O2Shape.Material
    ) {
        com.o2dialer.app.ui.components.O2Canvas(dark) { content() }
    }
}
