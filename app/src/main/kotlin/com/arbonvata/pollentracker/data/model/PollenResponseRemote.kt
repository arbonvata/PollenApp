package com.arbonvata.pollentracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ GENERIC PAGINATION WRAPPER ============
@Serializable
data class PaginatedResponseRemote<T>(
    @SerialName("_meta") val meta: PaginationMetaRemote,
    @SerialName("_links") val links: List<PaginationLinkRemote>,
    val items: List<T>,
)

@Serializable
data class PaginationMetaRemote(
    val totalRecords: Int,
    val offset: Int,
    val limit: Int,
    val count: Int,
)

@Serializable
data class PaginationLinkRemote(
    val href: String? = null,
    // "self" | "last" | "first" | "next" | "prev"
    val rel: String,
)

// ============ REFERENCE DATA ============

@Serializable
data class PollenTypeRename(
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
data class RegionRemote(
    val id: String? = null,
    val name: String,
    val longitude: String? = null,
    val latitude: String? = null,
    val forecasts: String,
)

@Serializable
data class PollenLevelDefinitionRemote(
    val level: Int,
    val name: String? = null,
)

// ============ FORECAST DATA ============

@Serializable
data class ForecastRemote(
    val id: String? = null,
    val regionId: String? = null,
    val startDate: String,
    val endDate: String,
    val text: String,
    val isEndOfSeason: Boolean? = null,
    val images: List<ForecastImageRemote>,
    val levelSeries: List<PollenLevelRemote>,
)

@Serializable
data class ForecastImageRemote(
    val id: String,
    val pollenId: String? = null,
    val url: String,
)

@Serializable
data class PollenLevelRemote(
    val pollenId: String? = null,
    val level: Int,
    val time: String,
)

// ============ POLLEN COUNT DATA ============

@Serializable
data class PollenCountRemote(
    val pollenId: String? = null,
    val regionId: String? = null,
    val dailyCount: Int? = null,
    val countDescription: String? = null,
    val technicalError: Boolean,
    val date: String,
)

// ============ ERROR HANDLING (422 responses) ============

@Serializable
data class HTTPValidationErrorRename(
    val detail: List<ValidationErrorRemote>,
)

@Serializable
data class ValidationErrorRemote(
    val loc: List<String>,
    val msg: String,
    val type: String,
)
