package dev.whole30journal.feature.home.presentation.usecase

import dev.whole30journal.feature.home.presentation.generated.resources.Res
import dev.whole30journal.feature.home.presentation.generated.resources.home_today_label
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString

class FormatDayLabelUseCase {

    enum class Style { Short, Long }

    suspend operator fun invoke(date: LocalDate, today: LocalDate, style: Style = Style.Long): String {
        if (date == today) return getString(Res.string.home_today_label)
        val shortDate = "${date.day}.${date.month.ordinal + 1}.${date.year}"
        return if (style == Style.Short) shortDate else "${fullWeekdayName(date.dayOfWeek)} $shortDate"
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
