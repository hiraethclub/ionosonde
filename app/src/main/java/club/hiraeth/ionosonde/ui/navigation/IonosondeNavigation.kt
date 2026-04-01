package club.hiraeth.ionosonde.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Flare
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import club.hiraeth.ionosonde.ui.about.AboutScreen
import club.hiraeth.ionosonde.ui.aurora.AuroraScreen
import club.hiraeth.ionosonde.ui.dashboard.DashboardScreen
import club.hiraeth.ionosonde.ui.kindex.KIndexScreen
import club.hiraeth.ionosonde.ui.map.SunlitMapScreen
import club.hiraeth.ionosonde.ui.settings.SettingsScreen
import club.hiraeth.ionosonde.ui.sfi.SfiScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object KIndex : Screen("kindex", "K-Index", Icons.Default.ShowChart)
    data object Sfi : Screen("sfi", "SFI", Icons.Default.Timeline)
    data object Aurora : Screen("aurora", "Aurora", Icons.Outlined.Flare)
    data object Map : Screen("map", "Map", Icons.Default.Map)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object About : Screen("about", "About", Icons.Default.Info)
}

private val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.KIndex,
    Screen.Sfi,
    Screen.Aurora,
    Screen.Map,
)

@Composable
fun IonosondeNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                )
            }
            composable(Screen.KIndex.route) { KIndexScreen() }
            composable(Screen.Sfi.route) { SfiScreen() }
            composable(Screen.Aurora.route) { AuroraScreen() }
            composable(Screen.Map.route) { SunlitMapScreen() }
            composable(Screen.Settings.route) { SettingsScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.About.route) { AboutScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
