package com.arbonvata.pollentracker.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.arbonvata.pollentracker.R
import com.arbonvata.pollentracker.navigation.AllergyList
import com.arbonvata.pollentracker.navigation.LocationChooser
import com.arbonvata.pollentracker.navigation.PollenTrackerNavHost
import com.arbonvata.pollentracker.navigation.TodaysPollen
import com.arbonvata.pollentracker.presentation.viewmodel.UserSettingsViewModel
import com.arbonvata.pollentracker.ui.theme.PollenTrackerTheme
import kotlinx.coroutines.launch

@Composable
fun PollenTrackerApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: UserSettingsViewModel = hiltViewModel(),
) {
    val userSettings by viewModel.userSettingsState.collectAsState()

    // Pick the start destination exactly once, from the first settings read;
    // later settings changes must not rebuild the nav graph.
    var startDestination by remember { mutableStateOf<Any?>(null) }
    LaunchedEffect(userSettings) {
        val settings = userSettings
        if (startDestination == null && settings != null) {
            startDestination = if (settings.isSetupComplete) TodaysPollen else LocationChooser
        }
    }

    val start = startDestination
    if (start == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination

        fun openFromDrawer(route: Any) {
            // No saveState/restoreState here: this app's start destination is itself a
            // drawer target, and the save/restore machinery misbehaves in that case
            // (restore branch fires with an empty saved stack and the navigation
            // becomes a no-op). Plain popUpTo + singleTop is deterministic.
            navController.navigate(
                route,
                navOptions {
                    popUpTo(navController.graph.findStartDestination().id)
                    launchSingleTop = true
                },
            )
        }

        PollenTrackerAppScaffold(
            isPlaceSelected = currentDestination?.hasRoute(LocationChooser::class) == true,
            isAllergensSelected = currentDestination?.hasRoute(AllergyList::class) == true,
            onChoosePlace = { openFromDrawer(LocationChooser) },
            onChooseAllergens = { openFromDrawer(AllergyList) },
            modifier = modifier,
        ) { innerPadding ->
            PollenTrackerNavHost(
                navController = navController,
                startDestination = start,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PollenTrackerAppScaffold(
    isPlaceSelected: Boolean,
    isAllergensSelected: Boolean,
    onChoosePlace: () -> Unit,
    onChooseAllergens: () -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    content: @Composable (PaddingValues) -> Unit,
) {
    val scope = rememberCoroutineScope()

    fun onDrawerItemClick(action: () -> Unit) {
        scope.launch { drawerState.close() }
        action()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                )
                HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                DrawerItem(
                    label = stringResource(R.string.drawer_favorite_place),
                    icon = Icons.Filled.Place,
                    selected = isPlaceSelected,
                    onClick = { onDrawerItemClick(onChoosePlace) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                )
                DrawerItem(
                    label = stringResource(R.string.drawer_allergens),
                    icon = Icons.Filled.Checklist,
                    selected = isAllergensSelected,
                    onClick = { onDrawerItemClick(onChooseAllergens) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                )
            }
        },
        modifier = modifier.fillMaxSize(),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = stringResource(R.string.cd_open_navigation_drawer),
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationDrawerItem(
        label = { Text(label) },
        icon = { Icon(imageVector = icon, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun PollenTrackerAppPreview() {
    PollenTrackerTheme {
        PollenTrackerAppScaffold(
            isPlaceSelected = true,
            isAllergensSelected = false,
            onChoosePlace = {},
            onChooseAllergens = {},
        ) { innerPadding ->
            PreviewContent(innerPadding)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PollenTrackerAppDrawerOpenPreview() {
    PollenTrackerTheme {
        PollenTrackerAppScaffold(
            drawerState = rememberDrawerState(DrawerValue.Open),
            isPlaceSelected = true,
            isAllergensSelected = false,
            onChoosePlace = {},
            onChooseAllergens = {},
        ) { innerPadding ->
            PreviewContent(innerPadding)
        }
    }
}

@Composable
private fun PreviewContent(innerPadding: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Screen content")
    }
}
