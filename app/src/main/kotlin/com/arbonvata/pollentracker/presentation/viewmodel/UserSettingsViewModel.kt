package com.arbonvata.pollentracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arbonvata.pollentracker.domain.model.Region
import com.arbonvata.pollentracker.domain.model.UserSettings
import com.arbonvata.pollentracker.domain.repositories.UserPreferenceRepository
import com.arbonvata.pollentracker.domain.usecases.GetRegionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class UserSettingsViewModel
    @Inject
    constructor(
        private val getRegionsUseCase: GetRegionsUseCase,
        private val repository: UserPreferenceRepository,
    ) : ViewModel() {
        private val _userSettingsState = MutableStateFlow<UserSettings?>(null)
        val userSettingsState = _userSettingsState.asStateFlow()
        private val _regionsState = MutableStateFlow<List<Region>>(emptyList())
        val regionsState = _regionsState.asStateFlow()

        init {
            loadInitData()
        }

        fun loadInitData() {
            viewModelScope.launch {
                val settings = repository.readData()
                _userSettingsState.update { settings }

                val regionsResult = getRegionsUseCase.invoke()
                _regionsState.update {
                    regionsResult.getOrDefault(emptyList())
                }
            }
        }

        fun writeData(userSettings: UserSettings) {
            viewModelScope.launch {
                repository.writeData(userSettings)
            }
        }
    }
