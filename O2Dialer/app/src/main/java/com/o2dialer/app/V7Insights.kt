package com.o2dialer.app
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.o2dialer.app.ui.components.*

@Composable fun V7ResultsHeader(progress: Prog) {
    val p=progress
    O2MetricGrid(listOf(
        { m -> O2StatWidget(L(R.string.ui_called_c764210c),p.called.toString(),"${p.pct}%",m,true,p.pct/100f) },
        { m -> O2StatWidget(L(R.string.ui_remaining_8cbe3646),p.remaining.toString(),L(R.string.ui_of_contacts_24670923, p.total),m) },
        { m -> O2StatWidget(L(R.string.ui_answered_f0c92feb),p.answered.toString(),L(R.string.ui_confirmed_calls_ac9a125c),m) },
        { m -> O2StatWidget(L(R.string.ui_no_answer_failed_ffcfbee5),(p.noAnswer+p.failed).toString(),L(R.string.ui_missed_failed_05f7669d, p.noAnswer, p.failed),m) }
    ))
}
