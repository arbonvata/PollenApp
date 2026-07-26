package com.arbonvata.pollentracker.presentation.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arbonvata.pollentracker.domain.model.Region
import com.arbonvata.pollentracker.domain.model.UserSettings
import com.arbonvata.pollentracker.presentation.viewmodel.UserSettingsViewModel

@Composable
fun LocationChooserScreen(
    modifier: Modifier = Modifier,
    viewModel: UserSettingsViewModel = hiltViewModel(),
    onNavigateNext: () -> Unit,
) {
    val userSettings by viewModel.userSettingsState.collectAsState()
    val regions by viewModel.regionsState.collectAsState()

    LocationChooserContent(
        modifier = modifier,
        regions = regions,
        initialSettings = userSettings,
        onSave = { updatedSettings ->
            viewModel.writeData(updatedSettings)
            onNavigateNext()
        },
    )
}

@Composable
fun LocationChooserContent(
    regions: List<Region>,
    initialSettings: UserSettings?,
    onSave: (UserSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var textInput by remember { mutableStateOf("") }
    var selectedRegion by remember(initialSettings, regions) {
        mutableStateOf(regions.find { it.id == initialSettings?.regionId })
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Search Location") },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )

        DynamicRadioButtonGroup(
            items = regions.filter { it.name.contains(textInput, ignoreCase = true) },
            selectedItem = selectedRegion,
            onItemSelect = { selectedRegion = it },
            itemIdProvider = { it.id ?: "" },
            itemTextProvider = { it.name },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .weight(1f),
        )

        Button(
            onClick = {
                selectedRegion?.id?.let { regionId ->
                    onSave(
                        UserSettings(
                            regionId = regionId,
                            allergyIds = initialSettings?.allergyIds ?: emptyList(),
                            allergyNames = initialSettings?.allergyNames ?: emptyList(),
                        ),
                    )
                }
            },
            enabled = selectedRegion != null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(text = "Save")
        }
    }
}

@Composable
fun <T> DynamicRadioButtonGroup(
    items: List<T>,
    selectedItem: T?,
    onItemSelect: (T) -> Unit,
    itemIdProvider: (T) -> Any, // Lambda to extract a unique ID from your object
    itemTextProvider: (T) -> String, // Lambda to extract the display text from your object
    modifier: Modifier = Modifier,
) {
    // Using LazyColumn handles lists of any size smoothly (even thousands of items)
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(items, key = { itemIdProvider(it) }) { item ->

            // Check selection by comparing unique IDs
            val isSelected = selectedItem?.let { itemIdProvider(it) == itemIdProvider(item) } == true

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onItemSelect(item) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onItemSelect(item) },
                )

                Text(
                    text = itemTextProvider(item),
                    modifier =
                        Modifier
                            .padding(start = 16.dp)
                            .weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationChooserScreenPreview() {
    LocationChooserContent(
        regions =
            listOf(
                Region(id = "1", name = "London", forecasts = ""),
                Region(id = "2", name = "Paris", forecasts = ""),
                Region(id = "3", name = "New York", forecasts = ""),
            ),
        initialSettings = UserSettings(regionId = "1", allergyIds = emptyList(), allergyNames = emptyList()),
        onSave = {},
    )
}
