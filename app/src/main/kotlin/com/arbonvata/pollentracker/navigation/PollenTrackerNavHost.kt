package com.arbonvata.pollentracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arbonvata.pollentracker.presentation.compose.AllergyListScreen
import com.arbonvata.pollentracker.presentation.compose.LocationChooserScreen
import com.arbonvata.pollentracker.presentation.compose.SummaryScreen
import com.arbonvata.pollentracker.presentation.compose.TodaysPollenScreen
import kotlinx.serialization.Serializable

@Serializable
object LocationChooser

@Serializable
object Summary

@Serializable
object AllergyList

@Serializable
object TodaysPollen

@Composable
fun PollenTrackerNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = LocationChooser,
        modifier = modifier,
    ) {
        composable<LocationChooser> {
            LocationChooserScreen(
                onNavigateNext = {
                    navController.navigate(AllergyList)
                },
            )
        }
        composable<AllergyList> {
            AllergyListScreen(
                onNavigateNext = {
                    navController.navigate(Summary)
                },
            )
        }
        composable<Summary> {
            SummaryScreen(
                onNavigateNext = {
                    navController.navigate(TodaysPollen)
                },
            )
        }
        composable<TodaysPollen> {
            TodaysPollenScreen(
                onNavigateNext = {},
            )
        }
    }
}
