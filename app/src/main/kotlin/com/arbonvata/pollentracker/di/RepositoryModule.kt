package com.arbonvata.pollentracker.di

import com.arbonvata.pollentracker.data.repo.PollenRepositoryImpl
import com.arbonvata.pollentracker.data.repo.UserSettingsRepositoryImpl
import com.arbonvata.pollentracker.domain.repositories.PollenRepository
import com.arbonvata.pollentracker.domain.repositories.UserPreferenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPollenRepository(pollenRepositoryImpl: PollenRepositoryImpl): PollenRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferenceRepository(userSettingsRepositoryImpl: UserSettingsRepositoryImpl): UserPreferenceRepository
}
