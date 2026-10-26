package com.o2dialer.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A quiet trend line: one smooth stroke, a wash underneath, drawn left to right.
 */
@Composable
fun Sparkline(
    values: List<Int>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 58.dp
) {
    val draw = remember { Animatable(0f) }
    LaunchedEffect(values) { draw.animateTo(1f, tween(1100, easing = Motion.easeOut)) }
    Canvas(modifier.fillMaxWidth().height(height)) {
        if (values.isEmpty()) return@Canvas
        val top = 8.dp.toPx()
        val bottom = size.height - 2.dp.toPx()
        val max = values.max().coerceAtLeast(1)
        val step = size.width / (values.size - 1).coerceAtLeast(1)
        val pts = values.mapIndexed { i, v ->
            Offset(i * step, bottom - (v.toFloat() / max) * (bottom - top))
        }
        if (pts.size == 1) {
            drawCircle(color, 3.dp.toPx(), pts.first())
            return@Canvas
        }
        val line = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            for (i in 1 until pts.size) {
                val p = pts[i - 1]
                val c = pts[i]
                val mid = (p.x + c.x) / 2f
                cubicTo(mid, p.y, mid, c.y, c.x, c.y)
            }
        }
        val area = Path().apply {
            addPath(line)
            lineTo(pts.last().x, size.height)
            lineTo(pts.first().x, size.height)
            close()
        }
        clipRect(right = size.width * draw.value) {
            drawPath(
                area,
                Brush.verticalGradient(listOf(color.copy(alpha = 0.22f), Color.Transparent)),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            drawPath(line, color, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(color, 3.2.dp.toPx(), pts.last())
            drawCircle(Color.White.copy(alpha = 0.85f), 1.4.dp.toPx(), pts.last())
        }
    }
}

/**
 * Stage breakdown as a single ring of arcs. Sweeps in once, then responds to filters.
 */
@Composable
fun StageDonut(
    segments: List<Pair<Int, Int>>,
    total: Int,
    modifier: Modifier = Modifier,
    size: Dp = 152.dp,
    stroke: Dp = 14.dp,
    selected: Int? = null
) {
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(segments) { sweep.animateTo(1f, tween(950, easing = Motion.easeOut)) }
    val track = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    Box(modifier.size(size), Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = stroke.toPx()
            drawArc(
                color = track, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                style = Stroke(sw, cap = StrokeCap.Round)
            )
            val live = segments.filter { it.second > 0 }
            if (live.isNotEmpty()) {
                val gap = if (live.size > 1) 3f else 0f
                var start = -90f
                live.forEach { (stage, count) ->
                    val portion = count.toFloat() / (total.coerceAtLeast(1))
                    val s = (360f * portion * sweep.value - gap).coerceAtLeast(0f)
                    if (s > 0f) {
                        drawArc(
                            color = Stg.color(stage), startAngle = start + gap / 2f, sweepAngle = s,
                            useCenter = false,
                            style = Stroke(
                                width = if (selected == stage) sw + 4.dp.toPx() else sw,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                    start += 360f * portion
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CountUp(total, style = MaterialTheme.typography.displaySmall)
            Text(
                T("tagged", "مصنّفة"), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
