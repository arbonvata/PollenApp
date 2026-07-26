package com.arbonvata.pollentracker.data.usecase

import com.arbonvata.pollentracker.domain.model.AllergenItem
import com.arbonvata.pollentracker.domain.repositories.PollenRepository
import com.arbonvata.pollentracker.domain.usecases.GetAllergensUseCase
import javax.inject.Inject

class GetAllergensUseCaseImpl
    @Inject
    constructor(
        private val pollenRepository: PollenRepository,
    ) : GetAllergensUseCase {
        override suspend fun invoke(): List<AllergenItem> = pollenRepository.getAllergensList()
    }
