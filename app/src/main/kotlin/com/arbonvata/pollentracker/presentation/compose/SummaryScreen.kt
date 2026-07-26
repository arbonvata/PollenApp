package com.arbonvata.pollentracker.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.arbonvata.pollentracker.presentation.viewmodel.UserSettingsViewModel

@Composable
fun SummaryScreen(
    modifier: Modifier = Modifier,
    userSettingsViewModel: UserSettingsViewModel = hiltViewModel(),
    onNavigateNext: () -> Unit,
) {
    val data = userSettingsViewModel.userSettingsState.collectAsState()

    LaunchedEffect(Unit) {
        userSettingsViewModel.loadInitData()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(
            text = "Summary Screen",
            style = MaterialTheme.typography.headlineMedium,
            fontStyle = FontStyle.Normal,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Location ID: ${data.value?.regionId ?: "Not set"}",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selected Allergens:",
            style = MaterialTheme.typography.titleMedium,
        )

        val allergensText = data.value?.allergyNames?.joinToString(", ") ?: "None"
        Text(
            text = allergensText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateNext,
        ) {
            Text("OK")
        }
    }
}
