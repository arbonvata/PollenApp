package com.arbonvata.pollentracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arbonvata.pollentracker.presentation.compose.PollenTrackerApp
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PollenTrackerTheme {
                PollenTrackerApp()
            }
        }
    }
}
