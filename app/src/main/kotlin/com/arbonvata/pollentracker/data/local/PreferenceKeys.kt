package com.arbonvata.pollentracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// 1. Create DataStore instance via extension property (delegate)
private val Context.dataStore by preferencesDataStore(name = "user_settings")

object PreferenceKeys {
    val REGION_KEY = stringPreferencesKey("region_key")
    val ALLERGENS_LIST = stringPreferencesKey("allergene_list")
}
