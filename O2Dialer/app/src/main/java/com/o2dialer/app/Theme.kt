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
const val APP_VERSION = "6.0"

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
    val s = 14.dp
    val m = 18.dp
    val l = 24.dp
    val xl = 30.dp
}

/** Semantic colours: muted jewel tones that sit inside the warm palette. */
object Sem {
    val success = Color(0xFF2F8A6A)
    val warning = Color(0xFFB27B32)
    val danger = Color(0xFFBE5540)
    val info = Color(0xFF4C7E8E)
}

/** Warm brass, used only for highlights and editorial detail. */
val Brass = Color(0xFFB9854C)
val Cream = Color(0xFFF5F0E4)
val Sage = Color(0xFF8FAF9D)

/** The dark "focus" surfaces: hero cards, live dialer, daily brief. */
val InkCard = Color(0xFF0C1712)
val InkCardDark = Color(0xFF111C16)

@Composable
fun inkSurface(): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) InkCardDark else InkCard

/** Primary brand colour: deep forest green. */
val Brand = Color(0xFF15503E)

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

private val Outfit = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_bold, FontWeight.Bold)
)

private val PlexArabic = FontFamily(
    Font(R.font.plex_arabic_regular, FontWeight.Normal),
    Font(R.font.plex_arabic_bold, FontWeight.Bold)
)

/** High-contrast editorial serif, used for display numbers and page titles. */
private val InstrumentSerif = FontFamily(
    Font(R.font.instrument_serif, FontWeight.Normal)
)

private val LightC = lightColorScheme(
    primary = Brand, onPrimary = Color(0xFFF7F3E7),
    primaryContainer = Color(0xFFDCEBE0), onPrimaryContainer = Color(0xFF0A2B21),
    secondary = Color(0xFFA9763D), onSecondary = Color(0xFFFFF8ED),
    secondaryContainer = Color(0xFFF4E3CB), onSecondaryContainer = Color(0xFF4A2F12),
    tertiary = Color(0xFF557A6C), onTertiary = Color(0xFFF4FBF6),
    tertiaryContainer = Color(0xFFDCEAE1), onTertiaryContainer = Color(0xFF163027),
    background = Color(0xFFF4F1E9), onBackground = Color(0xFF101713),
    surface = Color(0xFFFBF9F3), onSurface = Color(0xFF101713),
    surfaceVariant = Color(0xFFE9E4D7), onSurfaceVariant = Color(0xFF656A61),
    outline = Color(0xFFD8D2C2), outlineVariant = Color(0xFFE7E2D5),
    error = Color(0xFFB44734), onError = Color(0xFFFFF8F4),
    errorContainer = Color(0xFFF6DDD6), onErrorContainer = Color(0xFF4A1207),
    inverseSurface = Color(0xFF142019), inverseOnSurface = Color(0xFFF1EDE2),
    surfaceTint = Color.Transparent
)

/** The cinematic scheme used for the first-run experience, also the dark theme. */
val NocturneDark = darkColorScheme(
    primary = Color(0xFFA5DDC2), onPrimary = Color(0xFF06271D),
    primaryContainer = Color(0xFF1B3A2E), onPrimaryContainer = Color(0xFFD7F3E3),
    secondary = Color(0xFFE0B27E), onSecondary = Color(0xFF33230F),
    secondaryContainer = Color(0xFF3E2F1B), onSecondaryContainer = Color(0xFFF6DFBF),
    tertiary = Color(0xFF9DBFB0), onTertiary = Color(0xFF102A22),
    tertiaryContainer = Color(0xFF233C32), onTertiaryContainer = Color(0xFFD5EEE2),
    background = Color(0xFF080C0A), onBackground = Color(0xFFF1EDE2),
    surface = Color(0xFF101713), onSurface = Color(0xFFF1EDE2),
    surfaceVariant = Color(0xFF1A231D), onSurfaceVariant = Color(0xFF9BA79E),
    outline = Color(0xFF2A352D), outlineVariant = Color(0xFF1F2A23),
    error = Color(0xFFF1A08C), onError = Color(0xFF3D0B02),
    errorContainer = Color(0xFF5A2117), onErrorContainer = Color(0xFFFFDBD3),
    inverseSurface = Color(0xFFEDE9DE), inverseOnSurface = Color(0xFF141A16),
    surfaceTint = Color.Transparent
)

fun statusColor(s: Int): Color = when (s) {
    St.ANSWERED -> Sem.success
    St.NO_ANSWER -> Sem.warning
    St.FAILED -> Sem.danger
    St.CALLING -> Sem.info
    St.DONE -> Color(0xFF327A68)
    else -> Color(0xFF8A948C)
}

private fun typo(ff: FontFamily, arabic: Boolean): Typography {
    // Arabic script needs more vertical room, no negative tracking and no serif display.
    val k = if (arabic) 1.16f else 1f
    fun tr(v: Float): TextUnit = if (arabic) 0.sp else v.sp
    fun display(w: FontWeight, size: Int, lh: Int, track: Float = 0f) = TextStyle(
        fontFamily = if (arabic) ff else InstrumentSerif,
        fontWeight = if (arabic) FontWeight.Bold else w,
        fontSize = size.sp, lineHeight = (lh * k).sp, letterSpacing = tr(track),
        fontFeatureSettings = "lnum"
    )
    fun text(w: FontWeight, size: Int, lh: Int, track: Float = 0f) = TextStyle(
        fontFamily = ff, fontWeight = w, fontSize = size.sp, lineHeight = (lh * k).sp, letterSpacing = tr(track)
    )
    val b = FontWeight.Bold
    val n = FontWeight.Normal
    return Typography(
        displayLarge = display(n, 58, 60, -1.4f),
        displayMedium = display(n, 46, 48, -1.1f),
        displaySmall = display(n, 36, 40, -0.8f),
        headlineLarge = display(n, 33, 37, -0.6f),
        headlineMedium = display(n, 27, 31, -0.4f),
        headlineSmall = text(b, 22, 28, -0.4f),
        titleLarge = text(b, 19, 25, -0.3f),
        titleMedium = text(b, 16, 22, -0.2f),
        titleSmall = text(b, 14, 20),
        bodyLarge = text(n, 16, 24),
        bodyMedium = text(n, 14, 21),
        bodySmall = text(n, 12, 17),
        labelLarge = text(b, 14, 20),
        labelMedium = text(n, 12, 16, 0.1f),
        labelSmall = text(n, 11, 14, 0.2f)
    )
}

@Composable
fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val c = Store.cfg
    val dark = when (c.themeMode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
    val ctx = LocalContext.current
    val scheme = if (c.dynamicColor && Build.VERSION.SDK_INT >= 31) {
        if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    } else if (dark) NocturneDark else LightC
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
            // A quiet wash: warm paper at the top, a breath of green at the bottom.
            val top = scheme.primary.copy(alpha = if (dark) 0.05f else 0.035f)
            val bottom = scheme.secondary.copy(alpha = if (dark) 0.045f else 0.05f)
            Box(
                Modifier.fillMaxSize().drawBehind {
                    drawRect(
                        Brush.linearGradient(
                            listOf(top, Color.Transparent, bottom),
                            start = Offset(size.width * 0.1f, -size.height * 0.05f),
                            end = Offset(size.width * 0.9f, size.height * 1.05f)
                        )
                    )
                }
            ) { content() }
        }
    }
}
