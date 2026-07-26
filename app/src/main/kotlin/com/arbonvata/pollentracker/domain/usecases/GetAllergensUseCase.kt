package com.arbonvata.pollentracker.domain.usecases

import com.arbonvata.pollentracker.domain.model.AllergenItem

interface GetAllergensUseCase {
    suspend operator fun invoke(): List<AllergenItem>
}
