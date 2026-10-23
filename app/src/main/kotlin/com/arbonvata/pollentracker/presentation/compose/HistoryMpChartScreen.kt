package com.arbonvata.pollentracker.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.arbonvata.pollentracker.R
import com.arbonvata.pollentracker.domain.model.AllergenHistory
import com.arbonvata.pollentracker.presentation.viewmodel.HistoryUiState
import com.arbonvata.pollentracker.presentation.viewmodel.HistoryViewModel
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.compose.LineChart
import com.github.mikephil.charting.compose.rememberChartState
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// ~one x label per month, epoch days are the x values
private const val MONTH_GRANULARITY_DAYS = 30f

private val monthFormatter =
    IAxisValueFormatter { value, _ ->
        LocalDate.ofEpochDay(value.toLong()).month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

@Composable
fun HistoryMpChartScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
    onSwitchChart: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    HistoryMpChartScreenContent(
        uiState = uiState,
        onRetry = viewModel::loadData,
        modifier = modifier,
        onSwitchChart = onSwitchChart,
    )
}

@Composable
fun HistoryMpChartScreenContent(
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

            uiState.isError -> {
                ErrorState(
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )
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
                            Text(text = stringResource(R.string.chart_canvas))
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(uiState.allergenHistory, key = { it.pollenId }) { allergen ->
                            AllergenHistoryCardMpChart(allergen = allergen)
                        }
                    }
                }
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
fun AllergenHistoryCardMpChart(
    allergen: AllergenHistory,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val lineData =
        remember(allergen, lineColor) {
            val entries =
                allergen.levels.map { daily ->
                    Entry(daily.date.toEpochDay().toFloat(), (daily.level ?: 0).toFloat())
                }
            val dataSet =
                LineDataSet(entries, allergen.pollenName ?: allergen.pollenId).apply {
                    color = lineColor.toArgb()
                    lineWidth = 2f
                    isDrawCirclesEnabled = false
                    isDrawValuesEnabled = false
                }
            LineData(dataSet)
        }

    val contentDesc = stringResource(R.string.cd_yearly_pollen_levels, allergen.pollenName ?: allergen.pollenId)

    Card(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            AllergenHistoryCardHeader(allergen = allergen)

            Spacer(modifier = Modifier.height(12.dp))

            if (LocalInspectionMode.current) {
                // MPAndroidChart hosts an Android View, which does not render in previews.
                // Draw the same data with the Canvas chart so the preview shows the real shape.
                YearlyLevelChart(
                    levels = allergen.levels,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                )
            } else {
                LineChart(
                    data = lineData,
                    state = rememberChartState(),
                    contentDescription = contentDesc,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                    setup = {
                        description.isEnabled = false
                        legend.isEnabled = false
                        isTouchEnabled = false
                        axisRight.isEnabled = false
                        axisLeft.axisMinimum = 0f
                        axisLeft.axisMaximum = MAX_CHART_LEVEL.toFloat()
                        axisLeft.granularity = 1f
                        axisLeft.isGranularityEnabled = true
                        axisLeft.labelCount = 4
                        axisLeft.textColor = axisTextColor.toArgb()
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.isDrawGridLinesEnabled = false
                        xAxis.granularity = MONTH_GRANULARITY_DAYS
                        xAxis.isGranularityEnabled = true
                        xAxis.valueFormatter = monthFormatter
                        xAxis.textColor = axisTextColor.toArgb()
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryMpChartScreenPreview() {
    PollenTrackerTheme {
        HistoryMpChartScreenContent(
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
