package com.arbonvata.pollentracker.data.usecase

import com.arbonvata.pollentracker.domain.model.Region
import com.arbonvata.pollentracker.domain.repositories.PollenRepository
import com.arbonvata.pollentracker.domain.usecases.GetRegionsUseCase
import javax.inject.Inject

class GetRegionsUseCaseImpl
    @Inject
    constructor(
        private val repository: PollenRepository,
    ) : GetRegionsUseCase {
        override suspend fun invoke(
            offset: Int,
            limit: Int,
        ): Result<List<Region>> {
            try {
                val regions = repository.getAllRegions(pageSize = limit)
                return Result.success(regions)
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }
    }
