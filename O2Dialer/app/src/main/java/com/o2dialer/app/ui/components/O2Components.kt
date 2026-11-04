package com.o2dialer.app.ui.components

import com.o2dialer.app.L
import com.o2dialer.app.R

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.o2dialer.app.*
import com.o2dialer.app.ui.theme.*

@Composable fun O2Canvas(dark: Boolean, content: @Composable () -> Unit) {
    val colors = if(dark) O2Colors.DarkCanvas else O2Colors.LightCanvas
    Box(Modifier.fillMaxSize().drawWithCache {
        val base=Brush.verticalGradient(0f to colors[0], .52f to colors[1], 1f to colors[2])
        val top=Brush.radialGradient(listOf(Color.White.copy(alpha=if(dark) .025f else .72f),Color.Transparent),Offset(size.width*.5f,0f),size.height*.55f)
        val accent=Brush.radialGradient(listOf(O2Colors.Accent.copy(alpha=.06f),Color.Transparent),Offset(size.width*.92f,size.height*.28f),size.height*.48f)
        val bottom=Brush.radialGradient(listOf(Color(0xFFD6D3CC).copy(alpha=if(dark) .02f else .35f),Color.Transparent),Offset(size.width*.08f,size.height*.78f),size.height*.5f)
        onDrawBehind { drawRect(base); if (dark) { drawRect(top);drawRect(accent);drawRect(bottom) } }
    }) { content() }
}

@Composable fun O2Card(modifier: Modifier=Modifier, hero: Boolean=false, content: @Composable ColumnScope.()->Unit) {
    val cs=MaterialTheme.colorScheme
    val surface=cs.surface
    val gradient=Brush.linearGradient(listOf(lerp(O2Colors.Accent,Color.White,.04f),lerp(O2Colors.Accent,Color(0xFF18181B),.16f)))
    Column(modifier.shadow(if(hero) 10.dp else 3.dp,O2Shape.Card,ambientColor=Color.Black.copy(alpha=.05f),spotColor=if(hero) O2Colors.Accent.copy(alpha=.24f) else Color.Black.copy(alpha=.04f))
        .clip(O2Shape.Card).then(if(hero) Modifier.background(gradient) else Modifier.background(surface))
        .border(1.dp,if(hero) Color.White.copy(alpha=.15f) else cs.outlineVariant,O2Shape.Card)) {
        CompositionLocalProvider(LocalContentColor provides if(hero) Color.White else cs.onSurface) { content() }
    }
}

@Composable fun O2StatWidget(label:String,value:String,hint:String="",modifier:Modifier=Modifier,hero:Boolean=false,progress:Float?=null) {
    O2Card(modifier,hero) {
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text(label,style=MaterialTheme.typography.bodySmall,color=if(hero) Color.White.copy(alpha=.9f) else MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(value,Modifier.weight(1f),style=MaterialTheme.typography.displayMedium,maxLines=1,overflow=TextOverflow.Ellipsis)
                if(hero && progress!=null) Row(Modifier.background(Color.White.copy(alpha=.2f),CircleShape).padding(horizontal=10.dp,vertical=2.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                    Icon(O2Icons.TrendingUp,null,Modifier.size(12.dp))
                    Text("${(progress*100).toInt()}%",fontSize=11.sp,fontWeight=FontWeight.SemiBold)
                }
            }
            if(progress!=null) O2Progress(progress,if(hero) Color.White else O2Colors.Accent)
            if(hint.isNotBlank()) Text(hint,style=MaterialTheme.typography.labelMedium,color=if(hero) Color.White.copy(alpha=.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable fun O2MetricGrid(items: List<@Composable (Modifier)->Unit>) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns=if(maxWidth<300.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale>1.5f) 1 else 2
        Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
            items.chunked(columns).forEach { group ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                    group.forEachIndexed { index, item -> ReferenceEntrance(items.indexOf(item), Modifier.weight(1f).fillMaxHeight()) { item(Modifier.fillMaxSize()) } }
                    if(group.size<columns) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable fun O2Progress(value:Float,color:Color=O2Colors.Accent) {
    val p by animateFloatAsState(value.coerceIn(0f,1f),tween(if(LocalReducedMotion.current) 0 else 500),label="referenceProgress")
    Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(color.copy(alpha=.16f))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(p).background(color))
    }
}

@Composable fun O2StatusBadge(text:String,color:Color=O2Colors.Accent) {
    Row(Modifier.clip(CircleShape).background(color.copy(alpha=.11f)).padding(horizontal=10.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(5.dp).background(color,CircleShape))
        Text(text,color=if(isDarkTheme()) lerp(color,Color.White,.3f) else color,style=MaterialTheme.typography.labelMedium)
    }
}

@Composable fun O2PrimaryButton(text:String,onClick:()->Unit,modifier:Modifier=Modifier,icon:ImageVector?=null,enabled:Boolean=true) {
    BButton(text,onClick,modifier,icon=icon,enabled=enabled,compact=true)
}

@Composable fun O2InputField(value:String,onChange:(String)->Unit,placeholder:String,modifier:Modifier=Modifier) {
    val cs = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth().height(44.dp).background(cs.onSurface.copy(alpha=.04f), CircleShape).padding(horizontal=14.dp),
        verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        Icon(O2Icons.Search,null,Modifier.size(16.dp),tint=cs.onSurfaceVariant)
        androidx.compose.foundation.text.BasicTextField(value,onChange,Modifier.weight(1f),singleLine=true,
            textStyle=MaterialTheme.typography.bodyMedium.copy(fontSize=12.5.sp,color=cs.onSurface),cursorBrush=SolidColor(cs.onSurface),
            decorationBox={ field -> Box { if(value.isEmpty()) Text(placeholder,fontSize=12.5.sp,color=cs.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis); field() } })
        if(value.isNotEmpty()) IconButton({ onChange("") },Modifier.size(28.dp)) { Icon(O2Icons.Close,L(R.string.ui_clear_aeaec2c6),Modifier.size(14.dp)) }
    }
}

@Composable fun O2TopHeader(title:String,subtitle:String?=null,actions:@Composable RowScope.()->Unit={}) {
    Row(Modifier.fillMaxWidth().padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f)) {
            if(subtitle!=null) Text(subtitle,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text(title,style=MaterialTheme.typography.headlineLarge)
        }
        SoftIconButton(O2Icons.Globe,L(R.string.ui_change_language_1c32e0d1),{ Store.updateCfg { it.copy(lang=1-it.lang) } })
        actions()
    }
}

@Composable fun O2BottomNavBar() {
    val cs=MaterialTheme.colorScheme
    val dark=isDarkTheme()
    val count=Store.lists.count { Store.isRunningList(it.id) }
    val tabs=listOf(Triple(1,O2Icons.Phone,L(R.string.ui_dialer_bd42b0f9)),Triple(2,O2Icons.Chart,L(R.string.ui_results_e87ed283)),Triple(0,O2Icons.Megaphone,L(R.string.ui_campaigns_d8df9f8a)),Triple(3,O2Icons.Clock,L(R.string.ui_history_1bdd62e7)),Triple(4,O2Icons.Settings,L(R.string.ui_settings_f3f3f790)))
    val navShape=RoundedCornerShape(30.dp)
    val navFill=if(dark) Color(0xFF252932) else Color(0xFFF2F3F6)
    val navBorder=if(dark) Color.White.copy(alpha=.20f) else Color(0xFFCFD3DA)
    BoxWithConstraints(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=16.dp,vertical=12.dp),contentAlignment=Alignment.Center) {
        val navItemSize=when {
            maxWidth < 280.dp -> 40.dp
            maxWidth < 310.dp -> 44.dp
            else -> 48.dp
        }
        Row(Modifier.widthIn(max=390.dp).fillMaxWidth()
            .shadow(14.dp,navShape,ambientColor=Color.Black.copy(alpha=.10f),spotColor=Color.Black.copy(alpha=.18f))
            .clip(navShape).background(navFill)
            .border(1.dp,navBorder,navShape).padding(6.dp),horizontalArrangement=Arrangement.SpaceBetween) {
            tabs.forEach { (id,icon,label) ->
                val active=Store.tab==id
                val reduced = LocalReducedMotion.current
                val pop = remember { Animatable(1f) }
                val interaction = remember { MutableInteractionSource() }
                LaunchedEffect(active, reduced) {
                    if (active && !reduced) {
                        pop.snapTo(.82f)
                        pop.animateTo(1f, keyframes { durationMillis = 320; .82f at 0; 1.08f at 192 using ReferenceEase; 1f at 320 })
                    } else pop.snapTo(1f)
                }
                Box(Modifier.size(navItemSize).graphicsLayer { scaleX = pop.value; scaleY = pop.value }.pressScale(interaction, .9f).clip(CircleShape).background(if(active) O2Colors.Ink.copy(alpha=.9f) else Color.Transparent)
                    .semantics { contentDescription=if(id==0 && count>0) "$label ($count)" else label; selected=active; role=Role.Tab }
                    .clickable(interactionSource = interaction, indication = null) { if(id==4) Store.openSettings() else { if(id==0) Store.selectedListId=null; Store.tab=id } },contentAlignment=Alignment.Center) {
                    Icon(icon,null,tint=if(active) Color.White else if(dark) cs.onSurface.copy(alpha=.80f) else O2Colors.Ink.copy(alpha=.68f),modifier=Modifier.size(19.dp))
                    // A dot replaces the cramped circular count badge; stays inside the tab's bounds.
                    if(id==0 && count>0) Box(Modifier.align(Alignment.TopEnd).padding(top=4.dp,end=4.dp).size(7.dp).background(O2Colors.Accent,CircleShape))
                }
            }
        }
    }
}
