package com.arbonvata.pollentracker.domain.usecases

import com.arbonvata.pollentracker.domain.model.Region

interface GetRegionsUseCase {
    suspend fun invoke(
        offset: Int = 0,
        limit: Int = 100,
    ): Result<List<Region>>
}
