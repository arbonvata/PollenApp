package com.arbonvata.pollentracker.data.local

import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val REGION_KEY = stringPreferencesKey("region_key")
    val REGION_NAME_KEY = stringPreferencesKey("region_name_key")
    val ALLERGENS_LIST_ID = stringPreferencesKey("allergen_list_id")
    val ALLERGENS_LIST_NAME = stringPreferencesKey("allergen_list_name")
}
