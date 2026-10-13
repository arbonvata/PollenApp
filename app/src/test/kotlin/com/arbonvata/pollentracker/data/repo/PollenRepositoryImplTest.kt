package com.arbonvata.pollentracker.data.repo

import android.util.Log
import com.arbonvata.pollentracker.data.model.PaginatedResponseRemote
import com.arbonvata.pollentracker.data.model.PaginationMetaRemote
import com.arbonvata.pollentracker.data.model.PollenCountRemote
import com.arbonvata.pollentracker.data.model.PollenTypeRename
import com.arbonvata.pollentracker.data.network.PollenApiService
import com.arbonvata.pollentracker.data.network.mapper.PollenRemoteToLocalMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PollenRepositoryImplTest {
    private val zone = ZoneId.of("Europe/Stockholm")
    private lateinit var apiService: PollenApiService
    private lateinit var repository: PollenRepositoryImpl

    private val birchId = "birch-id"
    private val grassId = "grass-id"

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        apiService = mockk()
        repository = PollenRepositoryImpl(apiService, PollenRemoteToLocalMapper())
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun pollenType(
        id: String,
        name: String,
    ) = PollenTypeRename(id = id, name = name, forecasts = "")

    private fun pollenCount(
        pollenId: String,
        date: LocalDate,
        dailyCount: Int?,
        description: String?,
        technicalError: Boolean = false,
    ) = PollenCountRemote(
        pollenId = pollenId,
        regionId = "region-1",
        dailyCount = dailyCount,
        countDescription = description,
        technicalError = technicalError,
        date = "$date" + "T00:00:00",
    )

    private fun <T> page(
        items: List<T>,
        totalRecords: Int,
        offset: Int,
        limit: Int,
    ) = PaginatedResponseRemote(
        meta =
            PaginationMetaRemote(
                totalRecords = totalRecords,
                offset = offset,
                limit = limit,
                count = items.size,
            ),
        links = emptyList(),
        items = items,
    )

    private fun stubPollenTypes() {
        coEvery { apiService.getPollenTypes(any(), any()) } returns
            page(
                listOf(pollenType(birchId, "Björk"), pollenType(grassId, "Gräs")),
                totalRecords = 2,
                offset = 0,
                limit = 100,
            )
    }

    private fun stubYearlyPollenCount(today: LocalDate) {
        val jan2 = LocalDate.of(today.year, 1, 2)
        val jan3 = LocalDate.of(today.year, 1, 3)

        coEvery {
            apiService.getPollenCount(
                regionId = "region-1",
                pollenId = null,
                hasTechnicalError = false,
                startDate = LocalDate.of(today.year, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE),
                endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE),
                offset = 0,
                limit = 1000,
            )
        } returns
            page(
                listOf(
                    pollenCount(grassId, jan2, 0, "Inga halter"),
                    pollenCount(birchId, jan2, 1, "Låga halter"),
                ),
                totalRecords = 5,
                offset = 0,
                limit = 1000,
            )

        coEvery {
            apiService.getPollenCount(
                regionId = "region-1",
                pollenId = null,
                hasTechnicalError = false,
                startDate = LocalDate.of(today.year, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE),
                endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE),
                offset = 1000,
                limit = 1000,
            )
        } returns
            page(
                listOf(
                    // corrected value for an already-seen date - the last record must win
                    pollenCount(birchId, jan2, 10, "Måttliga halter"),
                    pollenCount(birchId, jan3, 60, "Mycket höga halter"),
                    pollenCount(grassId, jan3, null, null, technicalError = true),
                ),
                totalRecords = 5,
                offset = 1000,
                limit = 1000,
            )
    }

    @Test
    fun `yearly history paginates, groups per allergen, dedupes dates, maps levels`() =
        runTest {
            val today = LocalDate.now(zone)
            stubPollenTypes()
            stubYearlyPollenCount(today)

            val history = repository.getYearlyHistoryForAllergens(regionId = "region-1")

            assertEquals(2, history.size)
            assertEquals("Björk", history[0].pollenName)
            assertEquals("Gräs", history[1].pollenName)

            val birch = history[0]
            assertEquals(birchId, birch.pollenId)
            assertEquals(2, birch.levels.size)
            assertEquals(LocalDate.of(today.year, 1, 2), birch.levels[0].date)
            assertEquals(10, birch.levels[0].dailyCount)
            assertEquals("Måttliga halter", birch.levels[0].levelDescription)
            assertEquals(3, birch.levels[0].level)
            assertEquals(LocalDate.of(today.year, 1, 3), birch.levels[1].date)
            assertEquals(60, birch.levels[1].dailyCount)
            assertEquals(7, birch.levels[1].level)

            val grass = history[1]
            assertEquals(1, grass.levels.size)
            assertEquals(LocalDate.of(today.year, 1, 2), grass.levels[0].date)
            assertEquals(0, grass.levels[0].level)

            coVerify {
                apiService.getPollenCount(
                    regionId = "region-1",
                    pollenId = null,
                    hasTechnicalError = false,
                    startDate = LocalDate.of(today.year, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    offset = 0,
                    limit = 1000,
                )
                apiService.getPollenCount(
                    regionId = "region-1",
                    pollenId = null,
                    hasTechnicalError = false,
                    startDate = LocalDate.of(today.year, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    offset = 1000,
                    limit = 1000,
                )
            }
        }

    @Test
    fun `yearly history filters by allergen name`() =
        runTest {
            val today = LocalDate.now(zone)
            stubPollenTypes()
            stubYearlyPollenCount(today)

            val history =
                repository.getYearlyHistoryForAllergens(
                    regionId = "region-1",
                    allergens = listOf("Björk"),
                )

            assertEquals(1, history.size)
            assertEquals(birchId, history[0].pollenId)
        }

    @Test
    fun `yearly history returns empty list for unknown allergens`() =
        runTest {
            stubPollenTypes()

            val history =
                repository.getYearlyHistoryForAllergens(
                    regionId = "region-1",
                    allergens = listOf("Nonexistent"),
                )

            assertTrue(history.isEmpty())
        }
}
