package com.saurabharunima.superhumanraceassignment.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme

enum class PillStyle { Primary, Coral, Light }

/** Fully rounded button. 56dp tall (48dp+ touch target); `compact` is 48dp. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillStyle = PillStyle.Primary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    val colors = AppTheme.colors
    val brush: Brush = when (style) {
        PillStyle.Primary -> Brush.horizontalGradient(colors.primaryGradient)
        PillStyle.Coral -> Brush.verticalGradient(colors.coralGradient)
        PillStyle.Light -> SolidColor(MaterialTheme.colorScheme.surface)
    }
    val content = if (style == PillStyle.Light) MaterialTheme.colorScheme.onSurface else Color.White

    Row(
        modifier = modifier
            .heightIn(min = if (compact) 48.dp else 56.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(CircleShape)
            .background(brush)
            .then(if (style == PillStyle.Light) Modifier.border(1.dp, colors.cardBorder, CircleShape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = content)
    }
}
