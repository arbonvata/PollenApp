package com.arbonvata.pollentracker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arbonvata.pollentracker.domain.model.AllergenHistory
import com.arbonvata.pollentracker.domain.repositories.UserPreferenceRepository
import com.arbonvata.pollentracker.domain.usecases.GetYearlyHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class HistoryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val allergenHistory: List<AllergenHistory> = emptyList(),
)

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val getYearlyHistory: GetYearlyHistoryUseCase,
        private val userPreferenceRepository: UserPreferenceRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HistoryUiState())
        val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

        init {
            loadData()
        }

        fun loadData() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                try {
                    val settings = userPreferenceRepository.readData()

                    Log.d(
                        "HistoryVM",
                        "Settings loaded: region=${settings.regionId}, allergies=${settings.allergyIds}",
                    )

                    getYearlyHistory(
                        regionId = settings.regionId,
                        allergens = settings.allergyIds,
                    ).fold(
                        onSuccess = { history ->
                            Log.d("HistoryVM", "Loaded history for ${history.size} allergens")
                            _uiState.update { state ->
                                state.copy(isLoading = false, allergenHistory = history)
                            }
                        },
                        onFailure = { e ->
                            Log.e("HistoryVM", "Error loading history", e)
                            _uiState.update { state ->
                                state.copy(isLoading = false, error = "Failed to load history: ${e.message}")
                            }
                        },
                    )
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("HistoryVM", "Error loading settings", e)
                    _uiState.update { state ->
                        state.copy(isLoading = false, error = "Failed to load history: ${e.message}")
                    }
                }
            }
        }
    }
