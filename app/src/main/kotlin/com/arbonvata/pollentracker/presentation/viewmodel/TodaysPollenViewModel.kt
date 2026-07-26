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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodaysPollenViewModel
    @Inject
    constructor(
        private val repository: PollenRepository,
        private val userPreferenceRepository: UserPreferenceRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<List<PollenTrackerUiState>>(emptyList())
        val uiState: StateFlow<List<PollenTrackerUiState>> = _uiState.asStateFlow()

        init {
            loadData()
        }

        fun loadData() {
            viewModelScope.launch {
                try {
                    Log.d("TodaysPollenVM", "Starting data load")

                    // 1. Fetch reference data and user settings
                    val settings = userPreferenceRepository.readData()
                    val regionId = settings.regionId
                    val allergyIds = settings.allergyIds

                    Log.d("TodaysPollenVM", "Settings loaded: region=$regionId, allergies=$allergyIds")

                    val pollenTypesDeferred = async { repository.getAllPollenTypes() }
                    val levelDefsDeferred = async { repository.getAllPollenLevelDefinitions() }

                    val pollenTypes = pollenTypesDeferred.await()
                    val levelDefinitions = levelDefsDeferred.await()

                    Log.d("TodaysPollenVM", "Reference data loaded: ${pollenTypes.size} types, ${levelDefinitions.size} levels")

                    // 2. Fetch forecast data for selected allergens
                    val foreCastData: List<Forecast> =
                        repository.getForecastForAllergens(
                            regionId = regionId,
                            allergens = allergyIds,
                            daysIncludingToday = 3,
                        )

                    Log.d("TodaysPollenVM", "Forecast data loaded: ${foreCastData.size} forecasts")

                    // 3. Prepare lookups for efficient mapping
                    val pollenTypeMap = pollenTypes.associateBy { it.id }
                    val levelDefMap = levelDefinitions.associateBy { it.level }

                    // 4. Identify unique forecast times (days)
                    val allLevels = foreCastData.flatMap { it.levelSeries }
                    val sortedTimes = allLevels.map { it.time }.distinct().sorted()

                    Log.d("TodaysPollenVM", "Sorted times: $sortedTimes")

                    // 5. Generate UI state for each day (up to 3 days)
                    val states = mutableListOf<PollenTrackerUiState>()

                    for (dayIndex in 0..2) {
                        val dayItems =
                            if (dayIndex < sortedTimes.size) {
                                val targetTime = sortedTimes[dayIndex]

                                // Extract levels for this specific time
                                val currentLevelsMap =
                                    allLevels
                                        .filter { it.time == targetTime }
                                        .filter { it.pollenId != null }
                                        .associateBy { it.pollenId }

                                // Map selected allergens to UI items
                                allergyIds.mapNotNull { allergyId ->
                                    val pollenType = pollenTypeMap[allergyId] ?: return@mapNotNull null
                                    val levelInfo = currentLevelsMap[allergyId]

                                    val count = levelInfo?.level ?: 0
                                    val levelName = levelDefMap[count]?.name ?: "None"

                                    TodaysPollenScreenItemData(
                                        pollenCount = count,
                                        pollenLevel = levelName,
                                        pollenName = pollenType.name,
                                    )
                                }
                            } else {
                                emptyList()
                            }

                        Log.d("TodaysPollenVM", "Day $dayIndex processed with ${dayItems.size} items")

                        val dayState =
                            PollenTrackerUiState(
                                pollenTypes = pollenTypes,
                                levelDefinitions = levelDefinitions,
                                selectedRegionId = regionId,
                                selectedAllergyIds = allergyIds,
                                pollenDataByDay = listOf(dayItems),
                            )
                        states.add(dayState)
                    }

                    _uiState.update { states }
                } catch (e: Exception) {
                    Log.e("TodaysPollenVM", "Error loading data", e)
                    val errorState =
                        PollenTrackerUiState(
                            error = "Failed to load data: ${e.message}",
                            isLoading = false,
                        )
                    _uiState.update { listOf(errorState) }
                }
            }
        }
    }
