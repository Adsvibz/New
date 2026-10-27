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
    val xs = 6.dp
    val s = 9.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 20.dp
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

/** Primary brand colour: professional blue. */
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

private val LatinFont = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_bold, FontWeight.Bold)
)
private val ArabicFont = FontFamily(
    Font(R.font.plex_arabic_regular, FontWeight.Normal),
    Font(R.font.plex_arabic_medium, FontWeight.Medium),
    Font(R.font.plex_arabic_bold, FontWeight.Bold)
)

/** Precision Contrast: white working surfaces, cool slate canvas, decisive cobalt. */
private val LightC = lightColorScheme(
    primary = Color(0xFF2457E7), onPrimary = Color.White,
    primaryContainer = Color(0xFFEBF0FF), onPrimaryContainer = Color(0xFF163E91),
    secondary = Color(0xFF334860), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8EDF3), onSecondaryContainer = Color(0xFF26384D),
    tertiary = Color(0xFF087E5A), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDFF5EB), onTertiaryContainer = Color(0xFF06583F),
    background = Color(0xFFF7F9FC), onBackground = Color(0xFF101828),
    surface = Color.White, onSurface = Color(0xFF101828),
    surfaceVariant = Color(0xFFF2F4F7), onSurfaceVariant = Color(0xFF475467),
    outline = Color(0xFF98A2B3), outlineVariant = Color(0xFFE4E7EC),
    error = Color(0xFFB32632), onError = Color.White,
    errorContainer = Color(0xFFFDE8EA), onErrorContainer = Color(0xFF802028),
    inverseSurface = Color(0xFF112740), inverseOnSurface = Color.White,
    surfaceTint = Color.Transparent
)

/** Purpose-built dark hierarchy; not an automatic inversion of the light palette. */
val NocturneDark = darkColorScheme(
    primary = Color(0xFF8FB0FF), onPrimary = Color(0xFF0D2D70),
    primaryContainer = Color(0xFF263F74), onPrimaryContainer = Color(0xFFEBF0FF),
    secondary = Color(0xFFB7C9DC), onSecondary = Color(0xFF233B51),
    secondaryContainer = Color(0xFF293C51), onSecondaryContainer = Color(0xFFDEEBF7),
    tertiary = Color(0xFF75DAB0), onTertiary = Color(0xFF063B2A),
    tertiaryContainer = Color(0xFF174938), onTertiaryContainer = Color(0xFFDBF7E9),
    background = Color(0xFF101318), onBackground = Color(0xFFF2F6FC),
    surface = Color(0xFF1A1E25), onSurface = Color(0xFFF2F6FC),
    surfaceVariant = Color(0xFF252B35), onSurfaceVariant = Color(0xFFB5BECC),
    outline = Color(0xFF667085), outlineVariant = Color(0xFF343B48),
    error = Color(0xFFFFA0A8), onError = Color(0xFF631820),
    errorContainer = Color(0xFF662A34), onErrorContainer = Color(0xFFFFE1E6),
    inverseSurface = Color(0xFFEAF0F8), inverseOnSurface = Color(0xFF101828),
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
        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.None),
        fontFamily = ff,
        fontWeight = w,
        fontSize = size.sp, lineHeight = (lh * k).sp, letterSpacing = tr(track),
        fontFeatureSettings = "lnum"
    )
    fun text(w: FontWeight, size: Int, lh: Int, track: Float = 0f) = TextStyle(
        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.None),
        fontFamily = ff, fontWeight = w, fontSize = size.sp, lineHeight = (lh * k).sp, letterSpacing = tr(track)
    )
    val b = FontWeight.Bold
    val n = FontWeight.Normal
    return Typography(
        displayLarge = display(b, 58, 60, -1.4f),
        displayMedium = display(b, 46, 48, -1.1f),
        displaySmall = display(b, 32, 37, -0.6f),
        headlineLarge = display(b, 28, 36, -0.4f),
        headlineMedium = display(b, 24, 30, -0.3f),
        headlineSmall = text(FontWeight.Bold, 20, 26),
        titleLarge = text(FontWeight.Bold, 22, 28),
        titleMedium = text(b, 16, 24),
        titleSmall = text(b, 14, 20),
        bodyLarge = text(n, 16, 24),
        bodyMedium = text(n, 14, 21),
        bodySmall = text(n, 12, 17),
        labelLarge = text(b, 14, 20),
        labelMedium = text(FontWeight.Medium, 12, 18, 0.1f),
        labelSmall = text(FontWeight.Medium, 12, 16, 0.1f)
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
    // Balanced font metrics and real medium/bold weights keep text aligned with controls.
    val arabic = c.lang == 1
    val ty = remember(c.lang) { typo(if (arabic) ArabicFont else LatinFont, arabic) }
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
            content()
        }
    }
}
