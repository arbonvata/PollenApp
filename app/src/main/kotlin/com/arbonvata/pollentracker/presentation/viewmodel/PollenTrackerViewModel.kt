package com.arbonvata.pollentracker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arbonvata.pollentracker.domain.model.Forecast
import com.arbonvata.pollentracker.domain.model.PollenTrackerUiState
import com.arbonvata.pollentracker.domain.repositories.PollenRepository
import com.arbonvata.pollentracker.domain.repositories.UserPreferenceRepository
import com.arbonvata.pollentracker.presentation.compose.TodaysPollenScreenItemData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PollenTrackerViewModel
    @Inject
    constructor(
        private val repository: PollenRepository,
        private val userPreferenceRepository: UserPreferenceRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PollenTrackerUiState())
        val uiState: StateFlow<PollenTrackerUiState> = _uiState.asStateFlow()

        init {
            loadInitialData()
        }

        // load initial data
        fun loadInitialData() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                try {
                    // Load reference data in parallel
                    val regionsDeferred = async { repository.getAllRegions() }
                    val pollenTypesDeferred = async { repository.getAllPollenTypes() }
                    val levelDefsDeferred = async { repository.getAllPollenLevelDefinitions() }
                    val settingsDeferred = async { userPreferenceRepository.readData() }

                    val regions = regionsDeferred.await()
                    val pollenTypes = pollenTypesDeferred.await()
                    val levelDefinitions = levelDefsDeferred.await()
                    val settings = settingsDeferred.await()

                    _uiState.update {
                        it.copy(
                            regions = regions,
                            pollenTypes = pollenTypes,
                            levelDefinitions = levelDefinitions,
                            selectedRegionId = settings.regionId,
                            selectedAllergyIds = settings.allergyIds,
                            isLoading = false,
                        )
                    }

                    // Once we have a region, load forecasts
                    if (settings.regionId.isNotEmpty()) {
                        loadForecasts()
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Failed to load data",
                            isLoading = false,
                        )
                    }
                }
            }
        }

        fun onRegionSelected(regionId: String?) {
            _uiState.update { it.copy(selectedRegionId = regionId) }
            loadForecasts()
        }

        fun onPollenTypeSelected(pollenId: String?) {
            _uiState.update { it.copy(selectedPollenId = pollenId) }
            loadForecasts()
        }

        private fun loadForecasts() {
            val state = _uiState.value
            Log.d(
                "PollenTrackerVM",
                "Loading forecasts for region: ${state.selectedRegionId}, selectedAllergies: ${state.selectedAllergyIds}",
            )
            if (state.selectedRegionId == null) return

            viewModelScope.launch {
                try {
                    val forecasts =
                        repository.getAllForecasts(
                            regionId = state.selectedRegionId,
                            pollenId = state.selectedPollenId,
                            current = null, // Changed from true to null to get future days
                        )

                    Log.d("PollenTrackerVM", "Fetched ${forecasts.size} forecasts")
                    val processedData = processForecastData(forecasts, state)

                    _uiState.update {
                        it.copy(
                            forecasts = forecasts,
                            pollenDataByDay = processedData,
                        )
                    }
                } catch (e: Exception) {
                    Log.e("PollenTrackerVM", "Failed to load forecasts", e)
                    _uiState.update { it.copy(error = "Failed to load forecasts") }
                }
            }
        }

        private fun processForecastData(
            forecasts: List<Forecast>,
            state: PollenTrackerUiState,
        ): List<List<TodaysPollenScreenItemData>> {
            val pollenTypeMap = state.pollenTypes.associateBy { it.id }
            val levelDefMap = state.levelDefinitions.associateBy { it.level }

            // Get unique times and sort them
            val allLevels = forecasts.flatMap { it.levelSeries }
            val sortedTimes = allLevels.map { it.time }.distinct().sorted()

            Log.d("PollenTrackerVM", "Sorted times found: $sortedTimes")

            // Take up to 3 days
            return (0..2).map { dayIndex ->
                if (dayIndex < sortedTimes.size) {
                    val targetTime = sortedTimes[dayIndex]
                    val currentLevelsMap =
                        allLevels
                            .filter { it.time == targetTime }
                            .filter { it.pollenId != null }
                            .associateBy { it.pollenId }

                    val items =
                        state.selectedAllergyIds.mapNotNull { allergyId ->
                            val pollenType = pollenTypeMap[allergyId] ?: return@mapNotNull null
                            val level = currentLevelsMap[allergyId]

                            TodaysPollenScreenItemData(
                                pollenCount = level?.level ?: 0,
                                pollenLevel = levelDefMap[level?.level ?: 0]?.name ?: "None",
                                pollenName = pollenType.name,
                            )
                        }
                    Log.d("PollenTrackerVM", "Day $dayIndex (time $targetTime) has ${items.size} items")
                    items
                } else {
                    Log.d("PollenTrackerVM", "Day $dayIndex has no data (not enough sorted times)")
                    emptyList()
                }
            }
        }
    }
