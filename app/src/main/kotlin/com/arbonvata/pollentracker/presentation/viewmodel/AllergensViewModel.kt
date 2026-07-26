package com.arbonvata.pollentracker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arbonvata.pollentracker.domain.model.AllergenItem
import com.arbonvata.pollentracker.domain.repositories.UserPreferenceRepository
import com.arbonvata.pollentracker.domain.usecases.GetAllergensUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllergensViewModel
    @Inject
    constructor(
        private val getAllergensUseCase: GetAllergensUseCase,
        private val userPreferenceRepository: UserPreferenceRepository,
    ) : ViewModel() {
        private val _allergensState = MutableStateFlow<List<AllergenItem>>(emptyList())
        val allergensState = _allergensState.asStateFlow()

        private val _selectedAllergenIds = MutableStateFlow<Set<String>>(emptySet())
        val selectedAllergenIds = _selectedAllergenIds.asStateFlow()

        init {
            loadData()
        }

        fun loadData() {
            viewModelScope.launch {
                val allergens = getAllergensUseCase()
                Log.d("ArbonVata", allergens.toString())
                _allergensState.update { allergens }

                val settings = userPreferenceRepository.readData()
                _selectedAllergenIds.update { settings.allergyIds.toSet() }
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
                    _allergensState.value
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
