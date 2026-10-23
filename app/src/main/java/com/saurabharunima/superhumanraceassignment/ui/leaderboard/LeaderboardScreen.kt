package com.saurabharunima.superhumanraceassignment.ui.leaderboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.data.model.LeaderEntry
import com.saurabharunima.superhumanraceassignment.ui.components.CurrencyChip
import com.saurabharunima.superhumanraceassignment.ui.components.GradientHeader
import com.saurabharunima.superhumanraceassignment.ui.components.InitialsAvatar
import com.saurabharunima.superhumanraceassignment.ui.components.PillButton
import com.saurabharunima.superhumanraceassignment.ui.components.StatusBarIcons
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme
import com.saurabharunima.superhumanraceassignment.ui.theme.Blue700

@Composable
fun LeaderboardRoute(viewModel: LeaderboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LeaderboardScreen(
        state = state,
        onRetry = viewModel::load,
        onToggleFollow = viewModel::toggleFollow,
        onShowInfo = viewModel::showInfo,
    )
}

@Composable
fun LeaderboardScreen(
    state: LeaderboardState,
    onRetry: () -> Unit,
    onToggleFollow: (String) -> Unit,
    onShowInfo: (Boolean) -> Unit,
) {
    val content = state.content
    StatusBarIcons(lightBackground = content !is UiState.Success)

    Box(Modifier.fillMaxSize()) {
        when (content) {
            UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Column(
                Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Couldn't load the leaderboard", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Text(content.message, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.muted, textAlign = TextAlign.Center)
                PillButton("Retry", onRetry)
            }
            is UiState.Success -> LeaderboardContent(content.data, state.following, onToggleFollow, onShowInfo)
        }
        if (state.showInfo && content is UiState.Success) {
            InfoSheet(content.data.advanceTop) { onShowInfo(false) }
        }
    }
}

@Composable
private fun LeaderboardContent(
    data: LeaderboardData,
    following: Set<String>,
    onToggleFollow: (String) -> Unit,
    onShowInfo: (Boolean) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)) {
        item {
            GradientHeader {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    CurrencyChip(data.stats.seeds, "seeds", Icons.Filled.Favorite, AppTheme.colors.gold)
                    CurrencyChip(data.stats.streakDays, "streak days", Icons.Filled.CheckCircle, AppTheme.colors.leaf)
                    CurrencyChip(data.stats.gems, "gems", Icons.Filled.Star, AppTheme.colors.gem)
                }
                LeagueBadges()
                Column(
                    Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(data.leagueName, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text(
                        "The top ${data.advanceTop} advance to the next League",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = { onShowInfo(true) }) {
                        Text("How does it work?", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Surface(shape = CircleShape, color = Color.White) {
                        Text(
                            "Ends in ${data.timeLeft}",
                            style = MaterialTheme.typography.labelLarge,
                            color = Blue700,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Position", style = MaterialTheme.typography.labelLarge, color = AppTheme.colors.muted)
                Text("Points", style = MaterialTheme.typography.labelLarge, color = AppTheme.colors.muted)
            }
        }

        if (data.entries.isEmpty()) {
            item {
                Text(
                    "No one is in this league yet. Complete an activity to get on the board.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                )
            }
        } else {
            items(data.entries, key = { it.userId }) { entry ->
                LeaderRow(
                    entry = entry,
                    isFollowing = entry.userId in following,
                    onToggleFollow = { onToggleFollow(entry.userId) },
                )
            }
        }
    }
}

@Composable
private fun LeagueBadges() {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp)
            .semantics(mergeDescendants = true) { contentDescription = "League 1 of 5 unlocked" },
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        repeat(5) { i ->
            val unlocked = i == 0
            Box(
                Modifier.size(52.dp).clip(CircleShape)
                    .background(if (unlocked) Color(0xFFF3D9A8) else Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (unlocked) Icons.Filled.Star else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = if (unlocked) Color(0xFF8A5A1E) else Blue700,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

@Composable
private fun LeaderRow(entry: LeaderEntry, isFollowing: Boolean, onToggleFollow: () -> Unit) {
    val description = buildString {
        append("Rank ${entry.rank}, ${entry.name}")
        if (entry.isCurrentUser) append(", you")
        append(", ${entry.xp} XP, ${entry.gems} gems reward")
    }
    val highlight = if (entry.isCurrentUser) AppTheme.colors.iconTile else MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = highlight,
        border = BorderStroke(1.dp, if (entry.isCurrentUser) Blue700 else AppTheme.colors.cardBorder),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                .semantics(mergeDescendants = true) { contentDescription = description },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                entry.rank.toString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(28.dp),
            )
            InitialsAvatar(entry.name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (entry.isCurrentUser) "${entry.name} (you)" else entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = AppTheme.colors.gem, modifier = Modifier.size(18.dp))
                    Text(" +${entry.gems}", style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.muted)
                    if (!entry.isCurrentUser) {
                        TextButton(onClick = onToggleFollow) {
                            Text(
                                if (isFollowing) "Following" else "Follow",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isFollowing) AppTheme.colors.muted else Blue700,
                            )
                        }
                    }
                }
            }
            Text("${entry.xp} XP", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoSheet(advanceTop: Int, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("How leagues work", style = MaterialTheme.typography.titleLarge)
            Text(
                "Earn XP by completing activities. When the timer ends, the top $advanceTop players move up to the next league. Ties share the same rank.",
                style = MaterialTheme.typography.bodyLarge,
            )
            PillButton("Got it", onDismiss, Modifier.fillMaxWidth())
        }
    }
}
