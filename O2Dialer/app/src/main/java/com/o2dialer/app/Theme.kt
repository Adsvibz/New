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
    val s = 14.dp
    val m = 18.dp
    val l = 24.dp
    val xl = 30.dp
}

/** Semantic colours: muted jewel tones that sit inside the warm palette. */
object Sem {
    val success = Color(0xFF0D966A)
    val warning = Color(0xFFD48B1E)
    val danger = Color(0xFFD94E54)
    val info = Color(0xFF2457E7)
}

/** Warm brass, used only for highlights and editorial detail. */
val Brass = Color(0xFF5C7DFF)
val Cream = Color(0xFFF7F9FC)
val Sage = Color(0xFF7CA7E8)

/** The dark "focus" surfaces: hero cards, live dialer, daily brief. */
val InkCard = Color(0xFF111D35)
val InkCardDark = Color(0xFF14213B)

@Composable
fun inkSurface(): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) InkCardDark else InkCard

/** Primary brand colour: deep forest green. */
val Brand = Color(0xFF2457E7)

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
    primary = Brand, onPrimary = Color.White,
    primaryContainer = Color(0xFFE7EEFF), onPrimaryContainer = Color(0xFF183EAA),
    secondary = Color(0xFF4667A8), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAF0FC), onSecondaryContainer = Color(0xFF223B69),
    tertiary = Color(0xFF0D966A), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDF7ED), onTertiaryContainer = Color(0xFF07533D),
    background = Color(0xFFF7F9FC), onBackground = Color(0xFF101828),
    surface = Color.White, onSurface = Color(0xFF101828),
    surfaceVariant = Color(0xFFF1F4F9), onSurfaceVariant = Color(0xFF65728A),
    outline = Color(0xFFDCE3ED), outlineVariant = Color(0xFFEDF1F6),
    error = Color(0xFFD94E54), onError = Color.White,
    errorContainer = Color(0xFFFFE8E8), onErrorContainer = Color(0xFF8F262D),
    inverseSurface = Color(0xFF15203A), inverseOnSurface = Color.White,
    surfaceTint = Color.Transparent
)

/** Matching deep-navy alternative to the clean light workspace. */
val NocturneDark = darkColorScheme(
    primary = Color(0xFF92B1FF), onPrimary = Color(0xFF0B2668),
    primaryContainer = Color(0xFF243F7E), onPrimaryContainer = Color(0xFFE5EDFF),
    secondary = Color(0xFFACC2F0), onSecondary = Color(0xFF1C345C),
    secondaryContainer = Color(0xFF283954), onSecondaryContainer = Color(0xFFDCE7FF),
    tertiary = Color(0xFF66D8B0), onTertiary = Color(0xFF073D2C),
    tertiaryContainer = Color(0xFF164C3D), onTertiaryContainer = Color(0xFFD7F8EA),
    background = Color(0xFF0B1220), onBackground = Color(0xFFF6F8FE),
    surface = Color(0xFF141E2F), onSurface = Color(0xFFF6F8FE),
    surfaceVariant = Color(0xFF202C40), onSurfaceVariant = Color(0xFFABB9CD),
    outline = Color(0xFF35455D), outlineVariant = Color(0xFF29374B),
    error = Color(0xFFFF9FA6), onError = Color(0xFF5C1620),
    errorContainer = Color(0xFF6A2832), onErrorContainer = Color(0xFFFFDADD),
    inverseSurface = Color(0xFFE7EDFA), inverseOnSurface = Color(0xFF17213A),
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
        fontFamily = ff,
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
            val top = scheme.primary.copy(alpha = if (dark) 0.025f else 0.012f)
            val bottom = scheme.secondary.copy(alpha = if (dark) 0.025f else 0.012f)
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
