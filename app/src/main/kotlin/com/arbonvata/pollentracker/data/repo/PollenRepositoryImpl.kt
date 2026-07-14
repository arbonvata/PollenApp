package com.arbonvata.pollentracker.data.repo

import com.arbonvata.pollentracker.data.model.Forecast
import com.arbonvata.pollentracker.data.model.PaginatedResponse
import com.arbonvata.pollentracker.data.model.PollenCount
import com.arbonvata.pollentracker.data.model.PollenLevelDefinition
import com.arbonvata.pollentracker.data.model.PollenType
import com.arbonvata.pollentracker.data.model.Region
import com.arbonvata.pollentracker.data.network.PollenApiService
import com.arbonvata.pollentracker.domain.PollenRepository
import javax.inject.Inject

class PollenRepositoryImpl @Inject constructor(
    private val apiService: PollenApiService,
) : PollenRepository {
    override suspend fun getRegions(
        offset: Int,
        limit: Int,
    ): PaginatedResponse<Region> = apiService.getRegions(offset, limit)

    override suspend fun getAllRegions(pageSize: Int): List<Region> {
        val allitems = mutableListOf<Region>()
        var offset = 0
        do {
            val data = apiService.getRegions(offset, pageSize)
            allitems.addAll(data.items)
            if (data.meta.offset + data.meta.count >= data.meta.totalRecords) {
                break
            }
            offset += data.meta.limit
        } while (true)

        return allitems
    }

    override suspend fun getRegionById(regionId: String): Region? = apiService.getRegions().items.find { it.id == regionId }

    override suspend fun getRegionByName(name: String): Region? = apiService.getRegions().items.find { it.name == name }

    override suspend fun getPollenTypes(
        offset: Int,
        limit: Int,
    ): PaginatedResponse<PollenType> = apiService.getPollenTypes(offset, limit)

    override suspend fun getAllPollenTypes(pageSize: Int): List<PollenType> {
        val allItems = mutableListOf<PollenType>()
        var offset = 0

        do {
            val data = apiService.getPollenTypes(offset = offset, limit = pageSize)
            allItems.addAll(data.items)

            if (data.meta.offset + data.meta.count >= data.meta.totalRecords) {
                break
            }
            offset += data.meta.limit
        } while (true)

        return allItems
    }

    override suspend fun getPollenTypeById(pollenId: String): PollenType? = apiService.getPollenTypes().items.find { it.id == pollenId }

    // ============ POLLEN LEVEL DEFINITIONS ============
    override suspend fun getPollenLevelDefinitions(
        offset: Int,
        limit: Int,
    ): PaginatedResponse<PollenLevelDefinition> = apiService.getPollenLevelDefinitions(offset, limit)

    override suspend fun getAllPollenLevelDefinitions(pageSize: Int): List<PollenLevelDefinition> {
        val allItems = mutableListOf<PollenLevelDefinition>()
        var offset = 0

        do {
            val data = apiService.getPollenLevelDefinitions(offset = offset, limit = pageSize)
            allItems.addAll(data.items)

            if (data.meta.offset + data.meta.count >= data.meta.totalRecords) {
                break
            }
            offset += data.meta.limit
        } while (true)

        return allItems
    }

    override suspend fun getLevelDefinitionByValue(levelValue: Int): PollenLevelDefinition? =
        apiService.getPollenLevelDefinitions().items.find {
            it.level == levelValue
        }

    // ============ FORECASTS ============
    override suspend fun getForecasts(
        regionId: String?,
        pollenId: String?,
        current: Boolean?,
        startDate: String?,
        endDate: String?,
        offset: Int,
        limit: Int,
    ): PaginatedResponse<Forecast> =
        apiService.getForecasts(
            regionId = regionId,
            pollenId = pollenId,
            current = current,
            startDate = startDate,
            endDate = endDate,
            offset = offset,
            limit = limit,
        )

    override suspend fun getLatestForecast(
        regionId: String?,
        pollenId: String?,
    ): Forecast? {
        val data =
            apiService.getForecasts(
                regionId = regionId,
                pollenId = pollenId,
                current = true,
            )
        return data.items.firstOrNull()
    }

    override suspend fun getLatestForecastOrThrow(
        regionId: String?,
        pollenId: String?,
    ): Forecast {
        val data =
            apiService.getForecasts(
                regionId = regionId,
                pollenId = pollenId,
                current = true,
            )
        return data.items.firstOrNull()
            ?: throw NoSuchElementException("No forecast found for region: $regionId, pollen: $pollenId")
    }

    override suspend fun getForecastsForDateRange(
        startDate: String,
        endDate: String,
        regionId: String?,
        pollenId: String?,
        offset: Int,
        limit: Int,
    ): PaginatedResponse<Forecast> =
        apiService.getForecasts(
            regionId = regionId,
            pollenId = pollenId,
            startDate = startDate,
            endDate = endDate,
            offset = offset,
            limit = limit,
            current = true,
        )

    override suspend fun getAllForecasts(
        regionId: String?,
        pollenId: String?,
        startDate: String?,
        endDate: String?,
        pageSize: Int,
    ): List<Forecast> {
        val allItems = mutableListOf<Forecast>()
        var offset = 0

        do {
            val data =
                apiService.getForecasts(
                    regionId = regionId,
                    pollenId = pollenId,
                    startDate = startDate,
                    endDate = endDate,
                    offset = offset,
                    limit = pageSize,
                )
            allItems.addAll(data.items)

            if (data.meta.offset + data.meta.count >= data.meta.totalRecords) {
                break
            }
            offset += data.meta.limit
        } while (true)

        return allItems
    }

    override suspend fun getPollenCounts(
        regionId: String?,
        pollenId: String?,
        hasTechnicalError: Boolean?,
        startDate: String?,
        endDate: String?,
        offset: Int,
        limit: Int,
    ): PaginatedResponse<PollenCount> =
        apiService.getPollenCount(
            regionId = regionId,
            pollenId = pollenId,
            hasTechnicalError = hasTechnicalError,
            startDate = startDate,
            endDate = endDate,
            offset = offset,
            limit = limit,
        )

    override suspend fun getPollenCountsWithoutErrors(
        regionId: String?,
        pollenId: String?,
        startDate: String?,
        endDate: String?,
        offset: Int,
        limit: Int,
    ): PaginatedResponse<PollenCount> =
        apiService.getPollenCount(
            regionId = regionId,
            pollenId = pollenId,
            hasTechnicalError = false,
            startDate = startDate,
            endDate = endDate,
            offset = offset,
            limit = limit,
        )

    override suspend fun getAllPollenCounts(
        regionId: String?,
        pollenId: String?,
        startDate: String?,
        endDate: String?,
        excludeTechnicalErrors: Boolean,
        pageSize: Int,
    ): List<PollenCount> {
        val allItems = mutableListOf<PollenCount>()
        var offset = 0

        do {
            val data =
                apiService.getPollenCount(
                    regionId = regionId,
                    pollenId = pollenId,
                    hasTechnicalError = if (excludeTechnicalErrors) false else null,
                    startDate = startDate,
                    endDate = endDate,
                    offset = offset,
                    limit = pageSize,
                )
            allItems.addAll(data.items)

            if (data.meta.offset + data.meta.count >= data.meta.totalRecords) {
                break
            }
            offset += data.meta.limit
        } while (true)

        return allItems
    }
}
