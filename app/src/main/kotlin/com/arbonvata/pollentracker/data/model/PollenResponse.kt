package com.arbonvata.pollentracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ GENERIC PAGINATION WRAPPER ============
@Serializable
data class PaginatedResponse<T>(
    @SerialName("_meta") val meta: PaginationMeta,
    @SerialName("_links") val links: List<PaginationLink>,
    val items: List<T>,
)

@Serializable
data class PaginationMeta(
    val totalRecords: Int,
    val offset: Int,
    val limit: Int,
    val count: Int,
)

@Serializable
data class PaginationLink(
    val href: String? = null,
    // "self" | "last" | "first" | "next" | "prev"
    val rel: String,
)

// ============ REFERENCE DATA ============

@Serializable
data class PollenType(
    val id: String? = null,
    val name: String,
    val hasPollenCounts: Boolean? = null,
    val hasForecasts: Boolean? = null,
    val forecasts: String,
    val thresholdLow: Int? = null,
    val thresholdMedium: Int? = null,
    val thresholdHigh: Int? = null,
    val thresholdVeryHigh: Int? = null,
)

@Serializable
data class Region(
    val id: String? = null,
    val name: String,
    val longitude: String? = null,
    val latitude: String? = null,
    val forecasts: String,
)

@Serializable
data class PollenLevelDefinition(
    val level: Int,
    val name: String? = null,
)

// ============ FORECAST DATA ============

@Serializable
data class Forecast(
    val id: String? = null,
    val regionId: String? = null,
    val startDate: String,
    val endDate: String,
    val text: String,
    val isEndOfSeason: Boolean? = null,
    val images: List<ForecastImage>,
    val levelSeries: List<PollenLevel>,
)

@Serializable
data class ForecastImage(
    val id: String,
    val pollenId: String? = null,
    val url: String,
)

@Serializable
data class PollenLevel(
    val pollenId: String? = null,
    val level: Int,
    val time: String,
)

// ============ POLLEN COUNT DATA ============

@Serializable
data class PollenCount(
    val pollenId: String? = null,
    val regionId: String? = null,
    val dailyCount: Int? = null,
    val countDescription: String? = null,
    val technicalError: Boolean,
    val date: String,
)

// ============ ERROR HANDLING (422 responses) ============

@Serializable
data class HTTPValidationError(
    val detail: List<ValidationError>,
)

@Serializable
data class ValidationError(
    val loc: List<String>,
    val msg: String,
    val type: String,
)
