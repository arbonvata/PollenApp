package com.arbonvata.pollentracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arbonvata.pollentracker.presentation.compose.AllergyListScreen
import com.arbonvata.pollentracker.presentation.compose.HistoryMpChartScreen
import com.arbonvata.pollentracker.presentation.compose.HistoryScreen
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

@Serializable
object History

@Serializable
object HistoryMpChart

@Composable
fun PollenTrackerNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = LocationChooser,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
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
                    // Clear the setup flow from the back stack and land on TodaysPollen as the root destination
                    navController.navigate(TodaysPollen) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<TodaysPollen> {
            TodaysPollenScreen(
                onNavigateNext = {},
                onNavigateHistory = {
                    navController.navigate(History)
                },
            )
        }
        composable<History> {
            HistoryScreen(
                onSwitchChart = {
                    navController.navigate(HistoryMpChart)
                },
            )
        }
        composable<HistoryMpChart> {
            HistoryMpChartScreen(
                onSwitchChart = {
                    navController.popBackStack()
                },
            )
        }
    }
}
