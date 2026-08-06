package dev.whole30journal.umbrella.seed

import dev.whole30journal.feature.dayentry.domain.model.Achievement
import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.model.Meal
import dev.whole30journal.feature.dayentry.domain.model.Metric
import dev.whole30journal.feature.dayentry.domain.model.MetricTitle
import dev.whole30journal.feature.dayentry.domain.usecase.SaveDayEntryUseCase
import dev.whole30journal.feature.program.domain.usecase.ConfigureProgramUseCase
import dev.whole30journal.feature.program.domain.usecase.GetProgramUseCase
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Populates a fresh install's database with a realistic-looking 30-day program so the Home screen
 * has real data to render. No-ops once a [dev.whole30journal.feature.program.domain.model.Program]
 * already exists, so it never overwrites real data on later launches.
 */
class SampleDataSeeder(
    private val getProgram: GetProgramUseCase,
    private val configureProgram: ConfigureProgramUseCase,
    private val saveDayEntry: SaveDayEntryUseCase,
    @OptIn(ExperimentalTime::class)
    private val clock: Clock = Clock.System,
) {
    @OptIn(ExperimentalTime::class)
    suspend fun seedIfNeeded() {
        if (getProgram().getOrNull() != null) return
        val startDate = clock.todayIn(TimeZone.currentSystemDefault()).minus(SEED_ELAPSED_DAYS, DateTimeUnit.DAY)
        val program = configureProgram(startDate = startDate, durationDays = SEED_DURATION_DAYS).getOrThrow()
        val elapsedDays = 1..program.currentDayNumber.toInt()
        val unfilledDays = elapsedDays.shuffled(Random(SEED_RANDOM_SEED)).take(UNFILLED_DAY_COUNT).toSet()
        elapsedDays
            .filterNot { it in unfilledDays }
            .forEach { day -> saveDayEntry(buildSampleDayEntry(day, program.startDate)).getOrThrow() }
    }
}

private const val SEED_ELAPSED_DAYS = 14
private const val SEED_DURATION_DAYS = 30L
private const val UNFILLED_DAY_COUNT = 2
private const val SEED_RANDOM_SEED = 42

private fun buildSampleDayEntry(dayNumber: Int, startDate: LocalDate): DayEntry {
    val random = Random(SEED_RANDOM_SEED + dayNumber)
    val date = startDate.plus(dayNumber - 1, DateTimeUnit.DAY)

    val energy = trendingScore(dayNumber, random)
    val mood = trendingScore(dayNumber, random)
    val sleep = trendingScore(dayNumber, random)
    val cravings = trendingScore(dayNumber, random)
    val overall = listOf(energy, mood, sleep, cravings).average().roundToInt().coerceIn(1, MAX_SCORE)

    return DayEntry(
        dayNumber = dayNumber.toLong(),
        date = date.toString(),
        metrics = listOf(
            Metric(MetricTitle.OVERALL, "leaf", overall.toLong(), MAX_SCORE.toLong(), ""),
            Metric(MetricTitle.ENERGY, "energy", energy.toLong(), MAX_SCORE.toLong(), ""),
            Metric(MetricTitle.MOOD, "mood", mood.toLong(), MAX_SCORE.toLong(), ""),
            Metric(MetricTitle.SLEEP, "sleep", sleep.toLong(), MAX_SCORE.toLong(), ""),
            Metric(MetricTitle.CRAVINGS, "cravings", cravings.toLong(), MAX_SCORE.toLong(), ""),
        ),
        notes = NOTES.random(random),
        isComplete = true,
        meals = listOf(
            Meal(
                id = "day-$dayNumber-breakfast",
                label = "Breakfast",
                mealDescription = BREAKFASTS.random(random),
                photoToken = null,
                lovedIt = random.nextBoolean(),
                sortOrder = 0L,
            ),
            Meal(
                id = "day-$dayNumber-lunch",
                label = "Lunch",
                mealDescription = LUNCHES.random(random),
                photoToken = null,
                lovedIt = random.nextBoolean(),
                sortOrder = 1L,
            ),
            Meal(
                id = "day-$dayNumber-dinner",
                label = "Dinner",
                mealDescription = DINNERS.random(random),
                photoToken = null,
                lovedIt = random.nextBoolean(),
                sortOrder = 2L,
            ),
        ),
        achievements = ACHIEVEMENTS.shuffled(random).take(random.nextInt(0, 3)).mapIndexed { index, text ->
            Achievement(id = "day-$dayNumber-achievement-$index", text = text, sortOrder = index.toLong())
        },
    )
}

private const val MAX_SCORE = 10

/** Scores drift upward across the program (rough first days, steadier later) with per-metric jitter. */
private fun trendingScore(dayNumber: Int, random: Random): Int {
    val progress = (dayNumber - 1).toFloat() / (SEED_DURATION_DAYS - 1)
    val baseline = 3f + progress * 5f
    val jitter = random.nextInt(-1, 2)
    return (baseline.roundToInt() + jitter).coerceIn(1, MAX_SCORE)
}

private val BREAKFASTS = listOf(
    "Three-egg scramble with spinach, mushrooms, and avocado",
    "Sweet potato hash with ground turkey and peppers",
    "Coconut milk chia pudding with berries",
    "Bacon, eggs, and sauteed kale",
    "Almond butter and banana smoothie with spinach",
)

private val LUNCHES = listOf(
    "Grilled chicken salad with olive oil vinaigrette",
    "Leftover turkey chili, no beans",
    "Tuna salad lettuce wraps with avocado",
    "Zucchini noodles with shrimp and pesto",
    "Steak salad with roasted vegetables",
)

private val DINNERS = listOf(
    "Baked salmon with asparagus and cauliflower rice",
    "Slow-cooker beef stew with root vegetables",
    "Grilled pork chops with sauteed green beans",
    "Shrimp stir-fry with coconut aminos",
    "Roast chicken with Brussels sprouts and sweet potato",
)

private val ACHIEVEMENTS = listOf(
    "No sugar cravings today",
    "Cooked a new Whole30 recipe",
    "Drank all my water",
    "Skipped the office treats",
    "Slept a full 8 hours",
    "Got a workout in",
    "Meal prepped for tomorrow",
    "Said no to wine at dinner",
    "Tried a new vegetable",
    "Made it through a stressful day without cheating",
)

private val NOTES = listOf(
    "Felt strong today, energy holding steady.",
    "Rough morning but bounced back by lunch.",
    "Cravings hit hard around 3pm but pushed through.",
    "Best day so far, no cravings at all.",
    "A bit tired, might need more sleep tonight.",
    "Really enjoyed cooking dinner tonight.",
    "Skin is looking clearer already.",
    "Missed cheese today but stayed the course.",
    "Great workout, felt fueled all day.",
    "Slow, quiet day - kept it simple with meals.",
)
