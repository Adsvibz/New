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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.o2dialer.app.ui.theme.O2Colors

@Composable fun V7LiveHero(state: CampState,list: CallList,progress: Prog,now: Long, controls: @Composable () -> Unit = {}) {
    val next=remember(state.queuedIds,list.entries) { queuedEntries(state,list).firstOrNull() }
    val current=state.currentNumber.isNotBlank()
    val finished=state.status==CS.FINISHED
    val name=if(current) state.currentName else if(finished) "" else next?.name.orEmpty()
    val number=if(current) state.currentNumber else if(finished) "" else next?.number.orEmpty()
    val label=sessionLabel(state)
    val seconds=if(state.nextAt>now) ((state.nextAt-now+999)/1000).toInt() else 0
    O2Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)) {
            O2StatusBadge(label,if(state.error) MaterialTheme.colorScheme.error else if(state.status==CS.PAUSED) Sem.warning else O2Colors.Accent)
            Text(if(finished) L(R.string.ui_session_summary_079d759d) else if(current) L(R.string.ui_current_contact_a14c7824) else L(R.string.ui_up_next_d350fa10),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text(name.ifBlank { number.ifBlank { if(finished) L(R.string.ui_contacts_processed_1073aee5, progress.called) else label } },fontSize=25.sp,fontWeight=FontWeight.ExtraBold,lineHeight=36.sp,textAlign=TextAlign.Center)
            if(number.isNotBlank()) NumText(number,style=MaterialTheme.typography.bodyMedium)
            if(seconds>0) NumText(fmtDur(seconds),style=MaterialTheme.typography.displayLarge)
            if(current && state.status != CS.PAUSED) ReferenceCallingWave()
            controls()
            if(state.message.isNotBlank()) Text(state.message,textAlign=TextAlign.Center,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
