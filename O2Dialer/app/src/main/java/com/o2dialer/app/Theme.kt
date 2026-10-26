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
const val APP_VERSION = "5.0"

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

/** Corner radii. */
object Rd {
    val xs = 8.dp
    val s = 12.dp
    val m = 16.dp
    val l = 20.dp
    val xl = 28.dp
}

/** Semantic colours: always use these for meaning, never ad-hoc colours. */
object Sem {
    val success = Color(0xFF0F7A55)
    val warning = Color(0xFFB45309)
    val danger = Color(0xFFC03B32)
    val info = Color(0xFF2563EB)
}

/** The single dark "focus" surface used by the live hero and the today card. */
val InkCard = Color(0xFF161619)
val InkCardDark = Color(0xFF1E1E22)

@Composable
fun inkSurface(): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) InkCardDark else InkCard

/** Brand accent. Kept as a single solid colour, not a gradient. */
val BrandIndigo = Color(0xFF4F46E5)

/**
 * Text / icon colour that stays readable (>= 4.5:1) on a 10-20 % tint of [c],
 * in both the light and the dark theme. Use it instead of [c] itself for any text or icon.
 */
@Composable
fun readable(c: Color): Color {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (dark) lerp(c, Color.White, 0.35f) else lerp(c, Color.Black, 0.38f)
}

private val Outfit = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_bold, FontWeight.Bold)
)

private val PlexArabic = FontFamily(
    Font(R.font.plex_arabic_regular, FontWeight.Normal),
    Font(R.font.plex_arabic_bold, FontWeight.Bold)
)

private val LightC = lightColorScheme(
    primary = BrandIndigo, onPrimary = Color.White,
    primaryContainer = Color(0xFFECEAFF), onPrimaryContainer = Color(0xFF1A1147),
    secondary = Color(0xFF0F766E), onSecondary = Color.White,
    background = Color(0xFFF6F6F3), onBackground = Color(0xFF121214),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF121214),
    surfaceVariant = Color(0xFFF1F1ED), onSurfaceVariant = Color(0xFF6D6D74),
    outline = Color(0xFFE3E3DD), outlineVariant = Color(0xFFEEEEE9),
    error = Sem.danger, onError = Color.White,
    inverseSurface = Color(0xFF1B1B1E), inverseOnSurface = Color(0xFFF4F4F2),
    surfaceTint = Color.Transparent
)

private val DarkC = darkColorScheme(
    primary = Color(0xFFB9B0FF), onPrimary = Color(0xFF1B1245),
    primaryContainer = Color(0xFF332A78), onPrimaryContainer = Color(0xFFE7E4FF),
    secondary = Color(0xFF7DD3C8), onSecondary = Color(0xFF00382F),
    background = Color(0xFF0A0A0B), onBackground = Color(0xFFF3F3F5),
    surface = Color(0xFF131315), onSurface = Color(0xFFF3F3F5),
    surfaceVariant = Color(0xFF1D1D21), onSurfaceVariant = Color(0xFF9C9CA6),
    outline = Color(0xFF2A2A2F), outlineVariant = Color(0xFF232327),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    inverseSurface = Color(0xFFEDEDF0), inverseOnSurface = Color(0xFF1B1B1E),
    surfaceTint = Color.Transparent
)

fun statusColor(s: Int): Color = when (s) {
    St.ANSWERED -> Sem.success
    St.NO_ANSWER -> Sem.warning
    St.FAILED -> Sem.danger
    St.CALLING -> Sem.info
    St.DONE -> Color(0xFF0D9488)
    else -> Color(0xFF8A8A94)
}

private fun typo(ff: FontFamily, arabic: Boolean): Typography {
    // Arabic script needs more vertical room and no negative tracking.
    val k = if (arabic) 1.14f else 1f
    fun tr(v: Float): TextUnit = if (arabic) 0.sp else v.sp
    fun st(w: FontWeight, size: Int, lh: Int, track: Float = 0f) =
        TextStyle(fontFamily = ff, fontWeight = w, fontSize = size.sp, lineHeight = (lh * k).sp, letterSpacing = tr(track))
    val b = FontWeight.Bold
    val n = FontWeight.Normal
    return Typography(
        displayLarge = st(b, 40, 46, -1.2f),
        displayMedium = st(b, 34, 40, -1f),
        displaySmall = st(b, 30, 36, -0.8f),
        headlineLarge = st(b, 28, 34, -0.8f),
        headlineMedium = st(b, 24, 30, -0.6f),
        headlineSmall = st(b, 21, 28, -0.4f),
        titleLarge = st(b, 19, 25, -0.3f),
        titleMedium = st(b, 16, 22, -0.2f),
        titleSmall = st(b, 14, 20),
        bodyLarge = st(n, 16, 24),
        bodyMedium = st(n, 14, 21),
        bodySmall = st(n, 12, 17),
        labelLarge = st(b, 14, 20),
        labelMedium = st(n, 12, 16, 0.1f),
        labelSmall = st(n, 11, 14, 0.2f)
    )
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val c = Store.cfg
    val dark = when (c.themeMode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
    val ctx = LocalContext.current
    val scheme = if (c.dynamicColor && Build.VERSION.SDK_INT >= 31) {
        if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    } else if (dark) DarkC else LightC
    // IBM Plex Sans Arabic for Arabic mode, Outfit for English: one consistent voice per language.
    val arabic = c.lang == 1
    val ty = remember(c.lang) { typo(if (arabic) PlexArabic else Outfit, arabic) }
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
        shapes = Shapes(
            extraSmall = RoundedCornerShape(Rd.xs), small = RoundedCornerShape(Rd.s),
            medium = RoundedCornerShape(Rd.m), large = RoundedCornerShape(Rd.l),
            extraLarge = RoundedCornerShape(Rd.xl)
        )
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            // A single, very soft tonal wash at the top. No decorative glows.
            val wash = scheme.primary.copy(alpha = if (dark) 0.07f else 0.045f)
            Box(
                Modifier.fillMaxSize().drawBehind {
                    drawRect(
                        Brush.verticalGradient(
                            listOf(wash, Color.Transparent),
                            startY = -24.dp.toPx(), endY = size.height * 0.26f
                        )
                    )
                }
            ) { content() }
        }
    }
}
