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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LocationChooserScreen(

    modifier: Modifier = Modifier,
    locations: List<String>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        var textInput by remember { mutableStateOf("") }
        OutlinedTextField(
            value = textInput,
            onValueChange = { newText -> textInput = newText },
            label = { Text("Enter Location") },
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
        var chosenString by remember { mutableStateOf<String?>(null) }
        DynamicRadioButtonGroup(
            items = locations,
            selectedItem = chosenString, // You can manage the selected item state here
            onItemSelect = { chosenString = it },
            itemIdProvider = { it }, // Assuming the location string is unique
            itemTextProvider = { it }, // Display the location string
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1f),
        )

        Button(
            onClick = { /* Handle save action */ },
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
    LocationChooserScreen(
        locations = listOf("London", "Paris", "New York", "Tokyo"),
    )
}
