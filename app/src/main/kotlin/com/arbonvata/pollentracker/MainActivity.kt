package com.arbonvata.pollentracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arbonvata.pollentracker.presentation.compose.AllergyListScreen
import com.arbonvata.pollentracker.presentation.compose.LocationChooserScreen
import com.arbonvata.pollentracker.presentation.compose.SummaryScreen
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@Serializable
object LocationChooser

@Serializable
object Summary

@Serializable
object AllergyList

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PollenTrackerTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = LocationChooser,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable<LocationChooser> {
                            LocationChooserScreen(
                                onNavigateNext = {
                                    navController.navigate(AllergyList)
                                },
                            )
                        }
                        composable<Summary> {
                            SummaryScreen()
                        }
                        composable<AllergyList> {
                            AllergyListScreen(
                                onNavigateNext = {
                                    navController.navigate(Summary)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
