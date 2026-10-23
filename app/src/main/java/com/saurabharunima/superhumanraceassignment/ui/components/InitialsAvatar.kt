package com.saurabharunima.superhumanraceassignment.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Initials avatar; swap for Coil AsyncImage when avatar URLs exist. Decorative: parent rows describe the person. */
@Composable
fun InitialsAvatar(name: String, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    val palette = listOf(Color(0xFF1E90F0), Color(0xFF2E9E5B), Color(0xFFD6336C), Color(0xFFF5A623), Color(0xFF7C4DFF))
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Box(
        modifier.size(size).clip(CircleShape).background(palette[(name.hashCode() and Int.MAX_VALUE) % palette.size]),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}
