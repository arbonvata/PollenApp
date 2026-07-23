package com.arbonvata.pollentracker.domain.model

data class UserSettings(
    val regionId: String,
    val allergyIds: List<String>,
)
