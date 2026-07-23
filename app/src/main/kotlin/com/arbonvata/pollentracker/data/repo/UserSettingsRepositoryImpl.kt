package com.arbonvata.pollentracker.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.arbonvata.pollentracker.data.local.PreferenceKeys
import com.arbonvata.pollentracker.domain.model.UserSettings
import com.arbonvata.pollentracker.domain.repositories.UserPreferenceRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
        private val json: Json,
    ) : UserPreferenceRepository {
        override suspend fun writeData(userSettings: UserSettings) {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.REGION_KEY] = userSettings.regionId
                preferences[PreferenceKeys.ALLERGENS_LIST] = json.encodeToString(userSettings.allergyIds)
            }
        }

        override suspend fun readData(): UserSettings =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }.map { preferences ->
                    val regionId = preferences[PreferenceKeys.REGION_KEY] ?: ""
                    val allergiesJson = preferences[PreferenceKeys.ALLERGENS_LIST]
                    val allergyIds =
                        if (allergiesJson != null) {
                            try {
                                json.decodeFromString<List<String>>(allergiesJson)
                            } catch (e: Exception) {
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    UserSettings(regionId = regionId, allergyIds = allergyIds)
                }.first()
    }
