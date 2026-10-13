package com.arbonvata.pollentracker.domain.model

import java.time.LocalDate

data class AllergenHistory(
    val pollenId: String,
    val pollenName: String?,
    val levels: List<DailyPollenLevel>,
)

data class DailyPollenLevel(
    val date: LocalDate,
    val dailyCount: Int?,
    val levelDescription: String?,
    val level: Int?,
)
