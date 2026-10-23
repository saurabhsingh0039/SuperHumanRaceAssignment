package com.saurabharunima.superhumanraceassignment.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.saurabharunima.superhumanraceassignment.domain.DistanceFormatter
import com.saurabharunima.superhumanraceassignment.ui.theme.LocalDistanceUnit
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.data.model.Friend
import com.saurabharunima.superhumanraceassignment.domain.GeoMath
import com.saurabharunima.superhumanraceassignment.ui.components.InitialsAvatar
import com.saurabharunima.superhumanraceassignment.ui.components.PillButton
import com.saurabharunima.superhumanraceassignment.ui.components.PillStyle
import com.saurabharunima.superhumanraceassignment.ui.components.StatusBarIcons
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

private fun hasLocationPermission(context: Context) = LOCATION_PERMISSIONS.any {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun MapRoute(onBack: () -> Unit, viewModel: MapViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        viewModel.onPermissionResult(result.values.any { it })
    }
    // Survives rotation, so a denied dialog isn't re-shown on every recreation.
    var requestedOnce by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        when {
            hasLocationPermission(context) -> viewModel.onPermissionResult(true)
            !requestedOnce -> {
                requestedOnce = true
                launcher.launch(LOCATION_PERMISSIONS)
            }
            else -> viewModel.onPermissionResult(false)
        }
    }

    MapScreen(
        state = state,
        onBack = onBack,
        onAllow = { launcher.launch(LOCATION_PERMISSIONS) },
        onOpenSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        },
        onRetryLocation = viewModel::startLocating,
        onUseDemo = viewModel::useDemoLocation,
        onSelect = viewModel::select,
        onSearchArea = viewModel::searchArea,
        onRetryFriends = viewModel::retryFriends,
    )
}

@Composable
fun MapScreen(
    state: MapState,
    onBack: () -> Unit,
    onAllow: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetryLocation: () -> Unit,
    onUseDemo: () -> Unit,
    onSelect: (Friend?) -> Unit,
    onSearchArea: (Double, Double) -> Unit,
    onRetryFriends: () -> Unit,
) {
    // Dark status-bar icons: the top pill and fallback screens are light.
    StatusBarIcons(lightBackground = true)
    val targetReader = remember { MapTargetReader() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val stage = state.stage) {
            MapStage.Checking, MapStage.Locating -> CenterMessage {
                CircularProgressIndicator()
                Text("Finding your location…", style = MaterialTheme.typography.bodyLarge)
            }
            MapStage.NeedsPermission -> CenterMessage {
                Icon(Icons.Filled.Place, contentDescription = null, tint = AppTheme.colors.muted, modifier = Modifier.size(48.dp))
                Text("Location access needed", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(
                    "Allow location to see active friends near you. You can also explore with a demo location.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.muted,
                    textAlign = TextAlign.Center,
                )
                PillButton("Allow location", onAllow, Modifier.fillMaxWidth())
                PillButton("Open app settings", onOpenSettings, Modifier.fillMaxWidth(), PillStyle.Light)
                TextButton(onClick = onUseDemo) { Text("Use demo location", style = MaterialTheme.typography.labelLarge) }
            }
            is MapStage.LocationFailed -> CenterMessage {
                Icon(Icons.Filled.Place, contentDescription = null, tint = AppTheme.colors.muted, modifier = Modifier.size(48.dp))
                Text("Couldn't get your location", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(stage.message, style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.muted, textAlign = TextAlign.Center)
                PillButton("Try again", onRetryLocation, Modifier.fillMaxWidth())
                TextButton(onClick = onUseDemo) { Text("Use demo location", style = MaterialTheme.typography.labelLarge) }
            }
            MapStage.Ready -> ReadyMap(state, targetReader, onSelect, onSearchArea, onRetryFriends)
        }

        TopPill(
            title = "Around me",
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(16.dp),
        )
    }
}

@Composable
private fun CenterMessage(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp).padding(top = 72.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}

@Composable
private fun TopPill(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder),
    ) {
        Row(Modifier.padding(end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ReadyMap(
    state: MapState,
    targetReader: MapTargetReader,
    onSelect: (Friend?) -> Unit,
    onSearchArea: (Double, Double) -> Unit,
    onRetryFriends: () -> Unit,
) {
    val friends = (state.friends as? UiState.Success)?.data.orEmpty()

    Box(Modifier.fillMaxSize()) {
        FriendsMapView(
            center = state.center,
            friends = friends,
            selected = state.selected,
            onSelect = onSelect,
            targetReader = targetReader,
            modifier = Modifier.fillMaxSize(),
        )

        PillButton(
            text = if (state.friends is UiState.Loading) "Searching…" else "Search this area",
            onClick = {
                // Fall back to the user's position if the map can't report its centre yet.
                val target = targetReader.read() ?: state.center
                if (target != null) onSearchArea(target.lat, target.lng)
            },
            enabled = state.friends !is UiState.Loading,
            compact = true,
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(16.dp),
        )

        Column(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Required by OpenStreetMap's tile usage policy.
            Surface(
                modifier = Modifier.align(Alignment.End),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            ) {
                Text(
                    "© OpenStreetMap contributors",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            val selected = state.selected
            if (selected != null) {
                FriendCard(selected, state, onClose = { onSelect(null) })
            } else {
                FriendsPanel(state.friends, onSelect, onRetryFriends)
            }
        }
    }
}

@Composable
private fun FriendsPanel(friends: UiState<List<Friend>>, onSelect: (Friend) -> Unit, onRetry: () -> Unit) {
    PanelSurface {
        when (friends) {
            UiState.Loading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                Text("Finding active friends…", style = MaterialTheme.typography.bodyLarge)
            }
            is UiState.Error -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Couldn't load friends. ${friends.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onRetry) { Text("Retry", style = MaterialTheme.typography.labelLarge) }
            }
            is UiState.Success -> if (friends.data.isEmpty()) {
                Text("No active friends nearby. Try Search this area.", style = MaterialTheme.typography.bodyLarge)
            } else {
                val count = friends.data.size
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "$count active ${if (count == 1) "friend" else "friends"} nearby",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(friends.data, key = { it.userId }) { friend ->
                            FriendChip(friend) { onSelect(friend) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendChip(friend: Friend, onClick: () -> Unit) {
    Row(
        Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(end = 12.dp)
            .semantics(mergeDescendants = true) { contentDescription = "${friend.name}, active. Show on map" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InitialsAvatar(friend.name, size = 36.dp)
        Spacer(Modifier.width(8.dp))
        Text(friend.name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
private fun FriendCard(friend: Friend, state: MapState, onClose: () -> Unit) {
    val distance = state.center?.let { GeoMath.distanceKm(it.lat, it.lng, friend.lat, friend.lng) }
    val unit = LocalDistanceUnit.current
    PanelSurface {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar(friend.name, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(friend.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    buildString {
                        append("Active now")
                        if (distance != null) append(" · ${DistanceFormatter.short(distance, unit)} away")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.muted,
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close friend details")
            }
        }
    }
}

@Composable
private fun PanelSurface(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder),
    ) {
        Box(Modifier.padding(16.dp)) { content() }
    }
}
