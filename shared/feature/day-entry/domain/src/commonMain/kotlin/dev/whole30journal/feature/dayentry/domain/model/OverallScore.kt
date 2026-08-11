package dev.whole30journal.feature.dayentry.domain.model

import kotlin.math.roundToInt

private const val MAX_SCORE = 10

fun overallScore(scores: List<Int?>): Int? {
    val values = scores.filterNotNull()
    if (values.isEmpty()) return null
    return values.average().roundToInt().coerceIn(1, MAX_SCORE)
}
