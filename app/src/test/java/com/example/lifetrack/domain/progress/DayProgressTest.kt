package com.example.lifetrack.domain.progress

import com.example.lifetrack.domain.model.CompletionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DayProgressTest {

    @Test
    fun allPlannedHabitsDoneGivesFullProgress() {
        val progress = calculateDayProgress(
            listOf(CompletionStatus.DONE, CompletionStatus.DONE)
        )

        assertEquals(2, progress.done)
        assertEquals(0, progress.skipped)
        assertEquals(0, progress.pending)
        assertEquals(2, progress.expected)
        assertEquals(100, progress.percent)
        assertTrue(progress.hasPlanned)
    }

    @Test
    fun partiallyDoneDayRoundsPercent() {
        val progress = calculateDayProgress(
            listOf(
                CompletionStatus.DONE,
                CompletionStatus.DONE,
                null
            )
        )

        assertEquals(2, progress.done)
        assertEquals(1, progress.pending)
        assertEquals(3, progress.expected)
        assertEquals(67, progress.percent)
    }

    @Test
    fun skippedHabitStaysExpectedButIsNotDone() {
        val progress = calculateDayProgress(
            listOf(
                CompletionStatus.DONE,
                CompletionStatus.SKIPPED,
                null
            )
        )

        assertEquals(1, progress.done)
        assertEquals(1, progress.skipped)
        assertEquals(1, progress.pending)
        assertEquals(3, progress.expected)
        assertEquals(33, progress.percent)
    }

    @Test
    fun dayWithoutPlannedHabitsHasZeroPercent() {
        val progress = calculateDayProgress(emptyList())

        assertEquals(0, progress.done)
        assertEquals(0, progress.expected)
        assertEquals(0, progress.percent)
        assertFalse(progress.hasPlanned)
    }

    @Test
    fun unmarkedHabitsArePendingNotSkipped() {
        val progress = calculateDayProgress(listOf(null, null))

        assertEquals(0, progress.done)
        assertEquals(0, progress.skipped)
        assertEquals(2, progress.pending)
        assertEquals(0, progress.percent)
    }
}
