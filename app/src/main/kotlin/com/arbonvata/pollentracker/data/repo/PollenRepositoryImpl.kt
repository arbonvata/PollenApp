package com.arbonvata.pollentracker.data.repo

import android.util.Log
import com.arbonvata.pollentracker.data.model.PollenCountRemote
import com.arbonvata.pollentracker.data.network.PollenApiService
import com.arbonvata.pollentracker.data.network.mapper.PollenRemoteToLocalMapper
import com.arbonvata.pollentracker.data.network.mapper.toDomain
import com.arbonvata.pollentracker.domain.model.AllergenHistory
import com.arbonvata.pollentracker.domain.model.AllergenItem
import com.arbonvata.pollentracker.domain.model.Forecast
import com.arbonvata.pollentracker.domain.model.PaginatedResponse
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
            require(daysIncludingToday >= 1)

            if (regionId.isBlank()) {
                Log.w("PollenRepo", "regionId is blank - returning no forecasts")
                return emptyList()
            }

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

        /**
         * Measured daily levels per allergen from day 1 of the current year until today.
         * Fetches all pollen counts for the region in one paginated pass and groups
         * client-side, so one allergen filter per request is not needed.
         */
        @Suppress("MagicNumber")
        override suspend fun getYearlyHistoryForAllergens(
            regionId: String,
            allergens: List<String>,
        ): List<AllergenHistory> {
            if (regionId.isBlank()) {
                Log.w("PollenRepo", "regionId is blank - returning no history")
                return emptyList()
            }

            return withContext(Dispatchers.IO) {
                val today = LocalDate.now(zone)
                val startDate = LocalDate.of(today.year, 1, 1).format(df)
                val endDate = today.format(df)

                val allTypes = apiService.getPollenTypes().items
                val lookup =
                    allTypes.associate { it.name.lowercase(Locale.ROOT) to it.id } +
                        allTypes.associate { it.id?.lowercase(Locale.ROOT) to it.id }
                val targetPollenIds = allergens.mapNotNull { lookup[it.lowercase(Locale.ROOT)] }.toSet()

                if (allergens.isNotEmpty() && targetPollenIds.isEmpty()) {
                    Log.w("PollenRepo", "No matching pollen IDs for allergens: $allergens")
                    return@withContext emptyList()
                }

                Log.d("PollenRepo", "Fetching yearly pollen counts for region=$regionId from $startDate to $endDate")

                val counts = mutableListOf<PollenCountRemote>()
                var offset = 0
                val limit = 1000
                do {
                    val response =
                        apiService.getPollenCount(
                            regionId = regionId,
                            hasTechnicalError = false,
                            startDate = startDate,
                            endDate = endDate,
                            offset = offset,
                            limit = limit,
                        )
                    counts.addAll(response.items)
                    if (response.meta.offset + response.meta.count >= response.meta.totalRecords) {
                        break
                    }
                    offset += response.meta.limit
                } while (true)

                Log.d("PollenRepo", "Fetched ${counts.size} pollen count records for region $regionId")

                val idToName = allTypes.mapNotNull { type -> type.id?.let { it to type.name } }.toMap()

                counts
                    .filter { !it.technicalError && it.pollenId != null }
                    .filter { targetPollenIds.isEmpty() || it.pollenId in targetPollenIds }
                    .groupBy { it.pollenId!! }
                    .map { (pollenId, records) ->
                        AllergenHistory(
                            pollenId = pollenId,
                            pollenName = idToName[pollenId],
                            levels =
                                records
                                    .groupBy { it.date }
                                    .map { (_, sameDay) -> mapper.mapDailyPollenLevel(sameDay.last()) }
                                    .sortedBy { it.date },
                        )
                    }.sortedBy { it.pollenName ?: it.pollenId }
            }
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
