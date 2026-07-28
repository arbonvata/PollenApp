package com.arbonvata.pollentracker.data.repo

import android.util.Log
import com.arbonvata.pollentracker.data.network.PollenApiService
import com.arbonvata.pollentracker.data.network.mapper.PollenRemoteToLocalMapper
import com.arbonvata.pollentracker.data.network.mapper.toDomain
import com.arbonvata.pollentracker.domain.model.AllergenItem
import com.arbonvata.pollentracker.domain.model.Forecast
import com.arbonvata.pollentracker.domain.model.PaginatedResponse
import com.arbonvata.pollentracker.domain.model.PollenCount
import com.arbonvata.pollentracker.domain.model.PollenLevelDefinition
import com.arbonvata.pollentracker.domain.model.PollenType
import com.arbonvata.pollentracker.domain.model.Region
import com.arbonvata.pollentracker.domain.repositories.PollenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class PollenRepositoryImpl
    @Inject
    constructor(
        private val apiService: PollenApiService,
        private val mapper: PollenRemoteToLocalMapper,
    ) : PollenRepository {
        private val zone = ZoneId.of("Europe/Stockholm")
        private val df = DateTimeFormatter.ISO_LOCAL_DATE

        /**
         * @param regionId required - e.g. id for Lund/Malmö/Kristianstad from /v1/regions
         * @param allergens list of names or UUIDs e.g. listOf("Björk", "Gräs", "Gråbo")
         * @param daysIncludingToday 1 = today only, 3 = today + next 2 days
         */
        override suspend fun getForecastForAllergens(
            regionId: String,
            allergens: List<String>,
            daysIncludingToday: Int,
        ): List<Forecast> {
            require(regionId.isNotBlank()) { "regionId is required" }
            require(daysIncludingToday >= 1)

            val today = LocalDate.now(zone)
            val end = today.plusDays((daysIncludingToday - 1).toLong())

            val allTypes = apiService.getPollenTypes().items
            val lookup =
                allTypes.associate { it.name.lowercase(Locale.ROOT) to it.id } +
                    allTypes.associate { it.id?.lowercase(Locale.ROOT) to it.id }
            val targetPollenIds = allergens.mapNotNull { lookup[it.lowercase(Locale.ROOT)] }.toSet()

            if (targetPollenIds.isEmpty()) {
                Log.w("PollenRepo", "No matching pollen IDs for allergens: $allergens")
                return emptyList()
            }

            Log.d("PollenRepo", "Fetching current forecast for region=$regionId")
            val response =
                apiService.getForecasts(
                    regionId = regionId,
                    current = true,
                    // no start/end date — avoids the strict containment filter entirely
                )

            Log.d("PollenRepo", "API returned ${response.items.size} total items for region $regionId")

            return response.items
                .filter { item -> item.levelSeries.any { it.pollenId in targetPollenIds } }
                .map { mapper.mapForecast(it) }
                .also { Log.d("PollenRepo", "Returning ${it.size} filtered forecast items") }
        }

        override suspend fun getRegions(
            offset: Int,
            limit: Int,
        ): PaginatedResponse<Region> = apiService.getRegions(offset, limit).toDomain { mapper.mapRegion(it) }

        override suspend fun getAllRegions(pageSize: Int): List<Region> {
            val allItems = mutableListOf<Region>()
            var offset = 0
            do {
                val remoteData = apiService.getRegions(offset, pageSize)
                val domainData = remoteData.toDomain { mapper.mapRegion(it) }
                allItems.addAll(domainData.items)
                if (domainData.meta.offset + domainData.meta.count >= domainData.meta.totalRecords) {
                    break
                }
                offset += domainData.meta.limit
            } while (true)

            return allItems
        }

        override suspend fun getRegionById(regionId: String): Region? =
            apiService
                .getRegions()
                .items
                .find { it.id == regionId }
                ?.let { mapper.mapRegion(it) }

        override suspend fun getRegionByName(name: String): Region? =
            apiService
                .getRegions()
                .items
                .find { it.name == name }
                ?.let { mapper.mapRegion(it) }

        override suspend fun getPollenTypes(
            offset: Int,
            limit: Int,
        ): PaginatedResponse<PollenType> = apiService.getPollenTypes(offset, limit).toDomain { mapper.mapPollenType(it) }

        override suspend fun getAllPollenTypes(pageSize: Int): List<PollenType> {
            val allItems = mutableListOf<PollenType>()
            var offset = 0

            do {
                val remoteData = apiService.getPollenTypes(offset = offset, limit = pageSize)
                val domainData = remoteData.toDomain { mapper.mapPollenType(it) }
                allItems.addAll(domainData.items)

                if (domainData.meta.offset + domainData.meta.count >= domainData.meta.totalRecords) {
                    break
                }
                offset += domainData.meta.limit
            } while (true)

            return allItems
        }

        override suspend fun getPollenTypeById(pollenId: String): PollenType? =
            apiService
                .getPollenTypes()
                .items
                .find { it.id == pollenId }
                ?.let { mapper.mapPollenType(it) }

        override suspend fun getPollenLevelDefinitions(
            offset: Int,
            limit: Int,
        ): PaginatedResponse<PollenLevelDefinition> =
            apiService.getPollenLevelDefinitions(offset, limit).toDomain { mapper.mapPollenLevelDefinition(it) }

        override suspend fun getAllPollenLevelDefinitions(pageSize: Int): List<PollenLevelDefinition> {
            val allItems = mutableListOf<PollenLevelDefinition>()
            var offset = 0

            do {
                val remoteData = apiService.getPollenLevelDefinitions(offset = offset, limit = pageSize)
                val domainData = remoteData.toDomain { mapper.mapPollenLevelDefinition(it) }
                allItems.addAll(domainData.items)

                if (domainData.meta.offset + domainData.meta.count >= domainData.meta.totalRecords) {
                    break
                }
                offset += domainData.meta.limit
            } while (true)

            return allItems
        }

        override suspend fun getLevelDefinitionByValue(levelValue: Int): PollenLevelDefinition? =
            apiService
                .getPollenLevelDefinitions()
                .items
                .find { it.level == levelValue }
                ?.let { mapper.mapPollenLevelDefinition(it) }

        override suspend fun getForecasts(
            regionId: String?,
            pollenId: String?,
            current: Boolean?,
            startDate: String?,
            endDate: String?,
            offset: Int,
            limit: Int,
        ): PaginatedResponse<Forecast> =
            apiService
                .getForecasts(
                    regionId = regionId,
                    pollenId = pollenId,
                    current = current,
                    startDate = startDate,
                    endDate = endDate,
                    offset = offset,
                    limit = limit,
                ).toDomain { mapper.mapForecast(it) }

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
            return data.items.firstOrNull()?.let { mapper.mapForecast(it) }
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
            return data.items.firstOrNull()?.let { mapper.mapForecast(it) }
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
            apiService
                .getForecasts(
                    regionId = regionId,
                    pollenId = pollenId,
                    startDate = startDate,
                    endDate = endDate,
                    offset = offset,
                    limit = limit,
                    current = true,
                ).toDomain { mapper.mapForecast(it) }

        override suspend fun getAllForecasts(
            regionId: String?,
            pollenId: String?,
            current: Boolean?,
            startDate: String?,
            endDate: String?,
            pageSize: Int,
        ): List<Forecast> {
            val allItems = mutableListOf<Forecast>()
            var offset = 0

            do {
                val remoteData =
                    apiService.getForecasts(
                        regionId = regionId,
                        pollenId = pollenId,
                        current = current,
                        startDate = startDate,
                        endDate = endDate,
                        offset = offset,
                        limit = pageSize,
                    )
                val domainData = remoteData.toDomain { mapper.mapForecast(it) }
                allItems.addAll(domainData.items)

                if (domainData.meta.offset + domainData.meta.count >= domainData.meta.totalRecords) {
                    break
                }
                offset += domainData.meta.limit
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
            apiService
                .getPollenCount(
                    regionId = regionId,
                    pollenId = pollenId,
                    hasTechnicalError = hasTechnicalError,
                    startDate = startDate,
                    endDate = endDate,
                    offset = offset,
                    limit = limit,
                ).toDomain { mapper.mapPollenCount(it) }

        override suspend fun getPollenCountsWithoutErrors(
            regionId: String?,
            pollenId: String?,
            startDate: String?,
            endDate: String?,
            offset: Int,
            limit: Int,
        ): PaginatedResponse<PollenCount> =
            apiService
                .getPollenCount(
                    regionId = regionId,
                    pollenId = pollenId,
                    hasTechnicalError = false,
                    startDate = startDate,
                    endDate = endDate,
                    offset = offset,
                    limit = limit,
                ).toDomain { mapper.mapPollenCount(it) }

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
                val remoteData =
                    apiService.getPollenCount(
                        regionId = regionId,
                        pollenId = pollenId,
                        hasTechnicalError = if (excludeTechnicalErrors) false else null,
                        startDate = startDate,
                        endDate = endDate,
                        offset = offset,
                        limit = pageSize,
                    )
                val domainData = remoteData.toDomain { mapper.mapPollenCount(it) }
                allItems.addAll(domainData.items)

                if (domainData.meta.offset + domainData.meta.count >= domainData.meta.totalRecords) {
                    break
                }
                offset += domainData.meta.limit
            } while (true)

            return allItems
        }

        override suspend fun getAllergensList(): List<AllergenItem> =
            withContext(Dispatchers.IO) {
                val pollenTypes = fetchAllPollenTypes()

                pollenTypes.map { pollenType ->
                    AllergenItem(
                        id = pollenType.id ?: "",
                        name = pollenType.name,
                        hasForecast = pollenType.hasForecasts ?: false,
                        hasPollenCounts = pollenType.hasPollenCounts ?: false,
                    )
                }
            }

        @Suppress("MagicNumber")
        private suspend fun fetchAllPollenTypes(): List<PollenType> {
            val allItems = mutableListOf<PollenType>()
            var offset = 0
            val limit = 100

            do {
                val response = apiService.getPollenTypes(offset = offset, limit = limit)
                allItems.addAll(response.items.map { mapper.mapPollenType(it) })

                if (response.meta.offset + response.meta.count >= response.meta.totalRecords) {
                    break
                }
                offset += response.meta.limit
            } while (true)

            return allItems
        }
    }
