package com.arbonvata.pollentracker.domain.usecases

import com.arbonvata.pollentracker.domain.model.UserSettings

interface GetSettingsUseCase {
    suspend fun invoke(): Result<UserSettings>
}
