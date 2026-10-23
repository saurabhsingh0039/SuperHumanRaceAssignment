package com.saurabharunima.superhumanraceassignment.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saurabharunima.superhumanraceassignment.ui.components.PillButton
import com.saurabharunima.superhumanraceassignment.ui.components.PillStyle
import com.saurabharunima.superhumanraceassignment.ui.components.StatusBarIcons

/** Temporary screens so the shell is runnable now. Each is deleted when its real screen is built. */
@Composable
fun PlaceholderTab(
    title: String,
    lightBackground: Boolean,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    StatusBarIcons(lightBackground)
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        actions()
    }
}

@Composable
fun PlaceholderAction(text: String, onClick: () -> Unit) {
    PillButton(text, onClick, Modifier.fillMaxWidth(), PillStyle.Light, compact = true)
}

@Composable
fun PlaceholderPushed(title: String, onBack: () -> Unit) {
    StatusBarIcons(lightBackground = true)
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
