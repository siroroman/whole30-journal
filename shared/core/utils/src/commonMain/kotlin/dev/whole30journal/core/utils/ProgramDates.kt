package dev.whole30journal.core.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

fun dateForDay(dayNumber: Int, startDate: LocalDate): LocalDate = startDate.plus(dayNumber - 1, DateTimeUnit.DAY)
