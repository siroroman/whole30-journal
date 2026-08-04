package dev.whole30journal.feature.dayentry.domain.model

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

data class Meal(
    val id: String,
    val label: String,
    // "description" collides with KotlinBase's own description() in the generated iOS API.
    @OptIn(ExperimentalObjCName::class)
    @ObjCName("mealDescription")
    val description: String,
    val photoToken: String?,
    val lovedIt: Boolean,
    val sortOrder: Long,
)
