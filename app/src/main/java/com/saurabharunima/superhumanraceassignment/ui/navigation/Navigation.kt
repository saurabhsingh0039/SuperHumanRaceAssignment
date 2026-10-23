package com.saurabharunima.superhumanraceassignment.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.saurabharunima.superhumanraceassignment.ui.components.BottomBarItem

object Routes {
    // Auth graph
    const val LOGIN = "login"
    const val SIGNUP = "signup"

    // Tabs
    const val HOME = "home"
    const val MISSIONS = "missions"
    const val LEADERBOARD = "leaderboard"
    const val PROFILE = "profile"

    // Pushed screens (no bottom bar)
    const val SETTINGS = "settings"
    const val MAP = "map"
    const val CHAT = "chat"
}

/** Icons are core-set stand-ins; swap for the real trophy/flag vectors later. */
val BottomTabs = listOf(
    BottomBarItem(Routes.HOME, "Home", Icons.Filled.Home),
    BottomBarItem(Routes.MISSIONS, "Missions", Icons.Filled.CheckCircle),
    BottomBarItem(Routes.LEADERBOARD, "Leaderboard", Icons.Filled.Star),
    BottomBarItem(Routes.PROFILE, "Profile", Icons.Filled.Person),
)

fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
