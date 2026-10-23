package com.arbonvata.pollentracker.presentation.viewmodel

import android.util.Log
import app.cash.turbine.test
import com.arbonvata.pollentracker.domain.model.AllergenItem
import com.arbonvata.pollentracker.domain.model.UserSettings
import com.arbonvata.pollentracker.domain.repositories.UserPreferenceRepository
import com.arbonvata.pollentracker.domain.usecases.GetAllergensUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
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

@OptIn(ExperimentalCoroutinesApi::class)
class AllergensViewModelTest {
    private val getAllergensUseCase: GetAllergensUseCase = mockk()
    private val userPreferenceRepository: UserPreferenceRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val allergens =
        listOf(
            AllergenItem(id = "birch-id", name = "Björk", hasForecast = true, hasPollenCounts = true),
            AllergenItem(id = "grass-id", name = "Gräs", hasForecast = true, hasPollenCounts = true),
            AllergenItem(id = "dead-id", name = "No forecast", hasForecast = false, hasPollenCounts = false),
        )

    private val settings =
        UserSettings(
            regionId = "region-1",
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
    fun `loadData emits loading then allergens filtered to hasForecast and preselected ids`() =
        runTest {
            coEvery { getAllergensUseCase() } returns Result.success(allergens)

            val viewModel = AllergensViewModel(getAllergensUseCase, userPreferenceRepository)

            viewModel.allergensState.test {
                // Initial state before the init block runs
                assertEquals(AllergensUiState(), awaitItem())

                testDispatcher.scheduler.advanceUntilIdle()

                assertEquals(AllergensUiState(isLoading = true), awaitItem())

                val loaded = awaitItem()
                assertFalse(loaded.isLoading)
                assertFalse(loaded.isError)
                assertEquals(allergens.filter { it.hasForecast }, loaded.allergens)
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(setOf("birch-id"), viewModel.selectedAllergenIds.value)
        }

    @Test
    fun `loadData emits error state when use case fails instead of crashing`() =
        runTest {
            coEvery { getAllergensUseCase() } returns Result.failure(RuntimeException("network down"))

            val viewModel = AllergensViewModel(getAllergensUseCase, userPreferenceRepository)

            viewModel.allergensState.test {
                assertEquals(AllergensUiState(), awaitItem())

                testDispatcher.scheduler.advanceUntilIdle()

                assertEquals(AllergensUiState(isLoading = true), awaitItem())

                val errored = awaitItem()
                assertFalse(errored.isLoading)
                assertTrue(errored.isError)
                assertTrue(errored.allergens.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onAllergenToggle adds and removes selected ids`() =
        runTest {
            coEvery { getAllergensUseCase() } returns Result.success(allergens)

            val viewModel = AllergensViewModel(getAllergensUseCase, userPreferenceRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(setOf("birch-id"), viewModel.selectedAllergenIds.value)

            viewModel.onAllergenToggle("grass-id")
            assertEquals(setOf("birch-id", "grass-id"), viewModel.selectedAllergenIds.value)

            viewModel.onAllergenToggle("birch-id")
            assertEquals(setOf("grass-id"), viewModel.selectedAllergenIds.value)
        }

    @Test
    fun `saveAllergySelection persists ids and names then invokes onSuccess`() =
        runTest {
            coEvery { getAllergensUseCase() } returns Result.success(allergens)
            coEvery { userPreferenceRepository.writeData(any()) } returns Unit

            val viewModel = AllergensViewModel(getAllergensUseCase, userPreferenceRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onAllergenToggle("grass-id")

            var succeeded = false
            viewModel.saveAllergySelection { succeeded = true }
            testDispatcher.scheduler.advanceUntilIdle()

            val saved = slot<UserSettings>()
            coVerify { userPreferenceRepository.writeData(capture(saved)) }

            assertEquals(setOf("birch-id", "grass-id").toList().sorted(), saved.captured.allergyIds.sorted())
            assertEquals(listOf("Björk", "Gräs").sorted(), saved.captured.allergyNames.sorted())
            assertEquals("region-1", saved.captured.regionId)
            assertTrue(succeeded)
        }
}
