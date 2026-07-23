package com.arbonvata.pollentracker.presentation

import app.cash.turbine.test
import com.arbonvata.pollentracker.domain.model.Region
import com.arbonvata.pollentracker.domain.repositories.PollenRepository
import com.arbonvata.pollentracker.presentation.viewmodel.PollenTrackerViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PollenTrackerViewModelTest {
    private val repository: PollenRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Default mocks for init block
        coEvery { repository.getAllRegions(any()) } returns emptyList()
        coEvery { repository.getAllPollenTypes(any()) } returns emptyList()
        coEvery { repository.getAllPollenLevelDefinitions(any()) } returns emptyList()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadInitialData should update state with regions`() =
        runTest {
            // Given
            val regions = listOf(Region(id = "1", name = "Region 1", forecasts = "url"))
            coEvery { repository.getAllRegions(any()) } returns regions

            // When
            val viewModel = PollenTrackerViewModel(repository)

            // Then
            viewModel.uiState.test {
                // Initial state (isLoading = false)
                assertEquals(false, awaitItem().isLoading)

                // State after init block starts (isLoading = true)
                // We need to trigger the dispatcher
                testDispatcher.scheduler.runCurrent()

                val loadingState = awaitItem()
                assertEquals(true, loadingState.isLoading)

                // State after data loaded
                testDispatcher.scheduler.runCurrent()
                val successState = awaitItem()
                assertEquals(regions, successState.regions)
                assertEquals(false, successState.isLoading)
            }
        }

    @Test
    fun `onRegionSelected should update selected region id`() =
        runTest {
            // Given
            val regionId = "reg1"
            coEvery {
                repository.getAllForecasts(
                    regionId = any(),
                    pollenId = any(),
                    current = any(),
                    startDate = any(),
                    endDate = any(),
                    pageSize = any(),
                )
            } returns emptyList()

            val viewModel = PollenTrackerViewModel(repository)
            testDispatcher.scheduler.runCurrent()

            // When
            viewModel.onRegionSelected(regionId)
            testDispatcher.scheduler.runCurrent()

            // Then
            assertEquals(regionId, viewModel.uiState.value.selectedRegionId)
        }
}
