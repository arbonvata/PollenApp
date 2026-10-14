package com.arbonvata.pollentracker.domain.model

data class UserSettings(
    val regionId: String = "",
    val allergyIds: List<String> = emptyList(),
    val allergyNames: List<String> = emptyList(),
) {
    /** Both the favorite place and at least one allergen have been chosen. */
    val isSetupComplete: Boolean
        get() = regionId.isNotBlank() && allergyIds.isNotEmpty()
}
