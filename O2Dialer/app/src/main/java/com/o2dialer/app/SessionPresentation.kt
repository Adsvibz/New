package com.o2dialer.app

/** UI derives the next contact from the service's actual queue (including retries/shuffle). */
fun queuedEntries(state: CampState, list: CallList): List<Entry> {
    val byId = list.entries.associateBy { it.id }
    return state.queuedIds.mapNotNull { byId[it] }
}

fun sessionLabel(state: CampState): String = when {
    state.status == CS.FINISHED && state.error -> T("Session interrupted", "الجلسة توقفت بسبب خطأ")
    state.status == CS.FINISHED && state.completed -> T("Session complete", "اكتملت الجلسة")
    state.status == CS.FINISHED -> T("Session stopped", "توقفت الجلسة")
    state.status == CS.PAUSED -> T("Queue paused", "قائمة الاتصال متوقفة مؤقتًا")
    state.status == CS.WAITING -> T("Waiting", "في الانتظار")
    state.evaluating -> T("Checking result", "التحقق من النتيجة")
    state.awaitingStage -> T("Awaiting outcome", "بانتظار التقييم")
    state.currentNumber.isNotBlank() -> T("Calling", "جارٍ الاتصال")
    else -> T("Preparing next call", "تحضير المكالمة التالية")
}

/** Editing an unrelated historical outcome must never release or hold a live queue. */
fun stageCanControlQueue(state: CampState, listId: String): Boolean =
    state.listId == listId && state.status in setOf(CS.RUNNING, CS.PAUSED, CS.WAITING) &&
        state.currentNumber.isBlank() && !state.evaluating
