package com.saurabharunima.superhumanraceassignment.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.data.model.ChatMessage
import com.saurabharunima.superhumanraceassignment.ui.components.PillButton
import com.saurabharunima.superhumanraceassignment.ui.components.StatusBarIcons
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme
import com.saurabharunima.superhumanraceassignment.ui.theme.Blue700

@Composable
fun ChatRoute(onBack: () -> Unit, viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ChatScreen(
        state = state,
        suggestions = viewModel.suggestions,
        timeAgo = viewModel::timeAgo,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onRetryStart = viewModel::retryStart,
        onRetryReply = viewModel::retryReply,
        onDraftChange = viewModel::onDraftChange,
        onSend = viewModel::onSend,
        onSuggestion = viewModel::onSuggestion,
    )
}

@Composable
fun ChatScreen(
    state: ChatState,
    suggestions: List<String>,
    timeAgo: (Long) -> String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRetryStart: () -> Unit,
    onRetryReply: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onSuggestion: (String) -> Unit,
) {
    StatusBarIcons(lightBackground = true)

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        TopBar(onBack = onBack, onRefresh = onRefresh)

        when (val start = state.start) {
            UiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Column(
                Modifier.weight(1f).fillMaxWidth().padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Couldn't start the chat", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Text(start.message, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.muted, textAlign = TextAlign.Center)
                PillButton("Retry", onRetryStart)
            }
            is UiState.Success -> {
                MessageList(state, timeAgo, Modifier.weight(1f))
                state.replyError?.let { ReplyError(it, onRetryReply) }
                // Starter prompts only while the conversation hasn't begun.
                if (state.messages.size <= 1 && !state.typing) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(suggestions) { text ->
                            SuggestionChip(onClick = { onSuggestion(text) }, label = { Text(text) })
                        }
                    }
                }
                InputBar(state, onDraftChange, onSend)
            }
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit, onRefresh: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text("Ora", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).padding(start = 4.dp))
        IconButton(onClick = onRefresh) {
            Icon(Icons.Filled.Refresh, contentDescription = "Start a new chat")
        }
    }
}

@Composable
private fun MessageList(state: ChatState, timeAgo: (Long) -> String, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val itemCount = state.messages.size + if (state.typing) 1 else 0

    // Keep the newest message (or the typing bubble) in view.
    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(state.messages, key = { it.id }) { message ->
            Bubble(message.text, fromUser = message.fromUser, time = timeAgo(message.timestamp))
        }
        if (state.typing) {
            item(key = "typing") { TypingBubble() }
        }
    }
}

@Composable
private fun Bubble(text: String, fromUser: Boolean, time: String) {
    val speaker = if (fromUser) "You" else "Ora"
    Row(
        Modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = "$speaker: $text, $time" },
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (fromUser) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Surface(
                shape = if (fromUser) RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp) else RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
                color = if (fromUser) Blue700 else MaterialTheme.colorScheme.surface,
                border = if (fromUser) null else BorderStroke(1.dp, AppTheme.colors.cardBorder),
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (fromUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            Text(time, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.muted)
        }
    }
}

@Composable
private fun TypingBubble() {
    Surface(
        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder),
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            Text("Ora is typing…", style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.muted)
        }
    }
}

@Composable
private fun ReplyError(message: String, onRetry: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
        )
        TextButton(onClick = onRetry) { Text("Retry", style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
private fun InputBar(state: ChatState, onDraftChange: (String) -> Unit, onSend: () -> Unit) {
    val canSend = state.draft.isNotBlank() && !state.typing
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = state.draft,
            onValueChange = onDraftChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message…") },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
        )
        Surface(shape = CircleShape, color = Blue700, modifier = Modifier.alpha(if (canSend) 1f else 0.4f)) {
            IconButton(onClick = onSend, enabled = canSend, modifier = Modifier.size(52.dp)) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message", tint = Color.White)
            }
        }
    }
}
