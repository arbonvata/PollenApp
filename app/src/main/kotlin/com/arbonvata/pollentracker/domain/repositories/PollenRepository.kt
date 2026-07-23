package com.arbonvata.pollentracker.domain.repositories

import com.arbonvata.pollentracker.domain.model.Forecast
import com.arbonvata.pollentracker.domain.model.PaginatedResponse
import com.arbonvata.pollentracker.domain.model.PollenCount
import com.arbonvata.pollentracker.domain.model.PollenLevelDefinition
import com.arbonvata.pollentracker.domain.model.PollenType
import com.arbonvata.pollentracker.domain.model.Region

interface PollenRepository {
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

    suspend fun getPollenLevelDefinitions(
        offset: Int = 0,
        limit: Int = 100,
    ): PaginatedResponse<PollenLevelDefinition>

    suspend fun getAllPollenLevelDefinitions(pageSize: Int = 100): List<PollenLevelDefinition>

    suspend fun getLevelDefinitionByValue(levelValue: Int): PollenLevelDefinition?

    // ============ FORECASTS ============

    suspend fun getForecasts(
        regionId: String? = null,
        pollenId: String? = null,
        current: Boolean? = null,
        startDate: String? = null,
        endDate: String? = null,
        offset: Int = 0,
        limit: Int = 100,
    ): PaginatedResponse<Forecast>

    suspend fun getLatestForecast(
        regionId: String? = null,
        pollenId: String? = null,
    ): Forecast?

    suspend fun getLatestForecastOrThrow(
        regionId: String? = null,
        pollenId: String? = null,
    ): Forecast

    suspend fun getForecastsForDateRange(
        startDate: String,
        endDate: String,
        regionId: String? = null,
        pollenId: String? = null,
        offset: Int = 0,
        limit: Int = 100,
    ): PaginatedResponse<Forecast>

    suspend fun getAllForecasts(
        regionId: String? = null,
        pollenId: String? = null,
        current: Boolean? = null,
        startDate: String? = null,
        endDate: String? = null,
        pageSize: Int = 100,
    ): List<Forecast>

    // ============ POLLEN COUNTS ============

    suspend fun getPollenCounts(
        regionId: String? = null,
        pollenId: String? = null,
        hasTechnicalError: Boolean? = null,
        startDate: String? = null,
        endDate: String? = null,
        offset: Int = 0,
        limit: Int = 100,
    ): PaginatedResponse<PollenCount>

    suspend fun getPollenCountsWithoutErrors(
        regionId: String? = null,
        pollenId: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        offset: Int = 0,
        limit: Int = 100,
    ): PaginatedResponse<PollenCount>

    suspend fun getAllPollenCounts(
        regionId: String? = null,
        pollenId: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        excludeTechnicalErrors: Boolean = true,
        pageSize: Int = 100,
    ): List<PollenCount>
}
