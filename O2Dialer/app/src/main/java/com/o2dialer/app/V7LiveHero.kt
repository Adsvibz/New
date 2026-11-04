package com.o2dialer.app
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.o2dialer.app.ui.components.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.o2dialer.app.ui.theme.O2Colors
import com.o2dialer.app.ui.theme.O2Type

@Composable fun V7LiveHero(state: CampState,list: CallList,progress: Prog,now: Long, controls: @Composable () -> Unit = {}) {
    val next=remember(state.queuedIds,list.entries) { queuedEntries(state,list).firstOrNull() }
    val current=state.currentNumber.isNotBlank()
    val finished=state.status==CS.FINISHED
    val name=if(current) state.currentName else if(finished) "" else next?.name.orEmpty()
    val number=if(current) state.currentNumber else if(finished) "" else next?.number.orEmpty()
    val label=sessionLabel(state)
    val seconds=if(state.nextAt>now) ((state.nextAt-now+999)/1000).toInt() else 0
    val contactName = name.ifBlank { number.ifBlank { if (finished) L(R.string.ui_contacts_processed_1073aee5, progress.called) else label } }
    val longName = contactName.length > 34
    O2Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(if(finished) L(R.string.ui_session_summary_079d759d) else if(current) L(R.string.ui_current_contact_a14c7824) else L(R.string.ui_up_next_d350fa10),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                contactName, Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                fontSize = if (longName) 18.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (longName) 25.sp else 27.sp,
                textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis
            )
            // Use one physical Medium 500 face and identical metrics for both details.
            // Text instead of NumText avoids forcing the JetBrains Mono font.
            val detailStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = O2Type.Latin,
                fontWeight = FontWeight.Medium,
                fontSynthesis = FontSynthesis.None,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                textDirection = TextDirection.Ltr,
                fontFeatureSettings = "tnum"
            )
            if (number.isNotBlank()) Text(
                number,
                style = detailStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (seconds > 0) Text(
                fmtDur(seconds),
                style = detailStyle,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if(current && state.status != CS.PAUSED) ReferenceCallingWave()
            controls()
        }
    }
}
