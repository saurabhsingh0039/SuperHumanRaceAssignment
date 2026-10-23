package com.saurabharunima.superhumanraceassignment.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme
import com.saurabharunima.superhumanraceassignment.ui.theme.Blue700

/** What the bottom-right of an ActivityCard shows. Loading fixes the reference's stuck spinner: drive it from UiState. */
sealed interface ActivityAction {
    data class Xp(val reward: Int) : ActivityAction
    data object Activate : ActivityAction
    data object Loading : ActivityAction
    data object Done : ActivityAction
    data object None : ActivityAction
}

@Composable
fun ActivityCard(
    title: String,
    icon: ImageVector,
    action: ActivityAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBadge: Boolean = false,
    iconTint: Color = Blue700,
    /** Illustration slot, drawn behind the text. Leave empty to use a vector/placeholder later. */
    artwork: @Composable BoxScope.() -> Unit = {},
) {
    val colors = AppTheme.colors
    val shape = MaterialTheme.shapes.large
    val summary = buildString {
        append(title)
        if (action is ActivityAction.Xp) append(", plus ${action.reward} XP")
        if (action is ActivityAction.Loading) append(", loading")
        if (action is ActivityAction.Done) append(", completed")
        if (showBadge) append(", new")
    }

    Card(
        modifier = modifier
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = summary },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(180.dp)) {
            Box(Modifier.matchParentSize(), content = artwork)

            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.iconTile),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }

            if (showBadge) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(12.dp).size(14.dp)
                        .clip(CircleShape).background(colors.badge),
                )
            }

            when (action) {
                is ActivityAction.Xp -> XpPill(action.reward, Modifier.align(Alignment.BottomEnd).padding(12.dp))
                ActivityAction.Activate -> PillButton(
                    text = "ACTIVATE",
                    onClick = onClick,
                    style = PillStyle.Coral,
                    compact = true,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                )
                ActivityAction.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(28.dp),
                    strokeWidth = 3.dp,
                    color = Blue700,
                )
                ActivityAction.Done -> DonePill(Modifier.align(Alignment.BottomEnd).padding(12.dp))
                ActivityAction.None -> Unit
            }
        }
    }
}

@Composable
private fun XpPill(reward: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Brush.horizontalGradient(AppTheme.colors.primaryGradient))
            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("+$reward XP", style = MaterialTheme.typography.labelLarge, color = Color.White)
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Blue700, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DonePill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(AppTheme.colors.iconTile)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AppTheme.colors.leaf, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("DONE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
