@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ====================================================================== layout helpers
/** Caps the content width so tablets / landscape stay tidy. Phones are unaffected. */
@Composable
fun Responsive(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.TopCenter) {
        Box(Modifier.widthIn(max = 620.dp).fillMaxSize()) { content() }
    }
}

@Composable
fun isCompact(): Boolean = LocalConfiguration.current.screenWidthDp < 360

// ====================================================================== motion + atoms
@Composable
fun PulseDot(color: Color, size: Dp = 10.dp) {
    if (LocalReducedMotion.current || Store.campActive()) { Dot(color, size); return }
    val t = rememberInfiniteTransition(label = "pulse")
    val a = t.animateFloat(1f, 0.3f, infiniteRepeatable(tween(1100)), label = "pulseAlpha")
    val s = t.animateFloat(0.9f, 1.5f, infiniteRepeatable(tween(1100)), label = "pulseScale")
    Box(Modifier.size(size).graphicsLayer { alpha = a.value; scaleX = s.value; scaleY = s.value }.clip(CircleShape).background(color))
}

/** Small solid status dot. Cleaner than a coloured pill in dense rows. */
@Composable
fun Dot(color: Color, size: Dp = 7.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(color))
}

/** Loading placeholder with a soft travelling highlight. */
@Composable
fun Skeleton(modifier: Modifier = Modifier, radius: Dp = 12.dp) {
    if (LocalReducedMotion.current || Store.campActive()) {
        Box(modifier.clip(RoundedCornerShape(radius)).background(MaterialTheme.colorScheme.surfaceVariant)); return
    }
    val t = rememberInfiniteTransition(label = "skeleton")
    val a = t.animateFloat(0.4f, 0.85f, infiniteRepeatable(tween(1000)), label = "skeletonAlpha")
    Box(
        modifier.graphicsLayer { alpha = a.value }.clip(RoundedCornerShape(radius))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

// ====================================================================== progress
@Composable
fun BProgress(
    progress: Float, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary,
    track: Color = MaterialTheme.colorScheme.outline, height: Dp = 8.dp
) {
    val p by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        animationSpec = Motion.settle(), label = "bar"
    )

    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(track.copy(alpha = 0.55f))) {
        Box(
            Modifier.fillMaxWidth(p).fillMaxHeight().clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun ProgressRing(
    progress: Float, size: Dp, stroke: Dp, color: Color, track: Color, content: @Composable () -> Unit
) {
    val p by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = Motion.easeOut), label = "ring"
    )
    Box(Modifier.size(size), Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = stroke.toPx()
            drawArc(
                color = track.copy(alpha = 0.6f), startAngle = -90f, sweepAngle = 360f,
                useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(sw, cap = StrokeCap.Round)
            )
            if (p > 0f) drawArc(
                color = color,
                startAngle = -90f, sweepAngle = 360f * p, useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(sw, cap = StrokeCap.Round)
            )
        }
        content()
    }
}

// ====================================================================== buttons
enum class BtnKind { Primary, Tonal, Outline, Ghost, Danger }

@Composable
fun BButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, kind: BtnKind = BtnKind.Primary,
    icon: ImageVector? = null, enabled: Boolean = true, compact: Boolean = false, iconOnly: Boolean = false
) {
    val src = remember { MutableInteractionSource() }
    val cs = MaterialTheme.colorScheme
    val height = if (compact) 48.dp else 54.dp
    val shape = RoundedCornerShape(12.dp)
    val brush = when (kind) {
        BtnKind.Primary -> Brush.linearGradient(listOf(cs.primary, cs.primary))
        BtnKind.Danger -> Brush.linearGradient(listOf(cs.error, cs.error))
        BtnKind.Tonal -> Brush.linearGradient(
            listOf(cs.primaryContainer, cs.primaryContainer)
        )
        else -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }
    val fg = when (kind) {
        BtnKind.Primary -> cs.onPrimary
        BtnKind.Danger -> cs.onError
        BtnKind.Tonal -> cs.onPrimaryContainer
        BtnKind.Outline, BtnKind.Ghost -> cs.onSurface
    }
    val border = when (kind) {
        BtnKind.Outline -> Modifier.border(1.dp, cs.outline, shape)
        BtnKind.Tonal -> Modifier.border(1.dp, cs.primary.copy(alpha = 0.18f), shape)
        else -> Modifier
    }
    val elevation = Modifier
    Box(
        modifier
            .heightIn(min = height)
            .then(if (enabled) elevation else Modifier)
            .pressScale(src)
            .clip(shape)
            .background(brush)
            .then(border)
            .clickable(interactionSource = src, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .graphicsLayer { alpha = if (enabled) 1f else 0.45f }
            .padding(horizontal = if (iconOnly) 0.dp else 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (iconOnly && icon != null) {
            Icon(icon, text, tint = fg, modifier = Modifier.size(21.dp))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(19.dp))
                Text(
                    text, color = fg, style = MaterialTheme.typography.labelLarge,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** A soft, round icon button used in headers and inline actions. */
@Composable
fun SoftIconButton(
    icon: ImageVector, desc: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    container: Color = MaterialTheme.colorScheme.surface
) {
    val cs = MaterialTheme.colorScheme
    val src = remember { MutableInteractionSource() }
    val shape = CircleShape
    Box(
        modifier.size(48.dp).pressScale(src)
            .clip(shape).background(container)
            .border(1.dp, cs.outline.copy(alpha = 0.7f), shape)
            .clickable(interactionSource = src, indication = null, role = Role.Button, onClick = onClick),
        Alignment.Center
    ) {
        Icon(icon, desc, tint = tint, modifier = Modifier.size(19.dp))
    }
}

/** A large circular control with a caption underneath, used by the live call bar. */
@Composable
fun CircleAction(
    icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surface,
    content: Color = MaterialTheme.colorScheme.onSurface,
    outlined: Boolean = true, enabled: Boolean = true
) {
    val src = remember { MutableInteractionSource() }
    val cs = MaterialTheme.colorScheme
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier.size(62.dp).pressScale(src, 0.94f)
                .semantics { contentDescription = label }
                .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
                .shadow(if (outlined) 6.dp else 14.dp, CircleShape, ambientColor = container.copy(alpha = 0.4f))
                .clip(CircleShape)
                .then(
                    if (outlined) Modifier.background(container)
                    else Modifier.background(container)
                )
                .then(if (outlined) Modifier.border(1.dp, cs.outline, CircleShape) else Modifier)
                .clickable(interactionSource = src, indication = null, role = Role.Button, enabled = enabled, onClick = onClick),
            Alignment.Center
        ) { Icon(icon, null, tint = content, modifier = Modifier.size(25.dp)) }
        Text(
            label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = cs.onSurfaceVariant, maxLines = 2, textAlign = TextAlign.Center
        )
    }
}

// ====================================================================== inputs
@Composable
fun BTextField(
    value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, label: String? = null,
    placeholder: String? = null, supporting: String? = null, keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true, minLines: Int = 1, leading: ImageVector? = null, isError: Boolean = false
) {
    val cs = MaterialTheme.colorScheme
    val src = remember { MutableInteractionSource() }
    val focused by src.collectIsFocusedAsState()
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    val line by animateColorAsState(
        if (isError) cs.error else if (focused) cs.primary else cs.outline,
        tween(if (LocalReducedMotion.current) 0 else Motion.ENTER_MS), label = "fieldBorder"
    )
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) Text(label, style = MaterialTheme.typography.labelLarge,
            color = if (isError) cs.error else cs.onSurface)
        TextField(
            value = value, onValueChange = onChange,
            modifier = Modifier.fillMaxWidth()
                .border(if (focused || isError) 1.5.dp else 1.dp, line, RoundedCornerShape(12.dp))
                .then(if (label != null) Modifier.semantics { contentDescription = label } else Modifier),
            singleLine = singleLine, minLines = minLines,
            maxLines = if (singleLine) 1 else maxOf(5, minLines),
            isError = isError, shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                textDirection = if (keyboardType == KeyboardType.Phone || keyboardType == KeyboardType.Number)
                    TextDirection.Ltr else TextDirection.Content),
            placeholder = if (placeholder != null) { {
                Text(placeholder, color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            } } else null,
            leadingIcon = if (leading != null) { {
                Icon(leading, null, modifier = Modifier.size(20.dp), tint = cs.onSurfaceVariant)
            } } else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType,
                imeAction = if (singleLine) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Default),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focus.clearFocus() }),
            interactionSource = src,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cs.surface,
                unfocusedContainerColor = cs.surfaceVariant.copy(alpha = 0.35f),
                errorContainerColor = cs.surface,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent, errorIndicatorColor = Color.Transparent,
                focusedTextColor = cs.onSurface, unfocusedTextColor = cs.onSurface,
                errorTextColor = cs.onSurface, cursorColor = cs.primary
            )
        )
        if (supporting != null) Text(supporting, style = MaterialTheme.typography.bodySmall,
            color = if (isError) cs.error else cs.onSurfaceVariant)
    }
}

/** Segmented control with a sliding indicator and a soft spring. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Rd.s)
    BoxWithConstraints(
        Modifier.fillMaxWidth().height(50.dp).clip(shape)
            .background(cs.surfaceVariant.copy(alpha = 0.55f))
            .border(1.dp, cs.outline.copy(alpha = 0.6f), shape).padding(4.dp)
    ) {
        val n = options.size.coerceAtLeast(1)
        val slot = maxWidth / n
        val pos by animateFloatAsState(selected.toFloat(), Motion.gentle(), label = "segment")
        val x = slot * pos // offset is already layout-direction aware
        Box(
            Modifier.offset(x = x).width(slot).fillMaxHeight()
                .shadow(5.dp, RoundedCornerShape(11.dp), ambientColor = Color.Black.copy(alpha = 0.10f))
                .clip(RoundedCornerShape(11.dp)).background(cs.surface)
                .border(1.dp, cs.onSurface.copy(alpha = 0.04f), RoundedCornerShape(11.dp))
        )
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { i, o ->
                val on = i == selected
                val fg by animateColorAsState(if (on) cs.onSurface else cs.onSurfaceVariant, tween(200), label = "segFg")
                Box(
                    Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(11.dp))
                        .selectable(selected = on, role = Role.Tab) { onSelect(i) },
                    Alignment.Center
                ) {
                    Text(
                        o, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (options.size > 3) 12.sp else 13.5.sp
                        )
                    )
                }
            }
        }
    }
}

// ====================================================================== surfaces
/**
 * Shared neutral surface with a quiet separator and consistent corner radius.
 */
@Composable
fun Panel(
    modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, accent: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Rd.m)
    val interaction = remember { MutableInteractionSource() }
    val base = modifier
        .then(if (onClick != null) Modifier.pressScale(interaction, 0.99f) else Modifier)
        .clip(shape)
        .background(cs.surface)
        .border(1.dp, accent?.copy(alpha = 0.5f) ?: cs.outlineVariant, shape)
        .then(if (onClick != null) Modifier.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick) else Modifier)
    Column(base.fillMaxWidth(), content = content)
}

/** One-pixel separator, optionally indented to line up with text after a leading avatar. */
@Composable
fun Hairline(modifier: Modifier = Modifier, startIndent: Dp = 0.dp) {
    Box(
        modifier.fillMaxWidth().padding(start = startIndent).height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface)
}

@Composable
fun IconTile(icon: ImageVector, color: Color = MaterialTheme.colorScheme.primary, size: Dp = 42.dp) {
    val shape = RoundedCornerShape(size * 0.32f)
    Box(
        Modifier.size(size).clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, color.copy(alpha = 0.16f), shape), Alignment.Center
    ) {
        Icon(icon, null, tint = readable(color), modifier = Modifier.size(size * 0.46f))
    }
}

// ====================================================================== feedback: banner + toast + empty
@Composable
fun Banner(
    kind: Int, title: String, message: String? = null, modifier: Modifier = Modifier,
    actionText: String? = null, onAction: (() -> Unit)? = null
) {
    val c = when (kind) { Tk.SUCCESS -> Sem.success; Tk.ERROR -> Sem.danger; Tk.WARN -> Sem.warning; else -> Sem.info }
    val fg = readable(c)
    val icon = when (kind) {
        Tk.SUCCESS -> Icons.Rounded.CheckCircle
        Tk.ERROR -> Icons.Rounded.Error
        Tk.WARN -> Icons.Rounded.Warning
        else -> Icons.Rounded.Info
    }
    val shape = RoundedCornerShape(Rd.s)
    Row(
        modifier.fillMaxWidth().animateContentSize()
            .clip(shape)
            .background(c.copy(alpha = 0.07f))
            .border(1.dp, c.copy(alpha = 0.20f), shape)
            .padding(start = Sp.m, end = Sp.s, top = Sp.m, bottom = Sp.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(c.copy(alpha = 0.14f)), Alignment.Center) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(Sp.m))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (message != null) Text(
                message, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (actionText != null && onAction != null) TextButton(onClick = onAction) {
            Text(actionText, color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun ToastHost(modifier: Modifier = Modifier) {
    val t = Store.toast
    val accessibility = LocalAccessibilityManager.current
    var last by remember { mutableStateOf<UiToast?>(null) }
    LaunchedEffect(t?.id) {
        if (t != null) {
            last = t
            delay(accessibility?.calculateRecommendedTimeoutMillis(4000L, containsIcons = true, containsText = true, containsControls = false) ?: 4000L)
            if (Store.toast?.id == t.id) Store.toast = null
        }
    }
    AnimatedVisibility(
        visible = t != null, modifier = modifier,
        enter = slideInVertically(Motion.gentle()) { -it } + fadeIn(tween(180)),
        exit = slideOutVertically(tween(180)) { -it / 2 } + fadeOut(tween(150))
    ) {
        val shown = t ?: last
        if (shown != null) {
            val c = when (shown.kind) { Tk.SUCCESS -> Sem.success; Tk.ERROR -> Sem.danger; Tk.WARN -> Sem.warning; else -> Sem.info }
            val icon = when (shown.kind) {
                Tk.SUCCESS -> Icons.Rounded.CheckCircle
                Tk.ERROR -> Icons.Rounded.Error
                Tk.WARN -> Icons.Rounded.Warning
                else -> Icons.Rounded.Info
            }
            Row(
                Modifier.semantics { liveRegion = LiveRegionMode.Polite }.shadow(18.dp, RoundedCornerShape(50), ambientColor = Color.Black.copy(alpha = 0.25f))
                    .clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.inverseSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(50))
                    .padding(horizontal = Sp.l, vertical = Sp.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = c, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Sp.s))
                Text(
                    shown.msg, color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodyMedium, maxLines = 2
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector, title: String, sub: String, modifier: Modifier = Modifier,
    actionText: String? = null, onAction: (() -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxSize().padding(Sp.xxxl), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(136.dp), Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val c = this.size.minDimension / 2f
                drawCircle(cs.primary.copy(alpha = 0.05f), radius = c * 0.98f)
                drawCircle(cs.primary.copy(alpha = 0.08f), radius = c * 0.76f)
                drawCircle(cs.secondary.copy(alpha = 0.10f), radius = c * 0.54f)
            }
            Box(Modifier.size(62.dp).clip(CircleShape).background(cs.primary.copy(alpha = 0.10f)), Alignment.Center) {
                Icon(icon, null, tint = cs.primary, modifier = Modifier.size(27.dp))
            }
        }
        Spacer(Modifier.height(Sp.xl))
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Sp.s))
        Text(
            sub, color = cs.onSurfaceVariant, textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.widthIn(max = 300.dp)
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(Sp.xl))
            BButton(actionText, onAction, compact = true, icon = Icons.Rounded.ArrowForward)
        }
    }
}

// ====================================================================== small atoms
@Composable
fun NumText(
    n: String, modifier: Modifier = Modifier, style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified
) {
    Text(
        n, modifier = modifier,
        style = style.copy(textDirection = TextDirection.Ltr, fontFeatureSettings = "tnum"),
        color = color
    )
}

/** Number that counts up from zero, then eases to every new value. */
@Composable
fun CountUp(
    value: Int, modifier: Modifier = Modifier, style: TextStyle = MaterialTheme.typography.displaySmall,
    color: Color = Color.Unspecified
) {
    val a = remember { Animatable(value.toFloat()) }
    LaunchedEffect(value) { a.animateTo(value.toFloat(), tween(950, easing = Motion.easeOut)) }
    Text(
        a.value.roundToInt().toString(), modifier = modifier,
        style = style.copy(fontFeatureSettings = "tnum"), color = color
    )
}

@Composable
fun Avatar(text: String, color: Color = MaterialTheme.colorScheme.primary, size: Dp = 46.dp) {
    val ch = text.trim().firstOrNull { it.isLetter() }?.uppercase() ?: "#"
    Box(
        Modifier.size(size).clip(CircleShape)
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.24f), CircleShape),
        Alignment.Center
    ) {
        Text(ch, color = readable(color), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun Pill(text: String, color: Color, icon: ImageVector? = null, onClick: (() -> Unit)? = null) {
    val fg = readable(color)
    val shape = RoundedCornerShape(50)
    var m = Modifier.clip(shape).background(color.copy(alpha = 0.11f)).border(1.dp, color.copy(alpha = 0.16f), shape)
    if (onClick != null) m = m.clickable(role = Role.Button, onClick = onClick)
    Row(
        m.heightIn(min = if (onClick != null) 48.dp else 24.dp).padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text, color = fg,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
            maxLines = 1
        )
    }
}

fun stageIcon(s: Int): ImageVector = when (s) {
    Stg.POTENTIAL -> Icons.Rounded.Star
    Stg.NOT_INTERESTED -> Icons.Rounded.ThumbDown
    Stg.CALL_BACK -> Icons.Rounded.Replay
    Stg.WHATSAPP -> Icons.Rounded.Chat
    Stg.VOICEMAIL -> Icons.Rounded.Voicemail
    else -> Icons.Rounded.Label
}

@Composable
fun StatusPill(s: Int) = Pill(St.label(s), statusColor(s))

@Composable
fun StagePill(s: Int, onClick: (() -> Unit)? = null) = Pill(Stg.label(s), Stg.color(s), stageIcon(s), onClick)

/** Dot + label, for dense rows where a tinted pill would add noise. */
@Composable
fun StatusDot(s: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Dot(statusColor(s))
        Spacer(Modifier.width(6.dp))
        Text(St.label(s), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ActionChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val src = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(50)
    Row(
        Modifier.clip(shape)
            .background(cs.surface)
            .border(1.dp, cs.outline.copy(alpha = 0.8f), shape)
            .pressScale(src, 0.94f)
            .clickable(interactionSource = src, indication = null, role = Role.Button) { onClick() }
            .heightIn(min = 48.dp).padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = cs.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
fun WhatsAppButton(number: String, size: Dp = 40.dp) {
    val ctx = LocalContext.current
    val green = Color(0xFF1F8A70)
    Box(
        Modifier.size(maxOf(size, 48.dp)).clip(CircleShape).background(green.copy(alpha = 0.14f))
            .border(1.dp, green.copy(alpha = 0.25f), CircleShape)
            .clickable { openWhatsApp(ctx, number) },
        Alignment.Center
    ) {
        Icon(Icons.Rounded.Chat, contentDescription = "WhatsApp", tint = readable(green), modifier = Modifier.size(size * 0.46f))
    }
}

/** A handset and forward-flow arrow, shared with the adaptive launcher icon. */
@Composable
fun LogoMark(dp: Dp, color: Color = Cream) {
    Icon(
        painter = androidx.compose.ui.res.painterResource(R.drawable.ic_brand_mark),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(dp)
    )
}

// ====================================================================== top bar
@Composable
private fun LivePill() {
    val pct = Store.lists.find { it.id == Store.camp.listId }?.let { Store.prog(it).pct } ?: 0
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(Sem.success.copy(alpha = 0.10f))
            .border(1.dp, Sem.success.copy(alpha = 0.18f), RoundedCornerShape(50))
            .clickable { Store.tab = 1 }.heightIn(min = 32.dp).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PulseDot(Sem.success, 7.dp)
        Spacer(Modifier.width(7.dp))
        Text(
            "${T("Live", "مباشر")} $pct%", color = readable(Sem.success),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun BlendBar(
    title: String, onBack: (() -> Unit)? = null, showSettings: Boolean = false,
    subtitle: String? = null, actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        Modifier.fillMaxWidth().statusBarsPadding()
            .padding(start = Sp.xl, end = Sp.l, top = Sp.m, bottom = Sp.m)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                SoftIconButton(Icons.AutoMirrored.Filled.ArrowBack, T("Back", "رجوع"), onBack)
                Spacer(Modifier.width(Sp.m))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title, style = MaterialTheme.typography.headlineLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        subtitle, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(Sp.s))
            if (Store.campActive() && Store.tab != 1) {
                LivePill()
                Spacer(Modifier.width(Sp.s))
            }
            actions()
            if (showSettings) {
                SoftIconButton(Icons.Rounded.Settings, T("Settings", "الإعدادات"), { Store.openSettings() })
            }
        }
    }
}

// ====================================================================== dialog + sheet
@Composable
fun BDialog(
    title: String?, onDismiss: () -> Unit, confirmText: String? = null, onConfirm: (() -> Unit)? = null,
    dismissText: String = T("Cancel", "إلغاء"), destructive: Boolean = false, confirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = if (title != null) { { Text(title, style = MaterialTheme.typography.titleLarge) } } else null,
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Sp.l)) { content() }
        },
        confirmButton = {
            if (confirmText != null && onConfirm != null) BButton(
                confirmText, onConfirm, kind = if (destructive) BtnKind.Danger else BtnKind.Primary,
                compact = true, enabled = confirmEnabled)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText, style = MaterialTheme.typography.labelLarge) }
        }
    )
}

/**
 * Modern replacement for centred forms: a full-width bottom sheet with a big title,
 * comfortable fields and one clear primary action.
 */
@Composable
fun BSheet(
    title: String, onDismiss: () -> Unit, subtitle: String? = null, scrollable: Boolean = true,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = cs.surface,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = cs.outline) }
    ) {
        Column(
            Modifier.align(Alignment.CenterHorizontally).widthIn(max = 560.dp).fillMaxWidth()
                .navigationBarsPadding().imePadding().padding(horizontal = Sp.xl),
            verticalArrangement = Arrangement.spacedBy(Sp.l)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, T("Close", "إغلاق"), tint = cs.onSurfaceVariant) }
            }
            Column(
                Modifier.weight(1f, fill = false).fillMaxWidth()
                    .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(Sp.l)
            ) { content() }
            if (actions != null) {
                HorizontalDivider(color = cs.outlineVariant)
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Sp.s)) { actions() }
            }
            Spacer(Modifier.height(Sp.s))
        }
    }
}

/** Staggered fade-and-rise entrance (560 ms). Use `index` to delay each block. */
@Composable
fun Enter(index: Int, content: @Composable () -> Unit) {
    if (LocalReducedMotion.current || Store.campActive()) { content(); return }
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(Motion.STAGGER_MS.toLong() * index.coerceAtMost(6)); go = true }
    val a by animateFloatAsState(
        if (go) 1f else 0f,
        animationSpec = tween(Motion.ENTER_MS, easing = Motion.easeOut), label = "enter"
    )
    Box(
        Modifier.graphicsLayer {
            alpha = a
            translationY = (1f - a) * 12.dp.toPx()
            scaleX = 0.99f + 0.01f * a
            scaleY = 0.99f + 0.01f * a
        }
    ) { content() }
}

/** Solid accent "add" button, with a warm gradient and a tinted shadow. */
@Composable
fun BrandFab(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(19.dp)
    val src = remember { MutableInteractionSource() }
    Row(
        modifier.pressScale(src, 0.94f)
            .shadow(3.dp, shape, ambientColor = cs.primary.copy(alpha = 0.12f), spotColor = cs.primary.copy(alpha = 0.10f))
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(cs.primary, cs.primary)))
            .border(1.dp, Color.White.copy(alpha = 0.16f), shape)
            .clickable(interactionSource = src, indication = null, role = Role.Button, onClick = onClick)
            .heightIn(min = 54.dp).padding(horizontal = 19.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = cs.onPrimary, modifier = Modifier.size(20.dp))
        Text(text, color = cs.onPrimary, style = MaterialTheme.typography.labelLarge)
    }
}
