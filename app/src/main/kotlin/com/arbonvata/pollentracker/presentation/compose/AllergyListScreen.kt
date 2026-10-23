package com.arbonvata.pollentracker.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.arbonvata.pollentracker.R
import com.arbonvata.pollentracker.domain.model.AllergenItem
import com.arbonvata.pollentracker.presentation.getAllergenIcon
import com.arbonvata.pollentracker.presentation.viewmodel.AllergensUiState
import com.arbonvata.pollentracker.presentation.viewmodel.AllergensViewModel
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme

@Composable
fun AllergyListScreen(
    allergenListViewModel: AllergensViewModel = hiltViewModel(),
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allergensState by allergenListViewModel.allergensState.collectAsState()
    val selectedIds by allergenListViewModel.selectedAllergenIds.collectAsState()

    AllergyListContent(
        allergensState = allergensState,
        selectedIds = selectedIds,
        onAllergenToggle = { allergenListViewModel.onAllergenToggle(it) },
        onRetry = { allergenListViewModel.loadData() },
        onSaveClick = {
            allergenListViewModel.saveAllergySelection(onSuccess = onNavigateNext)
        },
        modifier = modifier,
    )
}

@Composable
fun AllergyListContent(
    allergensState: AllergensUiState,
    selectedIds: Set<String>,
    onAllergenToggle: (String) -> Unit,
    onRetry: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading = allergensState.isLoading
    val isError = allergensState.isError
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Button(
                onClick = onSaveClick,
                enabled = !isLoading && !isError,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(text = stringResource(R.string.save))
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
                text = stringResource(R.string.your_allergies),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
            )
            Text(
                text = stringResource(R.string.select_pollen_types_to_track),
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

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                isError -> {
                    ErrorState(
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    CommonAllergyListSection(
                        allergens = allergensState.allergens,
                        selectedIds = selectedIds,
                        onAllergenToggle = onAllergenToggle,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.error_load_failed),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
fun CommonAllergyListSection(
    allergens: List<AllergenItem>,
    selectedIds: Set<String>,
    onAllergenToggle: (String) -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.common_allergies),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
        )
        LazyColumn {
            items(items = allergens, key = { it.id }) { allergen ->
                AllergyListItem(
                    item = allergen,
                    isChecked = selectedIds.contains(allergen.id),
                    onCheckedChange = { onAllergenToggle(allergen.id) },
                )
            }
        }
    }
}

@Composable
fun AllergyListItem(
    item: AllergenItem,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = getAllergenIcon(item.name),
            contentDescription = item.name,
            modifier =
                Modifier
                    .padding(end = 16.dp)
                    .size(24.dp),
            tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = item.name,
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
            allergensState =
                AllergensUiState(
                    allergens =
                        listOf(
                            AllergenItem(
                                id = "1",
                                name = "Birch",
                                hasForecast = true,
                                hasPollenCounts = true,
                            ),
                            AllergenItem(
                                id = "2",
                                name = "Grass",
                                hasForecast = true,
                                hasPollenCounts = true,
                            ),
                        ),
                ),
            selectedIds = setOf("1"),
            onAllergenToggle = {},
            onRetry = {},
            onSaveClick = {},
        )
    }
}
