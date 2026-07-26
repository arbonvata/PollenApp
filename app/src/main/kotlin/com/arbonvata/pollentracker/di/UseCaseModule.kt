package com.arbonvata.pollentracker.di

import com.arbonvata.pollentracker.data.usecase.GetAllergensUseCaseImpl
import com.arbonvata.pollentracker.data.usecase.GetRegionsUseCaseImpl
import com.arbonvata.pollentracker.domain.usecases.GetAllergensUseCase
import com.arbonvata.pollentracker.domain.usecases.GetRegionsUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class UseCaseModule {
    @Binds
    @ViewModelScoped
    abstract fun bindGetRegionsUseCase(getRegionsUseCaseImpl: GetRegionsUseCaseImpl): GetRegionsUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindsGetAllergensUseCase(getAllergensUseCaseImpl: GetAllergensUseCaseImpl): GetAllergensUseCase
}
