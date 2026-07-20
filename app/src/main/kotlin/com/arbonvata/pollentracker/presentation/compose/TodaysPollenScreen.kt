package com.arbonvata.pollentracker.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme

data class TodaysPollenScreenItemData(
    val pollenType: String,
    val pollenCount: Int,
    val pollenLevel: String,
    val pollenName: String,
)

@Composable
fun TodaysPollenScreen(
    itemDataList: List<TodaysPollenScreenItemData>,
    modifier: Modifier = Modifier,
) {
    Column {
        DaysScreenHeader(itemDataList)

        LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
            items(itemDataList.filter { it.pollenCount > 0 }) { itemData ->
                TodaysPollenScreenItem(TodaysPollenScreenItemData = itemData)
            }
        }

        NoPollenDataForThoseItems(emptyAllergenList = itemDataList.filter { it.pollenCount == 0 })
    }
}

@Composable
fun DaysScreenHeader(itemDataList: List<TodaysPollenScreenItemData>) {
    // This tabrow is just a placeholder right now.
    // it will  be used for today, tomorrow, and after tomorrow pollen data.
    // For now, it will just be a static tabrow with no functionality.
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf("Home", "Explore", "Profile")

    SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = { Text(text = title) },
            )
        }
    }
}

@Composable
fun TodaysPollenScreenItem(TodaysPollenScreenItemData: TodaysPollenScreenItemData) {
    Row(modifier = Modifier) {
        AsyncImage(
            model = TodaysPollenScreenItemData.pollenType,
            contentDescription = TodaysPollenScreenItemData.pollenName,
            modifier = Modifier.size(50.dp).clip(CircleShape),
        )
        Column(modifier = Modifier.fillMaxHeight()) {
            Text(
                text = TodaysPollenScreenItemData.pollenName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Count: ${TodaysPollenScreenItemData.pollenCount}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
            )
            Text(
                text = "Level: ${TodaysPollenScreenItemData.pollenLevel}",
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun NoPollenDataForThoseItems(emptyAllergenList: List<TodaysPollenScreenItemData>) {
    Column {
        Text(
            text = "No pollen data available for the following allergens:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
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
    Column {
        AsyncImage(
            model = itemData.pollenType,
            contentDescription = "No pollen data available",
            modifier = Modifier.size(100.dp).clip(CircleShape),
        )

        Text(
            text = "No pollen data available for today.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TodaysPollenScreenPreview() {
    PollenTrackerTheme {
        TodaysPollenScreen(
            itemDataList =
                listOf(
                    TodaysPollenScreenItemData(
                        pollenType = "https://example.com/birch.png",
                        pollenCount = 150,
                        pollenLevel = "High",
                        pollenName = "Birch",
                    ),
                    TodaysPollenScreenItemData(
                        pollenType = "https://example.com/birch.png",
                        pollenCount = 150,
                        pollenLevel = "High",
                        pollenName = "Birch",
                    ),
                    TodaysPollenScreenItemData(
                        pollenType = "https://example.com/birch.png",
                        pollenCount = 150,
                        pollenLevel = "High",
                        pollenName = "Birch",
                    ),
                    TodaysPollenScreenItemData(
                        pollenType = "https://example.com/birch.png",
                        pollenCount = 150,
                        pollenLevel = "High",
                        pollenName = "Birch",
                    ),
                    TodaysPollenScreenItemData(
                        pollenType = "https://example.com/birch.png",
                        pollenCount = 150,
                        pollenLevel = "High",
                        pollenName = "Birch",
                    ),
                    TodaysPollenScreenItemData(
                        pollenType = "https://example.com/birch.png",
                        pollenCount = 150,
                        pollenLevel = "High",
                        pollenName = "Birch",
                    ),
                    TodaysPollenScreenItemData(
                        pollenType = "https://example.com/birch.png",
                        pollenCount = 0,
                        pollenLevel = "High",
                        pollenName = "Birch",
                    ),
                    TodaysPollenScreenItemData(
                        pollenType = "https://example.com/birch.png",
                        pollenCount = 0,
                        pollenLevel = "High",
                        pollenName = "Birch",
                    ),
                    // ... more mock data
                ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
