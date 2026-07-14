package com.arbonvata.pollentracker.data.network

import com.arbonvata.pollentracker.data.model.Forecast
import com.arbonvata.pollentracker.data.model.PaginatedResponse
import com.arbonvata.pollentracker.data.model.PollenCount
import com.arbonvata.pollentracker.data.model.PollenLevelDefinition
import com.arbonvata.pollentracker.data.model.PollenType
import com.arbonvata.pollentracker.data.model.Region
import retrofit2.http.GET
import retrofit2.http.Query

interface PollenApiService {
    // ============ REGIONS ============
    @GET("v1/regions")
    suspend fun getRegions(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
    ): PaginatedResponse<Region>

    // ============ POLLEN TYPES ============
    @GET("v1/pollen-types")
    suspend fun getPollenTypes(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
    ): PaginatedResponse<PollenType>

    // ============ POLLEN LEVEL DEFINITIONS ============
    @GET("v1/pollen-level-definitions")
    suspend fun getPollenLevelDefinitions(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
    ): PaginatedResponse<PollenLevelDefinition>

    // ============ FORECASTS ============
    @GET("v1/forecasts")
    suspend fun getForecasts(
        @Query("region_id") regionId: String? = null,
        @Query("pollen_id") pollenId: String? = null,
        @Query("current") current: Boolean? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
    ): PaginatedResponse<Forecast>

    // ============ POLLEN COUNT ============
    @GET("v1/pollen-count")
    suspend fun getPollenCount(
        @Query("region_id") regionId: String? = null,
        @Query("pollen_id") pollenId: String? = null,
        @Query("has_technical_error") hasTechnicalError: Boolean? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
    ): PaginatedResponse<PollenCount>
}
