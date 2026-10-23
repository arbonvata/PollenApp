package com.arbonvata.pollentracker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arbonvata.pollentracker.domain.model.Region
import com.arbonvata.pollentracker.domain.model.UserSettings
import com.arbonvata.pollentracker.domain.repositories.UserPreferenceRepository
import com.arbonvata.pollentracker.domain.usecases.GetRegionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegionsUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val regions: List<Region> = emptyList(),
)

@HiltViewModel
class UserSettingsViewModel
    @Inject
    constructor(
        private val getRegionsUseCase: GetRegionsUseCase,
        private val repository: UserPreferenceRepository,
    ) : ViewModel() {
        private val _userSettingsState = MutableStateFlow<UserSettings?>(null)
        val userSettingsState = _userSettingsState.asStateFlow()
        private val _regionsState = MutableStateFlow(RegionsUiState())
        val regionsState: StateFlow<RegionsUiState> = _regionsState.asStateFlow()

        init {
            loadInitData()
        }

        fun loadInitData() {
            viewModelScope.launch {
                try {
                    val settings = repository.readData()
                    _userSettingsState.update { settings }

                    _regionsState.update { it.copy(isLoading = true, isError = false) }
                    val regionsResult = getRegionsUseCase.invoke()

                    _regionsState.update { state ->
                        regionsResult.fold(
                            onSuccess = { regions -> state.copy(isLoading = false, regions = regions) },
                            onFailure = { e ->
                                Log.e("UserSettingsVM", "Error loading regions", e)
                                state.copy(isLoading = false, isError = true)
                            },
                        )
                    }
                } catch (e: CancellationException) {
                    // the scope is being cleared - let the coroutine end without an error state
                    throw e
                } catch (e: Exception) {
                    Log.e("UserSettingsVM", "Error loading settings", e)
                    _regionsState.update { it.copy(isLoading = false, isError = true) }
                }
            }
        }

        fun writeData(userSettings: UserSettings) {
            viewModelScope.launch {
                repository.writeData(userSettings)
            }
        }
    }
