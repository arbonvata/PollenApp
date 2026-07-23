package com.arbonvata.pollentracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arbonvata.pollentracker.domain.model.Forecast
import com.arbonvata.pollentracker.domain.model.PollenCount
import com.arbonvata.pollentracker.domain.model.PollenLevelDefinition
import com.arbonvata.pollentracker.domain.model.PollenType
import com.arbonvata.pollentracker.domain.model.Region
import com.arbonvata.pollentracker.domain.repositories.PollenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
)

@HiltViewModel
class PollenTrackerViewModel
    @Inject
    constructor(
        private val repository: PollenRepository,
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

                    val regions = regionsDeferred.await()
                    val pollenTypes = pollenTypesDeferred.await()
                    val levelDefinitions = levelDefsDeferred.await()

                    _uiState.update {
                        it.copy(
                            regions = regions,
                            pollenTypes = pollenTypes,
                            levelDefinitions = levelDefinitions,
                            isLoading = false,
                        )
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
            // Only load if a region is selected
            if (state.selectedRegionId == null) return

            viewModelScope.launch {
                try {
                    val forecasts =
                        repository.getAllForecasts(
                            regionId = state.selectedRegionId,
                            pollenId = state.selectedPollenId,
                            current = true,
                        )
                    _uiState.update { it.copy(forecasts = forecasts) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Failed to load forecasts") }
                }
            }
        }

        fun saveAllergySelection() {
            TODO("Not yet implemented")
        }
    }
