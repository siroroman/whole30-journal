package dev.whole30journal.core.utils

import dev.whole30journal.core.utils.generated.resources.Res
import dev.whole30journal.core.utils.generated.resources.utils_today_label
import dev.whole30journal.core.utils.generated.resources.utils_weekday_friday_full
import dev.whole30journal.core.utils.generated.resources.utils_weekday_friday_short
import dev.whole30journal.core.utils.generated.resources.utils_weekday_monday_full
import dev.whole30journal.core.utils.generated.resources.utils_weekday_monday_short
import dev.whole30journal.core.utils.generated.resources.utils_weekday_saturday_full
import dev.whole30journal.core.utils.generated.resources.utils_weekday_saturday_short
import dev.whole30journal.core.utils.generated.resources.utils_weekday_sunday_full
import dev.whole30journal.core.utils.generated.resources.utils_weekday_sunday_short
import dev.whole30journal.core.utils.generated.resources.utils_weekday_thursday_full
import dev.whole30journal.core.utils.generated.resources.utils_weekday_thursday_short
import dev.whole30journal.core.utils.generated.resources.utils_weekday_tuesday_full
import dev.whole30journal.core.utils.generated.resources.utils_weekday_tuesday_short
import dev.whole30journal.core.utils.generated.resources.utils_weekday_wednesday_full
import dev.whole30journal.core.utils.generated.resources.utils_weekday_wednesday_short
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class DateFormatter {

    enum class Style { Short, Long }

    suspend operator fun invoke(date: LocalDate, today: LocalDate, style: Style = Style.Long): String {
        if (date == today && style == Style.Long) return getString(Res.string.utils_today_label)
        val shortDate = "${date.day}.${date.month.ordinal + 1}.${date.year}"
        return if (style == Style.Short) shortDate else "${fullWeekdayName(date.dayOfWeek)} $shortDate"
    }

    suspend fun weekdayAbbreviation(dayOfWeek: DayOfWeek): String = getString(shortWeekdayNameRes(dayOfWeek))

    private suspend fun fullWeekdayName(dayOfWeek: DayOfWeek): String = getString(fullWeekdayNameRes(dayOfWeek))

    private fun fullWeekdayNameRes(dayOfWeek: DayOfWeek): StringResource = when (dayOfWeek) {
        DayOfWeek.MONDAY -> Res.string.utils_weekday_monday_full
        DayOfWeek.TUESDAY -> Res.string.utils_weekday_tuesday_full
        DayOfWeek.WEDNESDAY -> Res.string.utils_weekday_wednesday_full
        DayOfWeek.THURSDAY -> Res.string.utils_weekday_thursday_full
        DayOfWeek.FRIDAY -> Res.string.utils_weekday_friday_full
        DayOfWeek.SATURDAY -> Res.string.utils_weekday_saturday_full
        DayOfWeek.SUNDAY -> Res.string.utils_weekday_sunday_full
    }

    private fun shortWeekdayNameRes(dayOfWeek: DayOfWeek): StringResource = when (dayOfWeek) {
        DayOfWeek.MONDAY -> Res.string.utils_weekday_monday_short
        DayOfWeek.TUESDAY -> Res.string.utils_weekday_tuesday_short
        DayOfWeek.WEDNESDAY -> Res.string.utils_weekday_wednesday_short
        DayOfWeek.THURSDAY -> Res.string.utils_weekday_thursday_short
        DayOfWeek.FRIDAY -> Res.string.utils_weekday_friday_short
        DayOfWeek.SATURDAY -> Res.string.utils_weekday_saturday_short
        DayOfWeek.SUNDAY -> Res.string.utils_weekday_sunday_short
    }
}
