package com.example.lifetrack.domain.progress

import com.example.lifetrack.domain.model.CompletionStatus
import kotlin.math.roundToInt

data class DayProgress(
    val done: Int = 0,
    val skipped: Int = 0,
    val pending: Int = 0
) {
    val expected: Int get() = done + skipped + pending

    val hasPlanned: Boolean get() = expected > 0

    val percent: Int get() = if (expected == 0) 0 else (done * 100.0 / expected).roundToInt()
}

fun calculateDayProgress(statuses: Iterable<CompletionStatus?>): DayProgress {
    var done = 0
    var skipped = 0
    var pending = 0
    statuses.forEach { status ->
        when (status) {
            CompletionStatus.DONE -> done += 1
            CompletionStatus.SKIPPED -> skipped += 1
            null -> pending += 1
        }
    }
    return DayProgress(done = done, skipped = skipped, pending = pending)
}
