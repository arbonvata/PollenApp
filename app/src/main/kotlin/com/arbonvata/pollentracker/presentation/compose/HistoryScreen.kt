package com.arbonvata.pollentracker.presentation.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.arbonvata.pollentracker.R
import com.arbonvata.pollentracker.domain.model.AllergenHistory
import com.arbonvata.pollentracker.domain.model.DailyPollenLevel
import com.arbonvata.pollentracker.presentation.getAllergenIcon
import com.arbonvata.pollentracker.presentation.viewmodel.HistoryUiState
import com.arbonvata.pollentracker.presentation.viewmodel.HistoryViewModel
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme
import java.time.LocalDate
import kotlin.math.sin

// shared by the Canvas chart and the MPAndroidChart axis setup
internal const val MAX_CHART_LEVEL = 7

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
    onSwitchChart: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    HistoryScreenContent(
        uiState = uiState,
        onRetry = viewModel::loadData,
        modifier = modifier,
        onSwitchChart = onSwitchChart,
    )
}

@Composable
fun HistoryScreenContent(
    uiState: HistoryUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onSwitchChart: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetry) {
                        Text(text = stringResource(R.string.retry))
                    }
                }
            }

            uiState.allergenHistory.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = stringResource(R.string.no_historical_data))
                }
            }

            else -> {
                val year =
                    uiState.allergenHistory
                        .firstNotNullOfOrNull { allergen ->
                            allergen.levels
                                .firstOrNull()
                                ?.date
                                ?.year
                        }
                        ?: LocalDate.now().year

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.pollen_history_year, year),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onSwitchChart) {
                            Text(text = stringResource(R.string.chart_mpandroidchart))
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(uiState.allergenHistory, key = { it.pollenId }) { allergen ->
                            AllergenHistoryCard(allergen = allergen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AllergenHistoryCard(
    allergen: AllergenHistory,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            AllergenHistoryCardHeader(allergen = allergen)

            Spacer(modifier = Modifier.height(12.dp))

            YearlyLevelChart(
                levels = allergen.levels,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(96.dp),
            )
        }
    }
}

/** Header row shared by the Canvas and MPAndroidChart history cards. */
@Composable
fun AllergenHistoryCardHeader(allergen: AllergenHistory) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = getAllergenIcon(allergen.pollenName ?: ""),
            contentDescription = allergen.pollenName,
            modifier =
                Modifier
                    .size(40.dp)
                    .padding(end = 12.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = allergen.pollenName ?: allergen.pollenId,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.recorded_days, allergen.levels.size),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        allergen.levels.maxByOrNull { it.level ?: -1 }?.let { peak ->
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = peak.levelDescription ?: "-",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.peak_date, peak.date.toString()),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun YearlyLevelChart(
    levels: List<DailyPollenLevel>,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier) {
        val sorted = levels.sortedBy { it.date }
        if (sorted.size < 2) return@Canvas

        val minDay =
            sorted
                .first()
                .date
                .toEpochDay()
                .toFloat()
        val maxDay =
            sorted
                .last()
                .date
                .toEpochDay()
                .toFloat()
        val spanDays = (maxDay - minDay).coerceAtLeast(1f)

        // vertical gridline at the start of each month inside the data range
        var monthCursor = sorted.first().date.withDayOfMonth(1)
        while (monthCursor.toEpochDay().toFloat() <= maxDay) {
            val x = (monthCursor.toEpochDay() - minDay) / spanDays * size.width
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx(),
            )
            monthCursor = monthCursor.plusMonths(1)
        }

        val stepY = size.height / MAX_CHART_LEVEL
        val path = Path()
        sorted.forEachIndexed { index, daily ->
            val x = (daily.date.toEpochDay() - minDay) / spanDays * size.width
            val y = size.height - (daily.level ?: 0) * stepY
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

// Shared sample data for the Canvas and MPAndroidChart history screen previews
internal val previewDescriptions =
    listOf(
        "Inga halter",
        "Låga halter",
        "Låga halter",
        "Måttliga halter",
        "Måttliga halter",
        "Höga halter",
        "Höga halter",
        "Mycket höga halter",
    )

internal fun previewLevels(): List<DailyPollenLevel> {
    val start = LocalDate.of(LocalDate.now().year, 3, 1)
    return (0 until 150).map { index ->
        val level = (sin(index / 12f) * 3.5f + 3.5f).toInt()
        DailyPollenLevel(
            date = start.plusDays(index.toLong()),
            dailyCount = level * 8,
            levelDescription = previewDescriptions[level],
            level = level,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    PollenTrackerTheme {
        HistoryScreenContent(
            uiState =
                HistoryUiState(
                    allergenHistory =
                        listOf(
                            AllergenHistory(
                                pollenId = "birch-id",
                                pollenName = "Björk",
                                levels = previewLevels(),
                            ),
                            AllergenHistory(
                                pollenId = "grass-id",
                                pollenName = "Gräs",
                                levels = previewLevels().drop(30),
                            ),
                        ),
                ),
            onRetry = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
