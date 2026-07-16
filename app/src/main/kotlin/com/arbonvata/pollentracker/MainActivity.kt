package com.arbonvata.pollentracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.arbonvata.pollentracker.presentation.PollenTrackerViewModel
import com.arbonvata.pollentracker.presentation.compose.AllergyListScreen
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: PollenTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PollenTrackerTheme {
                AllergyListScreen(pollenTrackerViewModel = viewModel)
            }
        }
    }
}
