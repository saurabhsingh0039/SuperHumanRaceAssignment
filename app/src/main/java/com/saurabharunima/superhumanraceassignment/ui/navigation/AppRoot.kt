package com.saurabharunima.superhumanraceassignment.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.saurabharunima.superhumanraceassignment.ui.auth.LoginRoute
import com.saurabharunima.superhumanraceassignment.ui.auth.SignUpRoute
import com.saurabharunima.superhumanraceassignment.ui.session.SessionState
import com.saurabharunima.superhumanraceassignment.ui.session.SessionViewModel

/**
 * Auth gate. Each branch owns its own NavController, so logging out or in resets that
 * back stack and the user can never navigate back across the gate.
 */
@Composable
fun AppRoot(viewModel: SessionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (state) {
        SessionState.Loading -> SplashScreen()
        SessionState.SignedOut -> AuthNavHost()
        is SessionState.SignedIn -> MainShell(onLogout = { viewModel.logout() })
    }
}

@Composable
private fun SplashScreen() {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AuthNavHost() {
    val nav = rememberNavController()
    NavHost(nav, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) { LoginRoute(onGoToSignUp = { nav.navigate(Routes.SIGNUP) }) }
        composable(Routes.SIGNUP) { SignUpRoute(onBack = { nav.popBackStack() }) }
    }
}
