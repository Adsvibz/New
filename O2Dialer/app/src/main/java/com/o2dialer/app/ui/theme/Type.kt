@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
package com.o2dialer.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import com.o2dialer.app.R

object O2Type {
    val Latin = FontFamily(*listOf(400,500,600,700,800).map { w ->
        Font(R.font.plus_jakarta, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w.coerceAtLeast(600))))
    }.toTypedArray())
    val Arabic = FontFamily(Font(R.font.tajawal_regular), Font(R.font.tajawal_medium, FontWeight.Medium), Font(R.font.tajawal_bold, FontWeight.Bold))
    val Mono = FontFamily(Font(R.font.jetbrains_mono))
    /** Editorial display serif for Latin page titles and hero numbers; single regular weight by design. */
    val Serif = FontFamily(Font(R.font.instrument_serif))
    fun typography(arabic: Boolean): Typography {
        val family = if (arabic) Arabic else Latin
        val display = if (arabic) Arabic else Serif
        fun s(size: Int, line: Int, weight: FontWeight = if (arabic) FontWeight.SemiBold else FontWeight.Bold, fam: FontFamily = family) = TextStyle(
            fontFamily = fam, fontSize = size.sp, lineHeight = line.sp,
            fontWeight = weight,
            letterSpacing = 0.sp)
        fun d(size: Int, line: Int) = s(size, line, if (arabic) FontWeight.SemiBold else FontWeight.Normal, display)
        return Typography(
            displayLarge=d(36,44), displayMedium=d(28,36),
            displaySmall=d(26,34), headlineLarge=d(22,30),
            headlineMedium=d(22,30), headlineSmall=d(20,28),
            titleLarge=s(18,26,FontWeight.Bold), titleMedium=s(16,24,FontWeight.Bold),
            titleSmall=s(15,22,FontWeight.Bold), bodyLarge=s(14,22), bodyMedium=if (arabic) s(14,22) else s(13,20),
            bodySmall=s(12,18,if (arabic) FontWeight.Medium else FontWeight.Normal), labelLarge=if (arabic) s(13,20,FontWeight.Bold) else s(12,18,FontWeight.Bold),
            labelMedium=if (arabic) s(12,18,FontWeight.SemiBold) else s(11,16,FontWeight.SemiBold), labelSmall=if (arabic) s(11,17,FontWeight.Medium) else s(10,15,FontWeight.Medium))
    }
}
