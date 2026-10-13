package com.arbonvata.pollentracker.domain.repositories

import com.arbonvata.pollentracker.domain.model.AllergenHistory
import com.arbonvata.pollentracker.domain.model.AllergenItem
import com.arbonvata.pollentracker.domain.model.Forecast
import com.arbonvata.pollentracker.domain.model.PaginatedResponse
import com.arbonvata.pollentracker.domain.model.PollenLevelDefinition
import com.arbonvata.pollentracker.domain.model.PollenType
import com.arbonvata.pollentracker.domain.model.Region

@Suppress("TooManyFunctions")
interface PollenRepository {
    /**
     * @param regionId required - e.g. id for Lund/Malmö/Kristianstad from /v1/regions
     * @param allergens list of names or UUIDs e.g. listOf("Björk", "Gräs", "Gråbo")
     * @param daysIncludingToday 1 = today only, 3 = today + next 2 days
     */
    suspend fun getForecastForAllergens(
        regionId: String,
        allergens: List<String>,
        daysIncludingToday: Int,
    ): List<Forecast>

    /**
     * Measured daily levels per allergen from day 1 of the current year until today
     * (Europe/Stockholm). Days without measurements are simply absent from each list.
     *
     * @param regionId required - e.g. id for Stockholm from /v1/regions
     * @param allergens list of names or UUIDs e.g. listOf("Björk", "Gräs", "Gråbo"); empty = all pollen types
     */
    suspend fun getYearlyHistoryForAllergens(
        regionId: String,
        allergens: List<String> = emptyList(),
    ): List<AllergenHistory>

    suspend fun getRegions(
        offset: Int = 0,
        limit: Int = 100,
    ): PaginatedResponse<Region>

    suspend fun getAllRegions(pageSize: Int = 100): List<Region>

    suspend fun getRegionById(regionId: String): Region?

    suspend fun getRegionByName(name: String): Region?

    // ============ POLLEN TYPES ============
    suspend fun getPollenTypes(
        offset: Int = 0,
        limit: Int = 100,
    ): PaginatedResponse<PollenType>

    suspend fun getAllPollenTypes(pageSize: Int = 100): List<PollenType>

    suspend fun getPollenTypeById(pollenId: String): PollenType?

    // ============ POLLEN LEVEL DEFINITIONS ============

    suspend fun getAllPollenLevelDefinitions(pageSize: Int = 100): List<PollenLevelDefinition>

    suspend fun getLevelDefinitionByValue(levelValue: Int): PollenLevelDefinition?

    suspend fun getAllergensList(): List<AllergenItem>
}
