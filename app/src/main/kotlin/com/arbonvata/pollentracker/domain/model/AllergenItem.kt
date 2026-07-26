package com.arbonvata.pollentracker.domain.model

// Data class
data class AllergenItem(
    val id: String,
    val name: String,
    val hasForecast: Boolean,
    val hasPollenCounts: Boolean,
)
