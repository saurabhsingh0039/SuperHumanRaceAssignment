package com.saurabharunima.superhumanraceassignment.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme
import com.saurabharunima.superhumanraceassignment.ui.theme.SuperHumanTheme

private val previewTabs = listOf(
    BottomBarItem("home", "Home", Icons.Filled.Home),
    BottomBarItem("missions", "Missions", Icons.Filled.CheckCircle),
    BottomBarItem("leaderboard", "Leaderboard", Icons.Filled.Star),
    BottomBarItem("profile", "Profile", Icons.Filled.Person),
)

@Composable
private fun ComponentsGallery() {
    var selected by remember { mutableStateOf("home") }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GradientHeader {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CurrencyChip(15, "seeds", Icons.Filled.Favorite, AppTheme.colors.gold)
                CurrencyChip(0, "leaves", Icons.Filled.Favorite, AppTheme.colors.muted)
                CurrencyChip(3, "gems", Icons.Filled.Star, AppTheme.colors.gem)
            }
        }
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActivityCard("Stories", Icons.Filled.Star, ActivityAction.Xp(20), {}, Modifier.weight(1f), showBadge = true)
            ActivityCard("Mobility", Icons.Filled.Home, ActivityAction.Loading, {}, Modifier.weight(1f))
        }
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActivityCard("Wellness", Icons.Filled.Favorite, ActivityAction.Activate, {}, Modifier.weight(1f))
            ActivityCard("Invite", Icons.Filled.Person, ActivityAction.Xp(200), {}, Modifier.weight(1f))
        }
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PillButton("Sign in with Email", {}, Modifier.fillMaxWidth(), PillStyle.Light, Icons.Filled.Person)
            PillButton("START", {}, Modifier.fillMaxWidth(), PillStyle.Coral)
            PillButton("MARKETPLACE", {}, Modifier.fillMaxWidth(), PillStyle.Primary)
        }
        FloatingBottomBar(previewTabs, selected, { selected = it.route })
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEF3FD, heightDp = 900)
@Composable
private fun GalleryLight() = SuperHumanTheme(darkTheme = false) { ComponentsGallery() }

@Preview(showBackground = true, backgroundColor = 0xFF0F1530, heightDp = 900)
@Composable
private fun GalleryDark() = SuperHumanTheme(darkTheme = true) { ComponentsGallery() }
