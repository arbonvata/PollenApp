package com.arbonvata.pollentracker.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.arbonvata.pollentracker.domain.model.PollenType
import com.arbonvata.pollentracker.ui.theme.GrayLight40
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme

@Composable
fun PollenFavoriteRegionSettings(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = {
                    // Handle click to navigate to location chooser screen
                })
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Plats",
                style = MaterialTheme.typography.titleLarge,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Text(
                text = "Ej angiven",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Navigate to location chooser screen",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PollenAllergenLabel(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = GrayLight40, shape = MaterialTheme.shapes.medium)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(16.dp),
    ) {
        Text(
            text = "Ange vilka pollenslag du vill följa. Du kan välja flera. Dessa kommer att visas på startsidan.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
        )
    }
}

@Composable
fun PollenTypeSettingsList(
    pollenTypes: List<PollenType>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(pollenTypes.size) { index ->
            PollenTypeSettingsItem(
                pollenType = pollenTypes[index],
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
fun PollenTypeSettingsItem(
    modifier: Modifier = Modifier,
    pollenType: PollenType,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = pollenType.name,
            contentDescription = "Pollen Type Icon",
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = pollenType.name,
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = false, // Replace with actual state
            onCheckedChange = { /* Handle switch toggle */ },
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PollenFavoriteRegionSettingsPreview() {
    PollenTrackerTheme {
        PollenFavoriteRegionSettings()
    }
}

@Preview(showBackground = true)
@Composable
fun PollenTypeSettingsItemPreview() {
    PollenTrackerTheme {
        PollenTypeSettingsItem(
            pollenType =
                PollenType(
                    id = "1",
                    name = "Björk",
                    forecasts = "",
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PollenAllergenLabelPreview() {
    PollenTrackerTheme {
        PollenAllergenLabel()
    }
}

@Preview(showBackground = true)
@Composable
fun PollenTypeSettingsListPreview() {
    PollenTrackerTheme {
        PollenTypeSettingsList(
            pollenTypes =
                listOf(
                    PollenType(id = "1", name = "Björk", forecasts = ""),
                    PollenType(id = "2", name = "Gräs", forecasts = ""),
                    PollenType(id = "3", name = "Gråbo", forecasts = ""),
                ),
        )
    }
}
