package dev.whole30journal.core.utils

import dev.whole30journal.core.utils.generated.resources.Res
import dev.whole30journal.core.utils.generated.resources.utils_today_label
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString

class DateFormatter {

    enum class Style { Short, Long }

    suspend operator fun invoke(date: LocalDate, today: LocalDate, style: Style = Style.Long): String {
        if (date == today && style == Style.Long) return getString(Res.string.utils_today_label)
        val shortDate = "${date.day}.${date.month.ordinal + 1}.${date.year}"
        return if (style == Style.Short) shortDate else "${fullWeekdayName(date.dayOfWeek)} $shortDate"
    }

    fun weekdayAbbreviation(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
        DayOfWeek.MONDAY -> "MO"
        DayOfWeek.TUESDAY -> "TU"
        DayOfWeek.WEDNESDAY -> "WE"
        DayOfWeek.THURSDAY -> "TH"
        DayOfWeek.FRIDAY -> "FR"
        DayOfWeek.SATURDAY -> "SA"
        DayOfWeek.SUNDAY -> "SU"
    }

    private fun fullWeekdayName(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Monday"
        DayOfWeek.TUESDAY -> "Tuesday"
        DayOfWeek.WEDNESDAY -> "Wednesday"
        DayOfWeek.THURSDAY -> "Thursday"
        DayOfWeek.FRIDAY -> "Friday"
        DayOfWeek.SATURDAY -> "Saturday"
        DayOfWeek.SUNDAY -> "Sunday"
    }
}
