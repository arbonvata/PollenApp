package com.arbonvata.pollentracker.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.arbonvata.pollentracker.presentation.viewmodel.TodaysPollenViewModel
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class TodaysPollenScreenItemData(
    val pollenCount: Int,
    val pollenLevel: String,
    val pollenName: String,
)

@Composable
fun TodaysPollenScreen(
    modifier: Modifier = Modifier,
    viewModel: TodaysPollenViewModel = hiltViewModel(),
    onNavigateNext: () -> Unit,
) {
    val uiStates by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val currentUiState =
        if (selectedTabIndex < uiStates.size) {
            uiStates[selectedTabIndex]
        } else {
            null
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            DaysScreenHeader(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
            )
        },
    ) { innerPadding ->
        if (currentUiState == null || currentUiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (currentUiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = currentUiState.error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val itemDataList = currentUiState.pollenDataByDay.flatten()

            TodaysPollenScreenContent(
                itemDataList = itemDataList,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
fun TodaysPollenScreenContent(
    itemDataList: List<TodaysPollenScreenItemData>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (itemDataList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No forecast data for your selected allergens on this day.")
            }
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .weight(1f),
            ) {
                items(itemDataList.filter { it.pollenCount > 0 }) { itemData ->
                    TodaysPollenScreenItem(itemData = itemData)
                }
            }

            NoPollenDataForThoseItems(emptyAllergenList = itemDataList.filter { it.pollenCount == 0 })
        }
    }
}

@Composable
fun DaysScreenHeader(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val tabs =
        remember {
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)
            val dayAfterTomorrow = today.plusDays(2)

            val locale = Locale.getDefault()
            val tomorrowName =
                tomorrow.dayOfWeek
                    .getDisplayName(TextStyle.FULL, locale)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            val dayAfterTomorrowName =
                dayAfterTomorrow.dayOfWeek
                    .getDisplayName(TextStyle.FULL, locale)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

            listOf("Today", tomorrowName, dayAfterTomorrowName)
        }

    SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(text = title) },
            )
        }
    }
}

@Composable
fun TodaysPollenScreenItem(itemData: TodaysPollenScreenItemData) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = itemData.pollenName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Level: ${itemData.pollenLevel}",
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
            )
        }
        Text(
            text = "Count: ${itemData.pollenCount}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun NoPollenDataForThoseItems(emptyAllergenList: List<TodaysPollenScreenItemData>) {
    if (emptyAllergenList.isEmpty()) return

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "No pollen data available for:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyRow {
            items(emptyAllergenList) { itemData ->
                NoPollenDataItem(itemData = itemData)
            }
        }
    }
}

@Composable
fun NoPollenDataItem(itemData: TodaysPollenScreenItemData) {
    Box(
        modifier =
            Modifier
                .padding(end = 8.dp)
                .padding(8.dp),
    ) {
        Text(
            text = itemData.pollenName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TodaysPollenScreenPreview() {
    PollenTrackerTheme {
        TodaysPollenScreenContent(
            itemDataList =
                listOf(
                    TodaysPollenScreenItemData(
                        pollenCount = 150,
                        pollenLevel = "High",
                        pollenName = "Birch",
                    ),
                    TodaysPollenScreenItemData(
                        pollenCount = 20,
                        pollenLevel = "Low",
                        pollenName = "Grass",
                    ),
                    TodaysPollenScreenItemData(
                        pollenCount = 0,
                        pollenLevel = "None",
                        pollenName = "Mugwort",
                    ),
                ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
