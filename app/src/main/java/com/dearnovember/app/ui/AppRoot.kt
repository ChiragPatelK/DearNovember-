package com.dearnovember.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dearnovember.app.ui.home.HomeScreen
import com.dearnovember.app.ui.screens.*

private enum class NavTab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Home", Icons.Filled.Home),
    Journey("journey", "Journey", Icons.Filled.Favorite),
    Progress("progress", "Progress", Icons.Filled.Star),
    Journal("journal", "Journal", Icons.Filled.Edit),
    Profile("profile", "Profile", Icons.Filled.Person),
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val current by nav.currentBackStackEntryAsState()
    val open: (String) -> Unit = { route -> nav.navigate(route) { launchSingleTop = true } }
    val back: () -> Unit = { nav.popBackStack() }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = current?.destination?.route == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { pad ->
        NavHost(nav, startDestination = NavTab.Home.route, modifier = Modifier.padding(pad)) {
            composable("home") { HomeScreen(onOpen = open) }
            composable("journey") { JourneyScreen(onOpen = open) }
            composable("progress") { ProgressScreen() }
            composable("journal") { JournalScreen() }
            composable("profile") { ProfileScreen() }
            composable("physical") { PhysicalScreen(back) }
            composable("academics") { AcademicsScreen(back, open) }
            composable("subject/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                SubjectScreen(e.arguments?.getLong("id") ?: 0L, back)
            }
            composable("mental") { MentalScreen(back, open) }
            composable("speaking") { SpeakingScreen(back) }
            composable("discipline") { DisciplineScreen(back) }
            composable("intellect") { IntellectScreen(back) }
        }
    }
}
