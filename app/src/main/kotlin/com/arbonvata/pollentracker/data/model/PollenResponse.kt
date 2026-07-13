package com.arbonvata.pollentracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ PAGINATION WRAPPERS ============

@Serializable
data class PaginatedResponse<T>(
    @SerialName("_meta")
    val meta: PaginationMeta,
    @SerialName("_links")
    val links: List<PaginationLink>,
    val items: List<T>,
)

@Serializable
data class PaginationMeta(
    @SerialName("totalRecords")
    val totalRecords: Int,
    val offset: Int,
    val limit: Int,
    val count: Int,
)

@Serializable
data class PaginationLink(
    val href: String?,
    val rel: String, // enum: ["self", "last", "first", "next", "prev"]
)

// ============ FORECAST RESPONSE ============

@Serializable
data class Forecast(
    val id: String?, // UUID format
    val regionId: String?, // UUID format, note: API uses "regionId" (camelCase)
    val startDate: String, // date format YYYY-MM-DD
    val endDate: String, // date format YYYY-MM-DD
    val text: String,
    val isEndOfSeason: Boolean?, // Note: API uses "isEndOfSeason" (camelCase)
    val images: List<ForecastImage>,
    val levelSeries: List<PollenLevel>, // Note: API uses "levelSeries" (camelCase)
)

// ============ FORECAST IMAGE ============

@Serializable
data class ForecastImage(
    val id: String, // UUID, required (not nullable per spec)
    @SerialName("pollenId")
    val pollenId: String?, // UUID, nullable
    val url: String, // URI, required
)

// ============ POLLEN LEVEL ============

@Serializable
data class PollenLevel(
    @SerialName("pollenId")
    val pollenId: String?, // UUID, nullable
    val level: PollenLevelValue, // Int enum 0-7
    val time: String, // date-time format
)

// ============ POLLEN LEVEL VALUE ENUM ============

@Serializable
enum class PollenLevelValue(
    val value: Int,
) {
    @SerialName("0")
    NONE(0),

    @SerialName("1")
    LOW(1),

    @SerialName("2")
    LOW_TO_MODERATE(2),

    @SerialName("3")
    MODERATE(3),

    @SerialName("4")
    MODERATE_TO_HIGH(4),

    @SerialName("5")
    HIGH(5),

    @SerialName("6")
    HIGH_TO_VERY_HIGH(6),

    @SerialName("7")
    VERY_HIGH(7),
}

// ============ OPTIONAL: OTHER TYPES FROM API ============

@Serializable
data class PollenType(
    val id: String?, // UUID
    val name: String,
    @SerialName("hasPollenCounts")
    val hasPollenCounts: Boolean?,
    @SerialName("hasForecasts")
    val hasForecasts: Boolean?,
    val forecasts: String, // URI
    @SerialName("thresholdLow")
    val thresholdLow: Int?,
    @SerialName("thresholdMedium")
    val thresholdMedium: Int?,
    @SerialName("thresholdHigh")
    val thresholdHigh: Int?,
    @SerialName("thresholdVeryHigh")
    val thresholdVeryHigh: Int?,
)

@Serializable
data class Region(
    val id: String?, // UUID
    val name: String,
    val longitude: String?,
    val latitude: String?,
    val forecasts: String, // URI
)

@Serializable
data class PollenLevelDefinition(
    val level: PollenLevelValue,
    val name: String?, // e.g., "Inga halter", "Låga", etc.
)

@Serializable
data class PollenCount(
    @SerialName("pollenId")
    val pollenId: String?, // UUID
    @SerialName("regionId")
    val regionId: String?, // UUID
    @SerialName("dailyCount")
    val dailyCount: Int?,
    @SerialName("countDescription")
    val countDescription: CountDescriptionValue?,
    @SerialName("technicalError")
    val technicalError: Boolean, // Note: NOT nullable per spec
    val date: String, // date-time
)

@Serializable
enum class CountDescriptionValue {
    @SerialName("Inga halter")
    INGA_HALTER,

    @SerialName("Låga halter")
    LÅGA_HALTER,

    @SerialName("Måttliga halter")
    MÅTTLIGA_HALTER,

    @SerialName("Höga halter")
    HÖGA_HALTER,

    @SerialName("Mycket höga halter")
    MYCKET_HÖGA_HALTER,
}
