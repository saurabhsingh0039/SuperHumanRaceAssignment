package com.saurabharunima.superhumanraceassignment.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Edge-to-edge screens must pick status-bar icon colour per screen:
 * dark icons over light backgrounds, light icons over the blue header.
 */
@Composable
fun StatusBarIcons(lightBackground: Boolean) {
    val view = LocalView.current
    // Dark icons only make sense over a light background; a dark theme flips them to light.
    val darkIcons = lightBackground && MaterialTheme.colorScheme.background.luminance() > 0.5f
    if (view.isInEditMode) return
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
