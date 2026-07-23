package com.arbonvata.pollentracker.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arbonvata.pollentracker.domain.model.Forecast
import com.arbonvata.pollentracker.domain.model.ForecastImage
import com.arbonvata.pollentracker.presentation.viewmodel.PollenTrackerUiState
import com.arbonvata.pollentracker.presentation.viewmodel.PollenTrackerViewModel
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme

@Composable
fun AllergyListScreen(
    pollenTrackerViewModel: PollenTrackerViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by pollenTrackerViewModel.uiState.collectAsState()

    AllergyListContent(
        uiState = uiState,
        onSaveClick = { pollenTrackerViewModel.saveAllergySelection() },
        onLoadData = { pollenTrackerViewModel.loadInitialData() },
        modifier = modifier,
    )
}

@Composable
fun AllergyListContent(
    uiState: PollenTrackerUiState,
    onSaveClick: () -> Unit,
    onLoadData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        onLoadData()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Button(
                onClick = onSaveClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(text = "Save")
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
        ) {
            Text(
                text = "Your allergies",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
            )
            Text(
                text = "Select pollen types to track",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 0.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                thickness = 2.dp,
            )
            CommonAllergyListSection(
                items = uiState.forecasts.flatMap { it.images },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
fun CommonAllergyListSection(
    items: List<ForecastImage>,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Common allergies",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
        )
        LazyColumn {
            items(items = items) { item ->
                AllergyListItem(item = item)
            }
        }
    }
}

@Composable
fun AllergyListItem(
    item: ForecastImage,
    imageUrl: String = "",
    text: String = "",
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AllergyListScreenPreview() {
    PollenTrackerTheme {
        AllergyListContent(
            uiState =
                PollenTrackerUiState(
                    forecasts =
                        listOf(
                            Forecast(
                                startDate = "2026-07-16",
                                endDate = "2026-07-17",
                                text = "Sample Forecast",
                                images =
                                    listOf(
                                        ForecastImage(id = "1", url = "https://example.com/1.png"),
                                        ForecastImage(id = "2", url = "https://example.com/2.png"),
                                    ),
                                levelSeries = emptyList(),
                            ),
                        ),
                ),
            onSaveClick = {},
            onLoadData = {},
        )
    }
}
