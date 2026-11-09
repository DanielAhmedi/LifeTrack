package com.example.lifetrack.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lifetrack.ui.screens.achievements.AchievementsScreen
import com.example.lifetrack.ui.screens.calendar.CalendarScreen
import com.example.lifetrack.ui.screens.home.HomeScreen
import com.example.lifetrack.ui.screens.settings.SettingsScreen
import com.example.lifetrack.ui.screens.statistics.StatisticsScreen

@Composable
fun LifeTrackApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentItem = BottomNavItem.fromRoute(backStackEntry?.destination?.route)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (currentItem != null) {
                NavigationBar {
                    BottomNavItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = item == currentItem,
                            onClick = { navController.navigateToBottomNavItem(item) },
                            icon = {},
                            label = { Text(text = stringResource(item.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LifeTrackNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun LifeTrackNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val openSettings = { navController.navigate(Routes.SETTINGS) }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Routes.HOME) {
            HomeScreen(onOpenSettings = openSettings)
        }
        composable(Routes.CALENDAR) {
            CalendarScreen(onOpenSettings = openSettings)
        }
        composable(Routes.STATISTICS) {
            StatisticsScreen(onOpenSettings = openSettings)
        }
        composable(Routes.ACHIEVEMENTS) {
            AchievementsScreen(onOpenSettings = openSettings)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun NavHostController.navigateToBottomNavItem(item: BottomNavItem) {
    navigate(item.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}