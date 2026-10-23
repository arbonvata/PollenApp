package com.arbonvata.pollentracker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arbonvata.pollentracker.domain.model.AllergenItem
import com.arbonvata.pollentracker.domain.repositories.UserPreferenceRepository
import com.arbonvata.pollentracker.domain.usecases.GetAllergensUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllergensUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val allergens: List<AllergenItem> = emptyList(),
)

@HiltViewModel
class AllergensViewModel
    @Inject
    constructor(
        private val getAllergensUseCase: GetAllergensUseCase,
        private val userPreferenceRepository: UserPreferenceRepository,
    ) : ViewModel() {
        private val _allergensState = MutableStateFlow(AllergensUiState())
        val allergensState: StateFlow<AllergensUiState> = _allergensState.asStateFlow()

        private val _selectedAllergenIds = MutableStateFlow<Set<String>>(emptySet())
        val selectedAllergenIds = _selectedAllergenIds.asStateFlow()

        init {
            loadData()
        }

        fun loadData() {
            viewModelScope.launch {
                _allergensState.update { it.copy(isLoading = true, isError = false) }

                try {
                    val allergens =
                        getAllergensUseCase()
                            .fold(
                                onSuccess = { allergens -> allergens.filter { it.hasForecast } },
                                onFailure = { e ->
                                    Log.e("AllergensVM", "Error loading allergens", e)
                                    _allergensState.update { it.copy(isLoading = false, isError = true) }
                                    return@launch
                                },
                            )

                    _allergensState.update { it.copy(isLoading = false, allergens = allergens) }

                    val settings = userPreferenceRepository.readData()
                    _selectedAllergenIds.update { settings.allergyIds.toSet() }
                } catch (e: CancellationException) {
                    // the scope is being cleared - let the coroutine end without an error state
                    throw e
                } catch (e: Exception) {
                    Log.e("AllergensVM", "Error loading settings", e)
                    _allergensState.update { it.copy(isLoading = false, isError = true) }
                }
            }
        }

        fun onAllergenToggle(id: String) {
            _selectedAllergenIds.update { current ->
                if (current.contains(id)) {
                    current - id
                } else {
                    current + id
                }
            }
        }

        fun saveAllergySelection(onSuccess: () -> Unit) {
            viewModelScope.launch {
                val currentSettings = userPreferenceRepository.readData()

                val selectedIds = _selectedAllergenIds.value
                val selectedNames =
                    _allergensState.value.allergens
                        .filter { it.id in selectedIds }
                        .map { it.name }

                val updatedSettings =
                    currentSettings.copy(
                        allergyIds = selectedIds.toList(),
                        allergyNames = selectedNames,
                    )

                userPreferenceRepository.writeData(updatedSettings)
                onSuccess()
            }
        }
    }
