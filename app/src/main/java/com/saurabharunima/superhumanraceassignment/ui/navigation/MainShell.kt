package com.saurabharunima.superhumanraceassignment.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.saurabharunima.superhumanraceassignment.ui.chat.ChatRoute
import com.saurabharunima.superhumanraceassignment.ui.components.FloatingBottomBar
import com.saurabharunima.superhumanraceassignment.ui.home.HomeRoute
import com.saurabharunima.superhumanraceassignment.ui.leaderboard.LeaderboardRoute
import com.saurabharunima.superhumanraceassignment.ui.map.MapRoute
import com.saurabharunima.superhumanraceassignment.ui.missions.MissionsRoute
import com.saurabharunima.superhumanraceassignment.ui.profile.ProfileRoute
import com.saurabharunima.superhumanraceassignment.ui.settings.SettingsRoute

/**
 * Signed-in shell: 4 tabs plus pushed screens. Only the bottom inset is applied here so tab
 * screens can draw their gradient header under the status bar.
 */
@Composable
fun MainShell(onLogout: () -> Unit) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBar = BottomTabs.any { it.route == route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBar) {
                FloatingBottomBar(
                    items = BottomTabs,
                    selectedRoute = route,
                    onSelect = { nav.navigateToTab(it.route) },
                )
            }
        },
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(bottom = inner.calculateBottomPadding()),
        ) {
            // Replace each placeholder as its phase lands.
            composable(Routes.HOME) {
                HomeRoute(
                    onOpenMap = { nav.navigate(Routes.MAP) },
                    onOpenChat = { nav.navigate(Routes.CHAT) },
                )
            }
            composable(Routes.MISSIONS) { MissionsRoute() }
            composable(Routes.LEADERBOARD) { LeaderboardRoute() }
            composable(Routes.PROFILE) {
                ProfileRoute(
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    onLogout = onLogout,
                )
            }
            composable(Routes.SETTINGS) { SettingsRoute(onBack = { nav.popBackStack() }, onLogout = onLogout) }
            composable(Routes.MAP) { MapRoute(onBack = { nav.popBackStack() }) }
            composable(Routes.CHAT) { ChatRoute(onBack = { nav.popBackStack() }) }
        }
    }
}
