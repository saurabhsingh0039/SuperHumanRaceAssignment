package com.saurabharunima.superhumanraceassignment.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.data.model.AppSettings
import com.saurabharunima.superhumanraceassignment.data.model.DistanceUnit
import com.saurabharunima.superhumanraceassignment.data.model.ThemeMode
import com.saurabharunima.superhumanraceassignment.ui.components.PillButton
import com.saurabharunima.superhumanraceassignment.ui.components.SegmentedToggle
import com.saurabharunima.superhumanraceassignment.ui.components.StatusBarIcons
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    appSettings: AppSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by appSettings.settings.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        settings = settings,
        onBack = onBack,
        onLogout = onLogout,
        onRetry = viewModel::load,
        onNameChange = viewModel::onNameChange,
        onSaveName = viewModel::saveName,
        onMessageShown = viewModel::messageShown,
        onThemeMode = appSettings::setThemeMode,
        onDistanceUnit = appSettings::setDistanceUnit,
        onNotifications = appSettings::setNotifications,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    settings: AppSettings,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit,
    onNameChange: (String) -> Unit,
    onSaveName: () -> Unit,
    onMessageShown: () -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onDistanceUnit: (DistanceUnit) -> Unit,
    onNotifications: (Boolean) -> Unit,
) {
    StatusBarIcons(lightBackground = true)
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            onMessageShown()
        }
    }

    Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Settings", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 4.dp))
            }

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Section("Account") {
                    when (val profile = state.profile) {
                        UiState.Loading -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        is UiState.Error -> Column(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Couldn't load your account", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                            Text(profile.message, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.muted, textAlign = TextAlign.Center)
                            PillButton("Retry", onRetry, compact = true)
                        }
                        is UiState.Success -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = state.nameDraft,
                                onValueChange = onNameChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Name") },
                                singleLine = true,
                                isError = state.nameError != null,
                                supportingText = state.nameError?.let { err ->
                                    { Text(err, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { onSaveName() }),
                            )
                            Text("Email: ${profile.data.user.email}", style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.muted)
                            val unchanged = state.nameDraft.trim() == profile.data.user.name
                            PillButton(
                                text = if (state.saving) "Saving…" else "Save name",
                                onClick = onSaveName,
                                enabled = !state.saving && !unchanged,
                                compact = true,
                            )
                        }
                    }
                }

                Section("Appearance") {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Theme", style = MaterialTheme.typography.labelLarge)
                        SegmentedToggle(
                            options = ThemeMode.entries.map { it.label },
                            selectedIndex = settings.themeMode.ordinal,
                            onSelect = { onThemeMode(ThemeMode.entries[it]) },
                        )
                    }
                }

                Section("Units") {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Distance", style = MaterialTheme.typography.labelLarge)
                        SegmentedToggle(
                            options = DistanceUnit.entries.map { it.label },
                            selectedIndex = settings.distanceUnit.ordinal,
                            onSelect = { onDistanceUnit(DistanceUnit.entries[it]) },
                        )
                    }
                }

                Section("Notifications") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .toggleable(value = settings.notifications, role = Role.Switch, onValueChange = onNotifications)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Daily reminders", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        // The row handles the click, so the Switch itself is not separately focusable.
                        Switch(checked = settings.notifications, onCheckedChange = null)
                    }
                }

                TextButton(onClick = onLogout, modifier = Modifier.align(Alignment.CenterHorizontally).heightIn(min = 48.dp)) {
                    Text("Log out", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = AppTheme.colors.muted)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, AppTheme.colors.cardBorder),
        ) { content() }
    }
}
