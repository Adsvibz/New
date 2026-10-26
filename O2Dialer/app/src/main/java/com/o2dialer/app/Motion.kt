package com.o2dialer.app

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.MaterialTheme

/** One motion vocabulary for the whole app. */
object Motion {
    const val ENTER_MS = 560
    val easeOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val easeInOut = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

    fun <T> press() = spring<T>(dampingRatio = 0.62f, stiffness = 620f)
    fun <T> gentle() = spring<T>(dampingRatio = 0.9f, stiffness = 260f)
    fun <T> settle() = spring<T>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
}

/** Tactile press: quick down, soft spring back. */
@Composable
fun Modifier.pressScale(
    src: MutableInteractionSource,
    pressedScale: Float = 0.965f
): Modifier {
    val pressed by src.collectIsPressedAsState()
    val s by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = Motion.press(),
        label = "press"
    )
    return this.graphicsLayer { scaleX = s; scaleY = s }
}

/**
 * Slow-drifting fields of coloured light. Deliberately subtle: it should read as
 * atmosphere, not decoration. Used behind heroes and the first-run screens.
 */
@Composable
fun Aurora(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    ),
    alpha: Float = 0.55f
) {
    val t = rememberInfiniteTransition(label = "aurora")
    val x1 by t.animateFloat(
        0f, 1f, infiniteRepeatable(tween(21_000, easing = LinearEasing), RepeatMode.Reverse), label = "x1"
    )
    val y1 by t.animateFloat(
        0.1f, 0.9f, infiniteRepeatable(tween(17_000, easing = LinearEasing), RepeatMode.Reverse), label = "y1"
    )
    val x2 by t.animateFloat(
        1f, 0.15f, infiniteRepeatable(tween(26_000, easing = LinearEasing), RepeatMode.Reverse), label = "x2"
    )
    val y2 by t.animateFloat(
        0.9f, 0.15f, infiniteRepeatable(tween(19_000, easing = LinearEasing), RepeatMode.Reverse), label = "y2"
    )
    val breathe by t.animateFloat(
        0.85f, 1.15f, infiniteRepeatable(tween(9_000, easing = LinearEasing), RepeatMode.Reverse), label = "breathe"
    )
    Canvas(modifier) {
        val r = size.maxDimension * 0.72f * breathe
        val c1 = Offset(size.width * (0.12f + x1 * 0.30f), size.height * y1)
        val c2 = Offset(size.width * x2, size.height * y2)
        val c3 = Offset(size.width * 0.5f, size.height * (1.1f - y1 * 0.4f))
        drawCircle(
            Brush.radialGradient(
                listOf(colors[0].copy(alpha = alpha), colors[0].copy(alpha = alpha * 0.35f), Color.Transparent),
                center = c1, radius = r
            ),
            radius = r, center = c1
        )
        drawCircle(
            Brush.radialGradient(
                listOf(colors[1].copy(alpha = alpha * 0.65f), Color.Transparent),
                center = c2, radius = r * 0.8f
            ),
            radius = r * 0.8f, center = c2
        )
        drawCircle(
            Brush.radialGradient(
                listOf(colors[2].copy(alpha = alpha * 0.5f), Color.Transparent),
                center = c3, radius = r * 0.9f
            ),
            radius = r * 0.9f, center = c3
        )
    }
}
