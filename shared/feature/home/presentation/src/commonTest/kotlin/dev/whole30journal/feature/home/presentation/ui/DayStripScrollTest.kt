package dev.whole30journal.feature.home.presentation.ui

import dev.whole30journal.feature.home.presentation.vm.HomeContract
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DayStripScrollTest {

    private fun days(total: Int, today: Int) = (1..total).map { day ->
        HomeContract.DayCell(
            dayNumber = day,
            dayOfMonth = day,
            weekdayAbbreviation = "",
            isFilled = false,
            isToday = day == today,
        )
    }

    @Test
    fun `targets the selected day instead of todays index`() {
        val index = scrollTargetIndex(days(total = 30, today = 20), selectedDay = 10)

        assertEquals(7, index)
    }

    @Test
    fun `resolves todays own index when selectedDay defaults to today on first launch`() {
        val index = scrollTargetIndex(days(total = 30, today = 20), selectedDay = 20)

        assertEquals(17, index)
    }

    @Test
    fun `clamps to the start of the list instead of going negative`() {
        val index = scrollTargetIndex(days(total = 30, today = 20), selectedDay = 1)

        assertEquals(0, index)
    }

    @Test
    fun `returns null when the selected day is not present in the list`() {
        val index = scrollTargetIndex(days(total = 30, today = 20), selectedDay = 99)

        assertNull(index)
    }
}
