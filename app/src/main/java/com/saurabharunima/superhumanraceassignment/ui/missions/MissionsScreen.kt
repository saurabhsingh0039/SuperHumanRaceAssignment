package com.saurabharunima.superhumanraceassignment.ui.missions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.data.model.Comment
import com.saurabharunima.superhumanraceassignment.data.model.Post
import com.saurabharunima.superhumanraceassignment.ui.components.InitialsAvatar
import com.saurabharunima.superhumanraceassignment.ui.components.PillButton
import com.saurabharunima.superhumanraceassignment.ui.components.StatusBarIcons
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme

@Composable
fun MissionsRoute(viewModel: MissionsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MissionsScreen(
        state = state,
        timeAgo = viewModel::timeAgo,
        onRetry = viewModel::load,
        onLike = viewModel::toggleLike,
        onOpenComments = viewModel::openComments,
        onCloseComments = viewModel::closeComments,
        onDraftChange = viewModel::onDraftChange,
        onSend = viewModel::sendComment,
        onMessageShown = viewModel::onMessageShown,
    )
}

@Composable
fun MissionsScreen(
    state: FeedState,
    timeAgo: (Long) -> String,
    onRetry: () -> Unit,
    onLike: (String) -> Unit,
    onOpenComments: (String) -> Unit,
    onCloseComments: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onMessageShown: () -> Unit,
) {
    StatusBarIcons(lightBackground = true)

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            onMessageShown()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Text(
                "Missions",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )
            when (val content = state.content) {
                UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is UiState.Error -> Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Couldn't load the feed", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    Text(content.message, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.muted, textAlign = TextAlign.Center)
                    PillButton("Retry", onRetry)
                }
                is UiState.Success -> if (content.data.isEmpty()) {
                    Text(
                        "No posts yet. Complete a mission and share it with your friends.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTheme.colors.muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    )
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(content.data, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                time = timeAgo(post.timestamp),
                                onLike = { onLike(post.id) },
                                onComments = { onOpenComments(post.id) },
                            )
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp))
        state.commentPost?.let { post ->
            CommentsSheet(post, state, timeAgo, onCloseComments, onDraftChange, onSend)
        }
    }
}

@Composable
private fun PostCard(post: Post, time: String, onLike: () -> Unit, onComments: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics(mergeDescendants = true) {},
            ) {
                InitialsAvatar(post.userName, size = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(post.userName, style = MaterialTheme.typography.titleMedium)
                    Text(time, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.muted)
                }
            }
            Text(post.text, style = MaterialTheme.typography.bodyLarge)
            Surface(shape = CircleShape, color = AppTheme.colors.iconTile) {
                Text(
                    "Saved %.1f kg CO₂e".format(post.co2SavedKg),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.leaf,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton(
                    icon = if (post.likedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    tint = if (post.likedByMe) AppTheme.colors.gem else AppTheme.colors.muted,
                    count = post.likeCount,
                    description = "Like, ${post.likeCount} ${if (post.likeCount == 1) "like" else "likes"}",
                    stateText = if (post.likedByMe) "Liked" else "Not liked",
                    onClick = onLike,
                )
                ActionButton(
                    icon = Icons.Filled.MailOutline,
                    tint = AppTheme.colors.muted,
                    count = post.comments.size,
                    description = "Comments, ${post.comments.size}",
                    stateText = null,
                    onClick = onComments,
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    count: Int,
    description: String,
    stateText: String?,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                if (stateText != null) stateDescription = stateText
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Text(count.toString(), style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsSheet(
    post: Post,
    state: FeedState,
    timeAgo: (Long) -> String,
    onDismiss: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 16.dp)) {
            Text("Comments", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
            if (post.comments.isEmpty()) {
                Text(
                    "No comments yet. Be the first.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.muted,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                LazyColumn(
                    Modifier.fillMaxWidth().defaultMinSize(minHeight = 1.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(post.comments, key = { it.id }) { CommentRow(it, timeAgo(it.timestamp)) }
                }
            }
            state.commentError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp).semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Add a comment") },
                    singleLine = true,
                    enabled = !state.sending,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSend = { onSend() }),
                    shape = MaterialTheme.shapes.medium,
                )
                if (state.sending) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                } else {
                    IconButton(onClick = onSend, enabled = state.draft.isNotBlank()) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send comment",
                            tint = if (state.draft.isNotBlank()) MaterialTheme.colorScheme.primary else AppTheme.colors.muted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentRow(comment: Comment, time: String) {
    Row(
        Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InitialsAvatar(comment.userName, size = 36.dp)
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(comment.userName, style = MaterialTheme.typography.labelLarge)
                Text(time, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.muted)
            }
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
