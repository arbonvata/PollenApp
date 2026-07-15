package com.arbonvata.pollentracker.data.network.mapper

import com.arbonvata.pollentracker.data.model.ForecastImageRemote
import com.arbonvata.pollentracker.data.model.ForecastRemote
import com.arbonvata.pollentracker.data.model.HTTPValidationErrorRename
import com.arbonvata.pollentracker.data.model.PaginatedResponseRemote
import com.arbonvata.pollentracker.data.model.PaginationLinkRemote
import com.arbonvata.pollentracker.data.model.PaginationMetaRemote
import com.arbonvata.pollentracker.data.model.PollenCountRemote
import com.arbonvata.pollentracker.data.model.PollenLevelDefinitionRemote
import com.arbonvata.pollentracker.data.model.PollenLevelRemote
import com.arbonvata.pollentracker.data.model.PollenTypeRename
import com.arbonvata.pollentracker.data.model.RegionRemote
import com.arbonvata.pollentracker.data.model.ValidationErrorRemote
import com.arbonvata.pollentracker.domain.model.Forecast
import com.arbonvata.pollentracker.domain.model.ForecastImage
import com.arbonvata.pollentracker.domain.model.HTTPValidationError
import com.arbonvata.pollentracker.domain.model.PaginatedResponse
import com.arbonvata.pollentracker.domain.model.PaginationLink
import com.arbonvata.pollentracker.domain.model.PaginationMeta
import com.arbonvata.pollentracker.domain.model.PollenCount
import com.arbonvata.pollentracker.domain.model.PollenLevel
import com.arbonvata.pollentracker.domain.model.PollenLevelDefinition
import com.arbonvata.pollentracker.domain.model.PollenType
import com.arbonvata.pollentracker.domain.model.Region
import com.arbonvata.pollentracker.domain.model.ValidationError
import javax.inject.Inject

class PollenRemoteToLocalMapper
    @Inject
    constructor() {
        fun <T, R> mapPaginatedResponse(
            remote: PaginatedResponseRemote<T>,
            itemMapper: (T) -> R,
        ): PaginatedResponse<R> =
            PaginatedResponse(
                meta = mapPaginationMeta(remote.meta),
                links = remote.links.map { mapPaginationLink(it) },
                items = remote.items.map { itemMapper(it) },
            )

        fun mapPaginationMeta(remote: PaginationMetaRemote): PaginationMeta =
            PaginationMeta(
                totalRecords = remote.totalRecords,
                offset = remote.offset,
                limit = remote.limit,
                count = remote.count,
            )

        fun mapPaginationLink(remote: PaginationLinkRemote): PaginationLink =
            PaginationLink(
                href = remote.href,
                rel = remote.rel,
            )

        fun mapPollenType(remote: PollenTypeRename): PollenType =
            PollenType(
                id = remote.id,
                name = remote.name,
                hasPollenCounts = remote.hasPollenCounts,
                hasForecasts = remote.hasForecasts,
                forecasts = remote.forecasts,
                thresholdLow = remote.thresholdLow,
                thresholdMedium = remote.thresholdMedium,
                thresholdHigh = remote.thresholdHigh,
                thresholdVeryHigh = remote.thresholdVeryHigh,
            )

        fun mapRegion(remote: RegionRemote): Region =
            Region(
                id = remote.id,
                name = remote.name,
                longitude = remote.longitude,
                latitude = remote.latitude,
                forecasts = remote.forecasts,
            )

        fun mapPollenLevelDefinition(remote: PollenLevelDefinitionRemote): PollenLevelDefinition =
            PollenLevelDefinition(
                level = remote.level,
                name = remote.name,
            )

        fun mapForecast(remote: ForecastRemote): Forecast =
            Forecast(
                id = remote.id,
                regionId = remote.regionId,
                startDate = remote.startDate,
                endDate = remote.endDate,
                text = remote.text,
                isEndOfSeason = remote.isEndOfSeason,
                images = remote.images.map { mapForecastImage(it) },
                levelSeries = remote.levelSeries.map { mapPollenLevel(it) },
            )

        fun mapForecastImage(remote: ForecastImageRemote): ForecastImage =
            ForecastImage(
                id = remote.id,
                pollenId = remote.pollenId,
                url = remote.url,
            )

        fun mapPollenLevel(remote: PollenLevelRemote): PollenLevel =
            PollenLevel(
                pollenId = remote.pollenId,
                level = remote.level,
                time = remote.time,
            )

        fun mapPollenCount(remote: PollenCountRemote): PollenCount =
            PollenCount(
                pollenId = remote.pollenId,
                regionId = remote.regionId,
                dailyCount = remote.dailyCount,
                countDescription = remote.countDescription,
                technicalError = remote.technicalError,
                date = remote.date,
            )

        fun mapHTTPValidationError(remote: HTTPValidationErrorRename): HTTPValidationError =
            HTTPValidationError(
                detail = remote.detail.map { mapValidationError(it) },
            )

        fun mapValidationError(remote: ValidationErrorRemote): ValidationError =
            ValidationError(
                loc = remote.loc,
                msg = remote.msg,
                type = remote.type,
            )
    }

// Extension functions for convenience
fun PollenTypeRename.toDomain() = PollenRemoteToLocalMapper().mapPollenType(this)

fun RegionRemote.toDomain() = PollenRemoteToLocalMapper().mapRegion(this)

fun PollenLevelDefinitionRemote.toDomain() = PollenRemoteToLocalMapper().mapPollenLevelDefinition(this)

fun ForecastRemote.toDomain() = PollenRemoteToLocalMapper().mapForecast(this)

fun PollenCountRemote.toDomain() = PollenRemoteToLocalMapper().mapPollenCount(this)

fun <T, R> PaginatedResponseRemote<T>.toDomain(itemMapper: (T) -> R) = PollenRemoteToLocalMapper().mapPaginatedResponse(this, itemMapper)
