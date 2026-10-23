package com.arbonvata.pollentracker.domain.model

data class TodaysPollenScreenItemData(
    val pollenCount: Int,
    val pollenLevel: String?,
    val pollenName: String,
)

data class PollenTrackerUiState(
    val regions: List<Region> = emptyList(),
    val pollenTypes: List<PollenType> = emptyList(),
    val levelDefinitions: List<PollenLevelDefinition> = emptyList(),
    val forecasts: List<Forecast> = emptyList(),
    val pollenCounts: List<PollenCount> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val selectedRegionId: String? = null,
    val selectedPollenId: String? = null,
    val selectedAllergyIds: List<String> = emptyList(),
    val pollenDataByDay: List<List<TodaysPollenScreenItemData>> = emptyList(),
)
