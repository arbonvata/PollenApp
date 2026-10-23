package com.arbonvata.pollentracker.data.usecase

import com.arbonvata.pollentracker.domain.model.AllergenItem
import com.arbonvata.pollentracker.domain.repositories.PollenRepository
import com.arbonvata.pollentracker.domain.usecases.GetAllergensUseCase
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class GetAllergensUseCaseImpl
    @Inject
    constructor(
        private val pollenRepository: PollenRepository,
    ) : GetAllergensUseCase {
        override suspend fun invoke(): Result<List<AllergenItem>> =
            try {
                Result.success(pollenRepository.getAllergensList())
            } catch (e: CancellationException) {
                // cancellation is not a failure - rethrow so the calling scope can end
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
