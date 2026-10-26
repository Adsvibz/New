@file:OptIn(ExperimentalMaterial3Api::class)

package com.o2dialer.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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

// ====================================================================== layout helpers
/** Caps the content width so tablets / landscape stay tidy. Phones are unaffected. */
@Composable
fun Responsive(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.TopCenter) {
        Box(Modifier.widthIn(max = 600.dp).fillMaxSize()) { content() }
    }
}

@Composable
fun isCompact(): Boolean = LocalConfiguration.current.screenWidthDp < 360

// ====================================================================== motion
@Composable
fun Modifier.pressScale(src: MutableInteractionSource): Modifier {
    val pressed by src.collectIsPressedAsState()
    val s by animateFloatAsState(if (pressed) 0.975f else 1f, tween(110), label = "press")
    return this.graphicsLayer { scaleX = s; scaleY = s }
}

@Composable
fun PulseDot(color: Color, size: Dp = 10.dp) {
    val t = rememberInfiniteTransition(label = "pulse")
    val a = t.animateFloat(1f, 0.35f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a")
    Box(Modifier.size(size).graphicsLayer { alpha = a.value }.clip(CircleShape).background(color))
}

/** Small solid status dot. Cleaner than a coloured pill in dense rows. */
@Composable
fun Dot(color: Color, size: Dp = 7.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(color))
}

/** Loading placeholder. */
@Composable
fun Skeleton(modifier: Modifier = Modifier, radius: Dp = 12.dp) {
    val t = rememberInfiniteTransition(label = "skeleton")
    val a = t.animateFloat(0.45f, 0.9f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a")
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
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(500), label = "bar")
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(track)) {
        Box(Modifier.fillMaxWidth(p).fillMaxHeight().clip(CircleShape).background(color))
    }
}

@Composable
fun ProgressRing(
    progress: Float, size: Dp, stroke: Dp, color: Color, track: Color, content: @Composable () -> Unit
) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(600), label = "ring")
    Box(Modifier.size(size), Alignment.Center) {
        CircularProgressIndicator(
            progress = { p }, modifier = Modifier.fillMaxSize(), color = color,
            trackColor = track, strokeWidth = stroke, strokeCap = StrokeCap.Round
        )
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
    val m = modifier.pressScale(src).heightIn(min = if (compact) 44.dp else 52.dp)
    val shape = RoundedCornerShape(if (compact) 13.dp else 15.dp)
    val pad = PaddingValues(horizontal = if (iconOnly) 12.dp else 18.dp)
    val flat = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
    val body: @Composable RowScope.() -> Unit = {
        if (icon != null) Icon(icon, if (iconOnly) text else null, Modifier.size(19.dp))
        if (icon != null && !iconOnly) Spacer(Modifier.width(8.dp))
        if (!iconOnly) Text(text, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    when (kind) {
        BtnKind.Primary -> Button(
            onClick, m, enabled, shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = cs.primary, contentColor = cs.onPrimary,
                disabledContainerColor = cs.onSurface.copy(alpha = 0.10f),
                disabledContentColor = cs.onSurface.copy(alpha = 0.35f)
            ),
            elevation = flat, contentPadding = pad, interactionSource = src, content = body
        )
        BtnKind.Tonal -> Button(
            onClick, m, enabled, shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = cs.primary.copy(alpha = 0.09f), contentColor = cs.primary,
                disabledContainerColor = cs.onSurface.copy(alpha = 0.06f),
                disabledContentColor = cs.onSurface.copy(alpha = 0.3f)
            ),
            elevation = flat, contentPadding = pad, interactionSource = src, content = body
        )
        BtnKind.Danger -> Button(
            onClick, m, enabled, shape,
            colors = ButtonDefaults.buttonColors(containerColor = cs.error, contentColor = cs.onError),
            elevation = flat, contentPadding = pad, interactionSource = src, content = body
        )
        BtnKind.Outline -> OutlinedButton(
            onClick, m, enabled, shape, border = BorderStroke(1.dp, cs.outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.onSurface),
            contentPadding = pad, interactionSource = src, content = body
        )
        BtnKind.Ghost -> TextButton(
            onClick, m, enabled, shape,
            colors = ButtonDefaults.textButtonColors(contentColor = cs.onSurface),
            contentPadding = pad, interactionSource = src, content = body
        )
    }
}

/** A soft, square icon button used in headers and inline actions. */
@Composable
fun SoftIconButton(
    icon: ImageVector, desc: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    container: Color = MaterialTheme.colorScheme.surface
) {
    val cs = MaterialTheme.colorScheme
    val src = remember { MutableInteractionSource() }
    Box(
        modifier.size(40.dp).pressScale(src).clip(RoundedCornerShape(13.dp)).background(container)
            .border(1.dp, cs.outline, RoundedCornerShape(13.dp))
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
    outlined: Boolean = true
) {
    val src = remember { MutableInteractionSource() }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier.size(60.dp).pressScale(src).clip(CircleShape).background(container)
                .then(if (outlined) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape) else Modifier)
                .clickable(interactionSource = src, indication = null, role = Role.Button, onClick = onClick),
            Alignment.Center
        ) { Icon(icon, null, tint = content, modifier = Modifier.size(24.dp)) }
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
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
    val shape = RoundedCornerShape(Rd.m)
    TextField(
        value, onChange, modifier.fillMaxWidth(), singleLine = singleLine, minLines = minLines,
        maxLines = if (singleLine) 1 else maxOf(6, minLines), isError = isError, shape = shape,
        label = if (label != null) { { Text(label) } } else null,
        placeholder = if (placeholder != null) { { Text(placeholder, color = cs.onSurfaceVariant.copy(alpha = 0.8f)) } } else null,
        supportingText = if (supporting != null) { { Text(supporting) } } else null,
        leadingIcon = if (leading != null) { { Icon(leading, null, modifier = Modifier.size(19.dp)) } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = cs.surfaceVariant, unfocusedContainerColor = cs.surfaceVariant,
            disabledContainerColor = cs.surfaceVariant,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent, errorIndicatorColor = Color.Transparent,
            focusedTextColor = cs.onSurface, unfocusedTextColor = cs.onSurface,
            focusedLabelColor = cs.primary, unfocusedLabelColor = cs.onSurfaceVariant,
            cursorColor = cs.primary
        )
    )
}

@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(cs.surfaceVariant).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEachIndexed { i, o ->
            val on = i == selected
            val bg by animateColorAsState(if (on) cs.surface else Color.Transparent, tween(180), label = "seg")
            val fg by animateColorAsState(if (on) cs.onSurface else cs.onSurfaceVariant, tween(180), label = "segFg")
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(bg)
                    .then(if (on) Modifier.shadow(1.dp, RoundedCornerShape(10.dp)) else Modifier)
                    .clickable { onSelect(i) }.heightIn(min = 40.dp),
                Alignment.Center
            ) {
                Text(
                    o, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal, maxLines = 1,
                    color = fg, style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// ====================================================================== surfaces
@Composable
fun Panel(
    modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, accent: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val shape = RoundedCornerShape(Rd.l)
    val border = if (accent != null) BorderStroke(1.5.dp, accent) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    val flat = CardDefaults.cardElevation(defaultElevation = 0.dp)
    if (onClick != null) Card(
        onClick = onClick, modifier = modifier, shape = shape, colors = colors, elevation = flat,
        border = border, content = content
    )
    else Card(modifier = modifier, shape = shape, colors = colors, elevation = flat, border = border, content = content)
}

/** One-pixel separator, optionally indented to line up with text after a leading avatar. */
@Composable
fun Hairline(modifier: Modifier = Modifier, startIndent: Dp = 0.dp) {
    Box(
        modifier.fillMaxWidth().padding(start = startIndent).height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.75f))
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text, modifier = modifier,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = if (Store.cfg.lang == 0) 0.5.sp else 0.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun IconTile(icon: ImageVector, color: Color = MaterialTheme.colorScheme.primary, size: Dp = 42.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.10f)),
        Alignment.Center
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(size * 0.48f))
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
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(Rd.m)).background(c.copy(alpha = 0.07f))
            .border(1.dp, c.copy(alpha = 0.16f), RoundedCornerShape(Rd.m)).padding(Sp.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(c.copy(alpha = 0.12f)), Alignment.Center) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(Sp.m))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (message != null) Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (actionText != null && onAction != null) TextButton(onClick = onAction) {
            Text(actionText, color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun ToastHost(modifier: Modifier = Modifier) {
    val t = Store.toast
    var last by remember { mutableStateOf<UiToast?>(null) }
    LaunchedEffect(t?.id) {
        if (t != null) {
            last = t
            delay(2400)
            if (Store.toast?.id == t.id) Store.toast = null
        }
    }
    AnimatedVisibility(
        visible = t != null, modifier = modifier,
        enter = slideInVertically { -it } + fadeIn(), exit = slideOutVertically { -it } + fadeOut()
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
            Surface(
                shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 6.dp
            ) {
                Row(Modifier.padding(horizontal = Sp.l, vertical = Sp.m), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = c, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Sp.s))
                    Text(shown.msg, color = MaterialTheme.colorScheme.inverseOnSurface, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector, title: String, sub: String, modifier: Modifier = Modifier,
    actionText: String? = null, onAction: (() -> Unit)? = null
) {
    Column(
        modifier.fillMaxSize().padding(Sp.xxxl), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(84.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)), Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(Sp.xl))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Sp.s))
        Text(
            sub, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.widthIn(max = 320.dp)
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(Sp.xl))
            BButton(actionText, onAction, compact = true)
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

@Composable
fun Avatar(text: String, color: Color = MaterialTheme.colorScheme.primary, size: Dp = 44.dp) {
    val ch = text.trim().firstOrNull { it.isLetter() }?.uppercase() ?: "#"
    Box(Modifier.size(size).clip(CircleShape).background(color.copy(alpha = 0.12f)), Alignment.Center) {
        Text(ch, color = readable(color), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun Pill(text: String, color: Color, icon: ImageVector? = null, onClick: (() -> Unit)? = null) {
    val fg = readable(color)
    var m = Modifier.clip(RoundedCornerShape(50))
    if (onClick != null) m = m.clickable { onClick() }
    Row(
        m.background(color.copy(alpha = 0.10f)).heightIn(min = 24.dp)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text, color = fg,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
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
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(cs.surface)
            .border(1.dp, cs.outline, RoundedCornerShape(50))
            .pressScale(src)
            .clickable(interactionSource = src, indication = null) { onClick() }
            .heightIn(min = 40.dp).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = cs.primary, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun WhatsAppButton(number: String, size: Dp = 40.dp) {
    val ctx = LocalContext.current
    Box(
        Modifier.size(size).clip(CircleShape).background(Color(0xFF128C7E)).clickable { openWhatsApp(ctx, number) },
        Alignment.Center
    ) {
        Icon(Icons.Rounded.Chat, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(size * 0.48f))
    }
}

@Composable
fun LogoMark(dp: Dp) {
    Canvas(Modifier.size(dp)) {
        val r = this.size.minDimension * 0.28f
        val cy = this.size.height / 2f
        drawCircle(Color.White, r, Offset(this.size.width * 0.40f, cy))
        drawCircle(Color.White.copy(alpha = 0.55f), r, Offset(this.size.width * 0.62f, cy))
    }
}

// ====================================================================== top bar
@Composable
private fun LivePill() {
    val pct = Store.lists.find { it.id == Store.camp.listId }?.let { Store.prog(it).pct } ?: 0
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(Sem.success.copy(alpha = 0.10f))
            .clickable { Store.tab = 1 }.heightIn(min = 32.dp).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PulseDot(Sem.success, 7.dp)
        Spacer(Modifier.width(6.dp))
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
            .padding(start = Sp.xl, end = Sp.l, top = Sp.m, bottom = Sp.s)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                SoftIconButton(Icons.AutoMirrored.Filled.ArrowBack, T("Back", "رجوع"), onBack)
                Spacer(Modifier.width(Sp.m))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.headlineLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Spacer(Modifier.height(1.dp))
                    Text(
                        subtitle, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis
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
        onDismissRequest = onDismiss, shape = RoundedCornerShape(Rd.xl), containerColor = MaterialTheme.colorScheme.surface,
        title = if (title != null) { { Text(title, style = MaterialTheme.typography.headlineSmall) } } else null,
        text = { Column(verticalArrangement = Arrangement.spacedBy(Sp.m), content = content) },
        confirmButton = {
            if (confirmText != null && onConfirm != null)
                BButton(
                    confirmText, onConfirm, kind = if (destructive) BtnKind.Danger else BtnKind.Primary,
                    compact = true, enabled = confirmEnabled
                )
        },
        dismissButton = { BButton(dismissText, onDismiss, kind = BtnKind.Ghost, compact = true) }
    )
}

/**
 * Modern replacement for centred forms: a full-width bottom sheet with a big title,
 * comfortable fields and one clear primary action.
 */
@Composable
fun BSheet(
    title: String, onDismiss: () -> Unit, subtitle: String? = null, scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = Rd.xl, topEnd = Rd.xl),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) }
    ) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(horizontal = Sp.xl).padding(bottom = Sp.xxl),
            verticalArrangement = Arrangement.spacedBy(Sp.l)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                if (subtitle != null) Text(
                    subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

/** Staggered fade-and-rise entrance (450 ms, 12 dp). Use `index` to delay each block a little more. */
private val EnterEase = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

@Composable
fun Enter(index: Int, content: @Composable () -> Unit) {
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60L * index); go = true }
    val a by animateFloatAsState(if (go) 1f else 0f, tween(450, easing = EnterEase), label = "enter")
    Box(Modifier.graphicsLayer { alpha = a; translationY = (1f - a) * 12.dp.toPx() }) { content() }
}

/** Solid accent "add" button, no glow. */
@Composable
fun BrandFab(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(17.dp)
    val src = remember { MutableInteractionSource() }
    Row(
        modifier.pressScale(src).shadow(10.dp, shape, ambientColor = cs.primary.copy(alpha = 0.3f), spotColor = cs.primary.copy(alpha = 0.3f))
            .clip(shape).background(cs.primary)
            .clickable(interactionSource = src, indication = null, role = Role.Button, onClick = onClick)
            .heightIn(min = 52.dp).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = cs.onPrimary, modifier = Modifier.size(20.dp))
        Text(text, color = cs.onPrimary, style = MaterialTheme.typography.labelLarge)
    }
}
