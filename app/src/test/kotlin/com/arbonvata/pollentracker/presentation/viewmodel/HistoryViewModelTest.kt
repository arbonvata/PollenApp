package com.arbonvata.pollentracker.presentation.viewmodel

import android.util.Log
import app.cash.turbine.test
import com.arbonvata.pollentracker.domain.model.AllergenHistory
import com.arbonvata.pollentracker.domain.model.DailyPollenLevel
import com.arbonvata.pollentracker.domain.model.UserSettings
import com.arbonvata.pollentracker.domain.repositories.UserPreferenceRepository
import com.arbonvata.pollentracker.domain.usecases.GetYearlyHistoryUseCase
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private val getYearlyHistory: GetYearlyHistoryUseCase = mockk()
    private val userPreferenceRepository: UserPreferenceRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val settings =
        UserSettings(
            regionId = "region-1",
            allergyIds = listOf("birch-id"),
            allergyNames = listOf("Björk"),
        )

    private val history =
        listOf(
            AllergenHistory(
                pollenId = "birch-id",
                pollenName = "Björk",
                levels =
                    listOf(
                        DailyPollenLevel(
                            date = LocalDate.of(2026, 5, 1),
                            dailyCount = 12,
                            levelDescription = "Måttliga halter",
                            level = 3,
                        ),
                    ),
            ),
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
    fun `loadData emits loading then history`() =
        runTest {
            coEvery { getYearlyHistory(any(), any()) } returns Result.success(history)

            val viewModel = HistoryViewModel(getYearlyHistory, userPreferenceRepository)

            viewModel.uiState.test {
                // Initial state before the init block runs
                assertEquals(HistoryUiState(), awaitItem())

                testDispatcher.scheduler.advanceUntilIdle()

                assertEquals(HistoryUiState(isLoading = true), awaitItem())

                val loaded = awaitItem()
                assertFalse(loaded.isLoading)
                assertEquals(history, loaded.allergenHistory)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadData emits error state when use case fails`() =
        runTest {
            coEvery { getYearlyHistory(any(), any()) } returns Result.failure(RuntimeException("network down"))

            val viewModel = HistoryViewModel(getYearlyHistory, userPreferenceRepository)

            viewModel.uiState.test {
                assertEquals(HistoryUiState(), awaitItem())

                testDispatcher.scheduler.advanceUntilIdle()

                assertEquals(HistoryUiState(isLoading = true), awaitItem())

                val errored = awaitItem()
                assertFalse(errored.isLoading)
                assertTrue(errored.isError)
                assertTrue(errored.allergenHistory.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
