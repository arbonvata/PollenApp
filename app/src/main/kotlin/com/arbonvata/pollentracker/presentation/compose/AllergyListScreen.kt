package com.arbonvata.pollentracker.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.arbonvata.pollentracker.domain.model.AllergenItem
import com.arbonvata.pollentracker.presentation.viewmodel.AllergensViewModel
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme

@Composable
fun AllergyListScreen(
    allergenListViewModel: AllergensViewModel = hiltViewModel(),
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allergens by allergenListViewModel.allergensState.collectAsState()
    val selectedIds by allergenListViewModel.selectedAllergenIds.collectAsState()

    AllergyListContent(
        allergens = allergens,
        selectedIds = selectedIds,
        onAllergenToggle = { allergenListViewModel.onAllergenToggle(it) },
        onSaveClick = {
            allergenListViewModel.saveAllergySelection(onSuccess = onNavigateNext)
        },
        onLoadData = { allergenListViewModel.loadData() },
        modifier = modifier,
    )
}

@Composable
fun AllergyListContent(
    allergens: List<AllergenItem>,
    selectedIds: Set<String>,
    onAllergenToggle: (String) -> Unit,
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
                allergens = allergens,
                selectedIds = selectedIds,
                onAllergenToggle = onAllergenToggle,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
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
            selectedIds = setOf("1"),
            onAllergenToggle = {},
            onSaveClick = {},
            onLoadData = {},
        )
    }
}
