package com.o2dialer.app
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.o2dialer.app.ui.components.*

@Composable fun V7HistoryHeader(total: Int, answered: Int, missed: Int) {
    O2MetricGrid(listOf(
        { m -> O2StatWidget(L(R.string.ui_total_calls_ace8093b),total.toString(),L(R.string.ui_saved_history_c6464f85),m,true) },
        { m -> O2StatWidget(L(R.string.ui_answered_f0c92feb),answered.toString(),L(R.string.ui_unanswered_failed_f4d4b286, missed),m) }
    ))
}
