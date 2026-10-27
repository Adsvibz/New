package com.o2dialer.app

/** Re-evaluated at confirmation, never trusts a stale UI selection. */
internal fun removableCampaignIds(existing: Set<String>, requested: Set<String>, activeId: String?): Set<String> =
    requested.intersect(existing).filterTo(mutableSetOf()) { it != activeId }
