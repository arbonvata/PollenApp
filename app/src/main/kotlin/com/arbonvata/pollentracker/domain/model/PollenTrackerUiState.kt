package com.arbonvata.pollentracker.domain.model

import com.arbonvata.pollentracker.presentation.compose.TodaysPollenScreenItemData

data class PollenTrackerUiState(
    val regions: List<Region> = emptyList(),
    val pollenTypes: List<PollenType> = emptyList(),
    val levelDefinitions: List<PollenLevelDefinition> = emptyList(),
    val forecasts: List<Forecast> = emptyList(),
    val pollenCounts: List<PollenCount> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedRegionId: String? = null,
    val selectedPollenId: String? = null,
    val selectedAllergyIds: List<String> = emptyList(),
    val pollenDataByDay: List<List<TodaysPollenScreenItemData>> = emptyList(),
)
