@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
package com.o2dialer.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.o2dialer.app.R

object O2Type {
    val Latin = FontFamily(*listOf(400,500,600,700,800).map { w ->
        Font(R.font.plus_jakarta, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w.coerceAtLeast(600))))
    }.toTypedArray())
    val Arabic = FontFamily(
        Font(R.font.plex_arabic_regular, FontWeight.Normal),
        Font(R.font.plex_arabic_medium, FontWeight.Medium),
        Font(R.font.plex_arabic_semibold, FontWeight.SemiBold),
        Font(R.font.plex_arabic_bold, FontWeight.Bold)
    )
    val Mono = FontFamily(Font(R.font.jetbrains_mono))
    fun typography(arabic: Boolean): Typography {
        val family = if (arabic) Arabic else Latin
        fun s(size: Int, line: Int, weight: FontWeight = if (arabic) FontWeight.SemiBold else FontWeight.Bold, track: TextUnit = 0.sp) = TextStyle(
            fontFamily = family, fontSize = size.sp, lineHeight = line.sp,
            fontWeight = weight,
            letterSpacing = track)
        // Reference headings sit closer to 720-760 with about -0.028em tracking; tracking is Latin-only.
        fun h(size: Int, line: Int) = s(size, line, if (arabic) FontWeight.SemiBold else FontWeight.Bold, if (arabic) 0.sp else (-size * 0.028f).sp)
        fun t(size: Int, line: Int) = s(size, line, FontWeight.Bold, if (arabic) 0.sp else (-size * 0.02f).sp)
        return Typography(
            displayLarge=h(36,44), displayMedium=h(28,36),
            displaySmall=h(26,34), headlineLarge=h(22,30),
            headlineMedium=h(22,30), headlineSmall=h(20,28),
            titleLarge=t(18,26), titleMedium=t(16,24),
            titleSmall=t(15,22), bodyLarge=s(14,22), bodyMedium=if (arabic) s(14,22) else s(13,20),
            bodySmall=s(12,18,if (arabic) FontWeight.Medium else FontWeight.Normal), labelLarge=if (arabic) s(13,20,FontWeight.Bold) else s(12,18,FontWeight.Bold),
            labelMedium=if (arabic) s(12,18,FontWeight.SemiBold) else s(11,16,FontWeight.SemiBold), labelSmall=if (arabic) s(11,17,FontWeight.Medium) else s(10,15,FontWeight.Medium))
    }
}
