package com.arbonvata.pollentracker.data.usecase

import com.arbonvata.pollentracker.domain.model.AllergenHistory
import com.arbonvata.pollentracker.domain.repositories.PollenRepository
import com.arbonvata.pollentracker.domain.usecases.GetYearlyHistoryUseCase
import javax.inject.Inject

class GetYearlyHistoryUseCaseImpl
    @Inject
    constructor(
        private val repository: PollenRepository,
    ) : GetYearlyHistoryUseCase {
        override suspend operator fun invoke(
            regionId: String,
            allergens: List<String>,
        ): Result<List<AllergenHistory>> {
            try {
                val history = repository.getYearlyHistoryForAllergens(regionId, allergens)
                return Result.success(history)
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }
    }
