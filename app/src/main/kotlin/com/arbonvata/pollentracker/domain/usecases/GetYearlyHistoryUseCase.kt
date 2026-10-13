package com.arbonvata.pollentracker.domain.usecases

import com.arbonvata.pollentracker.domain.model.AllergenHistory

interface GetYearlyHistoryUseCase {
    suspend operator fun invoke(
        regionId: String,
        allergens: List<String> = emptyList(),
    ): Result<List<AllergenHistory>>
}
