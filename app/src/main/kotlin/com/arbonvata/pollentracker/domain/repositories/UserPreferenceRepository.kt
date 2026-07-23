package com.arbonvata.pollentracker.domain.repositories

import com.arbonvata.pollentracker.domain.model.UserSettings

interface UserPreferenceRepository {
    suspend fun writeData(userSettings: UserSettings)

    suspend fun readData(): UserSettings
}
