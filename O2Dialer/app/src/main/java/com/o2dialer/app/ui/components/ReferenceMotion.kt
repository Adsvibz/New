package com.o2dialer.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import com.o2dialer.app.ui.theme.O2Colors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.o2dialer.app.LocalReducedMotion
import kotlinx.coroutines.delay

/** Values copied from appCardCascade/appScreenEnter in design_reference/src/index.css. */
val ReferenceEase = CubicBezierEasing(.22f, 1f, .36f, 1f)

@Composable
fun ReferenceEntrance(index: Int = 0, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val reduced = LocalReducedMotion.current
    val progress = remember { Animatable(if (reduced) 1f else 0f) }
    val distance = with(LocalDensity.current) { 12f * density }
    LaunchedEffect(reduced) {
        if (reduced) progress.snapTo(1f)
        else {
            delay(20L + index.coerceAtMost(8) * 50L)
            progress.animateTo(1f, tween(420, easing = ReferenceEase))
        }
    }
    Box(modifier.graphicsLayer {
        alpha = progress.value
        translationY = distance * (1f - progress.value)
        scaleX = .985f + .015f * progress.value
        scaleY = scaleX
    }) { content() }
}

@Composable fun ReferenceCallingWave() {
    val reduced = LocalReducedMotion.current
    val transition = rememberInfiniteTransition(label = "referenceCallingWave")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(0, 180, 340, 120, 260).forEach { start ->
            val scale by transition.animateFloat(.35f, 1f,
                infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse, StartOffset(start)), label = "waveBar")
            Box(Modifier.width(3.dp).height(20.dp).graphicsLayer { scaleY = if(reduced) .65f else scale }
                .background(O2Colors.Accent, CircleShape))
        }
    }
}
