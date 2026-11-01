package com.o2dialer.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*

object O2Colors {
    val Accent: Color get() = ReferenceAppearance.selected.color
    val Ink = Color(0xFF111318)
    val Muted = Color(0xFF8A909E)
    val LightCanvas = listOf(Color(0xFFF7F6F2), Color(0xFFF1EFEA), Color(0xFFEAE7E1))
    val DarkCanvas = listOf(Color(0xFF131519), Color(0xFF171A20), Color(0xFF111317))
    val Light get() = lightColorScheme(
        primary = Ink, onPrimary = Color.White,
        primaryContainer = Ink.copy(alpha = .06f), onPrimaryContainer = Ink,
        secondary = Accent, onSecondary = Color.White,
        secondaryContainer = Accent.copy(alpha = .11f), onSecondaryContainer = Color(0xFFAA3C1D),
        tertiary = Color(0xFF047857), onTertiary = Color.White,
        tertiaryContainer = Color(0xFFDCFCE7), onTertiaryContainer = Color(0xFF065F46),
        background = LightCanvas.first(), onBackground = Ink,
        surface = Color.White.copy(alpha = .78f), onSurface = Ink,
        surfaceVariant = Color(0xFFF2F4F8).copy(alpha = .8f), onSurfaceVariant = Color(0xFF6E7582),
        outline = Color(0xFFE5E7EB), outlineVariant = Ink.copy(alpha = .06f),
        error = Color(0xFFB42318), onError = Color.White,
        errorContainer = Color(0xFFFEE2E2), onErrorContainer = Color(0xFF991B1B), surfaceTint = Color.Transparent
    )
    val Dark get() = darkColorScheme(
        primary = Color(0xFFF3F4F6), onPrimary = Ink,
        primaryContainer = Color.White.copy(alpha = .08f), onPrimaryContainer = Color(0xFFF3F4F6),
        secondary = Accent, onSecondary = Color.White,
        secondaryContainer = Accent.copy(alpha = .15f), onSecondaryContainer = Color(0xFFFFA58C),
        tertiary = Color(0xFF6EE7B7), onTertiary = Color(0xFF064E3B),
        background = DarkCanvas.first(), onBackground = Color(0xFFF3F4F6),
        surface = Color(0xFF1C1F26).copy(alpha = .72f), onSurface = Color(0xFFF3F4F6),
        surfaceVariant = Color(0xFF16191F).copy(alpha = .85f), onSurfaceVariant = Color(0xFF9CA3AF),
        outline = Color.White.copy(alpha = .14f), outlineVariant = Color.White.copy(alpha = .08f),
        error = Color(0xFFFCA5A5), onError = Color(0xFF7F1D1D),
        errorContainer = Color(0xFF451A1A), onErrorContainer = Color(0xFFFECACA), surfaceTint = Color.Transparent
    )
}
