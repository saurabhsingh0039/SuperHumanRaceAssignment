package com.saurabharunima.superhumanraceassignment.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saurabharunima.superhumanraceassignment.domain.DistanceFormatter
import com.saurabharunima.superhumanraceassignment.ui.theme.LocalDistanceUnit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.data.model.Profile
import com.saurabharunima.superhumanraceassignment.data.model.Report
import com.saurabharunima.superhumanraceassignment.data.model.ReportPeriod
import com.saurabharunima.superhumanraceassignment.ui.components.PillButton
import com.saurabharunima.superhumanraceassignment.ui.components.SegmentedToggle
import com.saurabharunima.superhumanraceassignment.ui.components.StatCard
import com.saurabharunima.superhumanraceassignment.ui.components.StatusBarIcons
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme
import com.saurabharunima.superhumanraceassignment.ui.theme.Blue700

@Composable
fun ProfileRoute(
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Returning from Settings may have changed the name.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshProfileSilently() }
    ProfileScreen(
        state = state,
        onOpenSettings = onOpenSettings,
        onLogout = onLogout,
        onRetryProfile = viewModel::loadProfile,
        onRetryReport = viewModel::loadReport,
        onSelectTab = viewModel::selectTab,
        onSelectPeriod = viewModel::selectPeriod,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    onRetryProfile: () -> Unit,
    onRetryReport: () -> Unit,
    onSelectTab: (ProfileTab) -> Unit,
    onSelectPeriod: (ReportPeriod) -> Unit,
) {
    StatusBarIcons(lightBackground = true)

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Profile", style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        when (val profile = state.profile) {
            UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Column(
                Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Couldn't load your profile", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Text(profile.message, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.muted, textAlign = TextAlign.Center)
                PillButton("Retry", onRetryProfile)
            }
            is UiState.Success -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Hi, ${profile.data.user.name}", style = MaterialTheme.typography.titleMedium, color = AppTheme.colors.muted)
                SegmentedToggle(
                    options = ProfileTab.entries.map { it.label },
                    selectedIndex = state.tab.ordinal,
                    onSelect = { onSelectTab(ProfileTab.entries[it]) },
                )
                when (state.tab) {
                    ProfileTab.PERFORMANCE -> PerformanceContent(profile.data)
                    ProfileTab.IMPACT -> ImpactContent(state, onSelectPeriod, onRetryReport)
                }
                TextButton(onClick = onLogout, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Log out", style = MaterialTheme.typography.labelLarge, color = AppTheme.colors.muted)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PerformanceContent(profile: Profile) {
    var showMarketplace by rememberSaveable { mutableStateOf(false) }
    val stats = profile.stats

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("${stats.streakDays}", if (stats.streakDays == 1) "Day" else "Days", Icons.Filled.CheckCircle, AppTheme.colors.leaf, Modifier.weight(1f))
        StatCard("${stats.xp}", "Points", Icons.Filled.Star, Blue700, Modifier.weight(1f))
        StatCard("${stats.seeds}", "Seeds", Icons.Filled.Favorite, AppTheme.colors.gold, Modifier.weight(1f))
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = AppTheme.colors.gem, modifier = Modifier.size(40.dp))
                Column {
                    Text("GEMS", style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.muted)
                    Text("${stats.gems}", style = MaterialTheme.typography.headlineMedium)
                }
            }
            PillButton("MARKETPLACE", { showMarketplace = true }, compact = true)
        }
    }

    if (showMarketplace) {
        ModalBottomSheet(onDismissRequest = { showMarketplace = false }) {
            Column(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Marketplace", style = MaterialTheme.typography.titleLarge)
                Text("Spend your gems on rewards. Coming soon.", style = MaterialTheme.typography.bodyLarge)
                PillButton("Close", { showMarketplace = false }, Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImpactContent(
    state: ProfileState,
    onSelectPeriod: (ReportPeriod) -> Unit,
    onRetry: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReportPeriod.entries.forEach { period ->
            FilterChip(
                selected = period == state.period,
                onClick = { onSelectPeriod(period) },
                label = { Text(if (period == ReportPeriod.WEEKLY) "Weekly" else "Monthly") },
            )
        }
    }

    when (val report = state.report) {
        UiState.Loading -> Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is UiState.Error -> Column(
            Modifier.fillMaxWidth().padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Couldn't load your impact report", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(report.message, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.muted, textAlign = TextAlign.Center)
            PillButton("Retry", onRetry)
        }
        is UiState.Success -> ReportContent(report.data)
    }
}

@Composable
private fun ReportContent(report: Report) {
    val totalKm = report.modeUsage.sumOf { it.km }
    val hasData = report.modeUsage.isNotEmpty()

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("%.1f kg".format(report.totalSavedKg), "CO₂e saved", Icons.Filled.Done, AppTheme.colors.leaf, Modifier.weight(1f))
        StatCard("%.1f kg".format(report.totalEmittedKg), "CO₂e emitted", Icons.Filled.Warning, AppTheme.colors.gold, Modifier.weight(1f))
        StatCard(DistanceFormatter.short(totalKm, LocalDistanceUnit.current), "Distance", Icons.Filled.Place, Blue700, Modifier.weight(1f))
    }

    if (!hasData) {
        Text(
            "No trips logged in this period yet.",
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        )
        return
    }

    ChartCard("CO₂e saved per day") { DailyCo2BarChart(report.daily) }
    ChartCard("Transport usage") { TransportUsageChart(report.modeUsage) }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
