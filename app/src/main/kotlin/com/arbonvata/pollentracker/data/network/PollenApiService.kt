package com.arbonvata.pollentracker.data.network

import com.arbonvata.pollentracker.data.model.ForecastRemote
import com.arbonvata.pollentracker.data.model.PaginatedResponseRemote
import com.arbonvata.pollentracker.data.model.PollenCountRemote
import com.arbonvata.pollentracker.data.model.PollenLevelDefinitionRemote
import com.arbonvata.pollentracker.data.model.PollenTypeRename
import com.arbonvata.pollentracker.data.model.RegionRemote
import retrofit2.http.GET
import retrofit2.http.Query

interface PollenApiService {
    // ============ REGIONS ============
    @GET("v1/regions")
    suspend fun getRegions(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
    ): PaginatedResponseRemote<RegionRemote>

    // ============ POLLEN TYPES ============
    @GET("v1/pollen-types")
    suspend fun getPollenTypes(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
    ): PaginatedResponseRemote<PollenTypeRename>

    // ============ POLLEN LEVEL DEFINITIONS ============
    @GET("v1/pollen-level-definitions")
    suspend fun getPollenLevelDefinitions(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
    ): PaginatedResponseRemote<PollenLevelDefinitionRemote>

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
    ): PaginatedResponseRemote<ForecastRemote>

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
    ): PaginatedResponseRemote<PollenCountRemote>
}
