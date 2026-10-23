package com.saurabharunima.superhumanraceassignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabharunima.superhumanraceassignment.data.model.ThemeMode
import com.saurabharunima.superhumanraceassignment.ui.navigation.AppRoot
import com.saurabharunima.superhumanraceassignment.ui.settings.AppSettingsViewModel
import com.saurabharunima.superhumanraceassignment.ui.theme.LocalDistanceUnit
import com.saurabharunima.superhumanraceassignment.ui.theme.SuperHumanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appSettings: AppSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by appSettings.settings.collectAsStateWithLifecycle()
            val dark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CompositionLocalProvider(LocalDistanceUnit provides settings.distanceUnit) {
                SuperHumanTheme(darkTheme = dark) { AppRoot() }
            }
        }
    }
}
