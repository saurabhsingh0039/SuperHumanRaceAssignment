package com.saurabharunima.superhumanraceassignment.ui.home

import com.saurabharunima.superhumanraceassignment.domain.DistanceFormatter
import com.saurabharunima.superhumanraceassignment.ui.theme.LocalDistanceUnit
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.data.model.Activity
import com.saurabharunima.superhumanraceassignment.data.model.ActivityType
import com.saurabharunima.superhumanraceassignment.ui.components.ActivityAction
import com.saurabharunima.superhumanraceassignment.ui.components.ActivityCard
import com.saurabharunima.superhumanraceassignment.ui.components.CurrencyChip
import com.saurabharunima.superhumanraceassignment.ui.components.GradientHeader
import com.saurabharunima.superhumanraceassignment.ui.components.PillButton
import com.saurabharunima.superhumanraceassignment.ui.components.PillStyle
import com.saurabharunima.superhumanraceassignment.ui.components.StatusBarIcons
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme
import com.saurabharunima.superhumanraceassignment.ui.theme.Blue700

@Composable
fun HomeRoute(
    onOpenMap: () -> Unit,
    onOpenChat: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onRetry = viewModel::refresh,
        onActivityClick = viewModel::onActivityClick,
        onStart = viewModel::onStart,
        onDismissSheet = viewModel::onDismissSheet,
        onComplete = viewModel::onComplete,
        onMessageShown = viewModel::onMessageShown,
        onOpenMap = onOpenMap,
        onOpenChat = onOpenChat,
    )
}

@Composable
fun HomeScreen(
    state: HomeState,
    onRetry: () -> Unit,
    onActivityClick: (Activity) -> Unit,
    onStart: () -> Unit,
    onDismissSheet: () -> Unit,
    onComplete: (Activity) -> Unit,
    onMessageShown: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenChat: () -> Unit,
) {
    val content = state.content
    // Light icons only when the blue header is on screen.
    StatusBarIcons(lightBackground = content !is UiState.Success)

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            onMessageShown()
        }
    }

    Box(Modifier.fillMaxSize()) {
        when (content) {
            UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> ErrorState(content.message, onRetry)
            is UiState.Success -> HomeContent(
                data = content.data,
                mobility = state.mobility,
                onActivityClick = onActivityClick,
                onStart = onStart,
                onOpenMap = onOpenMap,
                onOpenChat = onOpenChat,
            )
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp))
        state.sheet?.let { ActivitySheet(it, onDismissSheet, onComplete) }
    }
}

@Composable
private fun HomeContent(
    data: HomeData,
    mobility: UiState<MobilitySummary>,
    onActivityClick: (Activity) -> Unit,
    onStart: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenChat: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        GradientHeader {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenChat) {
                    Icon(Icons.Filled.MailOutline, contentDescription = "Ask Ora", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurrencyChip(data.stats.seeds, "seeds", Icons.Filled.Favorite, AppTheme.colors.gold)
                    CurrencyChip(
                        data.stats.streakDays, "streak days", Icons.Filled.CheckCircle,
                        if (data.stats.streakDays > 0) AppTheme.colors.leaf else AppTheme.colors.muted,
                    )
                    CurrencyChip(data.stats.gems, "gems", Icons.Filled.Star, AppTheme.colors.gem)
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Mascot placeholder: swap for the real illustration asset.
                Box(
                    Modifier.size(80.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Face, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (data.completedToday) "Great job! Come back tomorrow to keep your streak going."
                        else "Complete an activity and start with the right streak!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    if (!data.completedToday) {
                        PillButton("START", onStart, style = PillStyle.Coral, compact = true)
                    }
                }
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StreakCard(data)

            Text("Today's activities", style = MaterialTheme.typography.titleLarge)
            data.activities.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { activity ->
                        ActivityCard(
                            title = activity.title,
                            icon = activity.type.icon(),
                            action = actionFor(activity, mobility),
                            onClick = { onActivityClick(activity) },
                            modifier = Modifier.weight(1f),
                            showBadge = activity.isNew,
                            iconTint = activity.type.tint(),
                            artwork = {
                                ActivityArt(activity.type.icon(), activity.type.tint())
                                if (activity.type == ActivityType.MOBILITY) MobilityStats(mobility)
                            },
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Text("Other Activities", style = MaterialTheme.typography.titleLarge)
            ShortcutCard(
                title = "Friends nearby",
                subtitle = "See active friends on the map",
                icon = Icons.Filled.Place,
                onClick = onOpenMap,
            )
            Spacer(Modifier.size(8.dp))
        }
    }
}

private fun actionFor(activity: Activity, mobility: UiState<MobilitySummary>): ActivityAction = when {
    activity.completed -> ActivityAction.Done
    activity.type == ActivityType.MOBILITY ->
        if (mobility is UiState.Loading) ActivityAction.Loading else ActivityAction.None
    activity.needsActivation -> ActivityAction.Activate
    activity.xpReward > 0 -> ActivityAction.Xp(activity.xpReward)
    else -> ActivityAction.None
}

// Core-set icons are stand-ins; replace with the real activity vectors later.
private fun ActivityType.icon(): ImageVector = when (this) {
    ActivityType.STORIES -> Icons.Filled.Create
    ActivityType.QUIZ -> Icons.Filled.Info
    ActivityType.MOBILITY -> Icons.Filled.Place
    ActivityType.WELLNESS -> Icons.Filled.Favorite
    ActivityType.ROUTINE -> Icons.Filled.DateRange
    ActivityType.INVITE -> Icons.Filled.AddCircle
}

@Composable
private fun ActivityType.tint(): Color = when (this) {
    ActivityType.STORIES -> AppTheme.colors.gold
    ActivityType.QUIZ -> Blue700
    ActivityType.MOBILITY, ActivityType.WELLNESS -> AppTheme.colors.gem
    ActivityType.ROUTINE, ActivityType.INVITE -> AppTheme.colors.leaf
}

@Composable
private fun BoxScope.ActivityArt(icon: ImageVector, tint: Color) {
    Icon(
        icon, contentDescription = null,
        tint = tint.copy(alpha = 0.12f),
        modifier = Modifier.align(Alignment.Center).size(72.dp),
    )
}

@Composable
private fun BoxScope.MobilityStats(mobility: UiState<MobilitySummary>) {
    Column(Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 14.dp, end = 12.dp)) {
        when (mobility) {
            UiState.Loading -> Unit
            is UiState.Error -> Text(
                "Couldn't load. Tap to retry.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
            is UiState.Success -> {
                Text(DistanceFormatter.short(mobility.data.km, LocalDistanceUnit.current), style = MaterialTheme.typography.titleMedium)
                Text(
                    "%.1f kg CO₂e saved".format(mobility.data.savedKg),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.muted,
                )
            }
        }
    }
}

@Composable
private fun StreakCard(data: HomeData) {
    val streak = data.stats.streakDays
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(AppTheme.colors.iconTile),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.CheckCircle, null, tint = AppTheme.colors.leaf, modifier = Modifier.size(28.dp))
                }
                Column {
                    Text(
                        if (data.completedToday) "Streak: $streak ${if (streak == 1) "day" else "days"}"
                        else "Activate your streak!",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "Come back every day to increase your streak days!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.muted,
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                data.week.forEach { DayCircleView(it) }
            }
        }
    }
}

@Composable
private fun DayCircleView(day: DayCircle) {
    val description = buildString {
        append(day.label)
        if (day.isToday) append(", today")
        if (day.done) append(", completed")
    }
    Column(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            day.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (day.isToday) Blue700 else AppTheme.colors.muted,
        )
        val base = Modifier.size(32.dp).clip(CircleShape)
        Box(
            modifier = when {
                day.done -> base.background(AppTheme.colors.leaf)
                day.isToday -> base.background(AppTheme.colors.iconTile).border(2.dp, Blue700, CircleShape)
                else -> base.background(AppTheme.colors.iconTile)
            },
            contentAlignment = Alignment.Center,
        ) {
            if (day.done) Icon(Icons.Filled.Done, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ShortcutCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.large
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(AppTheme.colors.iconTile),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = Blue700, modifier = Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.muted)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = AppTheme.colors.muted)
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Couldn't load your home screen", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.muted, textAlign = TextAlign.Center)
        PillButton("Retry", onRetry)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivitySheet(activity: Activity, onDismiss: () -> Unit, onComplete: (Activity) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(activity.title, style = MaterialTheme.typography.titleLarge)
            Text(
                if (activity.type == ActivityType.MOBILITY) "Detailed mobility tracking is coming soon."
                else "This activity is coming soon. In this demo you can mark it as done to see your XP and streak update.",
                style = MaterialTheme.typography.bodyLarge,
            )
            if (activity.xpReward > 0 && !activity.completed) {
                PillButton("Mark as done (+${activity.xpReward} XP)", { onComplete(activity) }, Modifier.fillMaxWidth())
            }
            PillButton("Close", onDismiss, Modifier.fillMaxWidth(), PillStyle.Light)
        }
    }
}
