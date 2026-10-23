package com.arbonvata.pollentracker.presentation.viewmodel

import android.util.Log
import app.cash.turbine.test
import com.arbonvata.pollentracker.domain.model.PollenTrackerUiState
import com.arbonvata.pollentracker.domain.model.UserSettings
import com.arbonvata.pollentracker.domain.repositories.PollenRepository
import com.arbonvata.pollentracker.domain.repositories.UserPreferenceRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodaysPollenViewModelTest {
    private val pollenRepository: PollenRepository = mockk()
    private val userPreferenceRepository: UserPreferenceRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val settings =
        UserSettings(
            regionId = "region-1",
            regionName = "Stockholm",
            allergyIds = listOf("birch-id"),
            allergyNames = listOf("Björk"),
        )

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
        Dispatchers.setMain(testDispatcher)
        coEvery { userPreferenceRepository.readData() } returns settings
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `loadData emits error state when repository throws`() =
        runTest {
            coEvery { pollenRepository.getAllPollenTypes() } throws RuntimeException("network down")

            val viewModel = TodaysPollenViewModel(pollenRepository, userPreferenceRepository)

            viewModel.uiState.test {
                // Initial state before the init block runs
                assertEquals(emptyList<PollenTrackerUiState>(), awaitItem())

                testDispatcher.scheduler.advanceUntilIdle()

                val errored = awaitItem().single()
                assertTrue(errored.isError)
                assertTrue(errored.pollenDataByDay.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadData clears the error state on retry`() =
        runTest {
            coEvery { pollenRepository.getAllPollenTypes() } throws RuntimeException("network down")

            val viewModel = TodaysPollenViewModel(pollenRepository, userPreferenceRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value
                    .single()
                    .isError,
            )

            // the repository recovers - retrying should leave the error state
            coEvery { pollenRepository.getAllPollenTypes() } returns emptyList()
            coEvery { pollenRepository.getAllPollenLevelDefinitions() } returns emptyList()
            coEvery { pollenRepository.getForecastForAllergens(any(), any(), any()) } returns emptyList()

            viewModel.loadData()
            testDispatcher.scheduler.advanceUntilIdle()

            val states = viewModel.uiState.value
            assertEquals(EXPECTED_DAY_STATE_COUNT, states.size)
            assertTrue(states.none { it.isError })
        }

    private companion object {
        const val EXPECTED_DAY_STATE_COUNT = 3
    }
}
