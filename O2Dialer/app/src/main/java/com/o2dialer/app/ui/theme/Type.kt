@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
package com.o2dialer.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import com.o2dialer.app.R

object O2Type {
    val Latin = FontFamily(*listOf(400,500,600,700,800).map { w ->
        Font(R.font.inter_variable, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))
    }.toTypedArray())
    val Arabic = FontFamily(Font(R.font.tajawal_regular), Font(R.font.tajawal_medium, FontWeight.Medium), Font(R.font.tajawal_bold, FontWeight.Bold))
    val Mono = FontFamily(Font(R.font.jetbrains_mono))
    fun typography(arabic: Boolean): Typography {
        val family = if (arabic) Arabic else Latin
        fun s(size: Int, line: Int, weight: FontWeight = FontWeight.SemiBold) = TextStyle(
            fontFamily = family, fontSize = size.sp, lineHeight = line.sp,
            fontWeight = if (!arabic && weight == FontWeight.ExtraBold) FontWeight.Bold else weight,
            letterSpacing = if (arabic || size < 20) 0.sp else (-size * .011f).sp)
        return Typography(
            displayLarge=s(36,44,FontWeight.ExtraBold), displayMedium=s(28,36,FontWeight.ExtraBold),
            displaySmall=s(26,34,FontWeight.ExtraBold), headlineLarge=s(22,30,FontWeight.ExtraBold),
            headlineMedium=s(22,30,FontWeight.ExtraBold), headlineSmall=s(20,28,FontWeight.Bold),
            titleLarge=s(18,26,FontWeight.Bold), titleMedium=s(16,24,FontWeight.Bold),
            titleSmall=s(15,22,FontWeight.Bold), bodyLarge=s(14,22), bodyMedium=s(14,22),
            bodySmall=s(12,18,FontWeight.Medium), labelLarge=s(13,20,FontWeight.Bold),
            labelMedium=s(12,18,FontWeight.SemiBold), labelSmall=s(11,17,FontWeight.Medium))
    }
}
