package com.saurabharunima.superhumanraceassignment.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.data.model.ChatMessage
import com.saurabharunima.superhumanraceassignment.data.repository.ChatRepository
import com.saurabharunima.superhumanraceassignment.domain.MarkdownStripper
import com.saurabharunima.superhumanraceassignment.domain.TimeAgo
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatState(
    /** Loading/Error describe fetching the opening greeting; Success means the chat is usable. */
    val start: UiState<Unit> = UiState.Loading,
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val typing: Boolean = false,
    val replyError: String? = null,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chat: ChatRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var startJob: Job? = null
    private var replyJob: Job? = null
    private var lastUserText: String? = null

    val suggestions = listOf("Tell me about CO2e", "Transport tips", "Food tips", "Shopping tips")

    init {
        start()
    }

    fun timeAgo(timestamp: Long): String = TimeAgo.format(clock.millis(), timestamp)

    fun retryStart() = start()

    /** Refresh icon: drop the conversation and begin again. In-flight work is cancelled so no stale reply lands. */
    fun refresh() {
        replyJob?.cancel()
        lastUserText = null
        start()
    }

    fun onDraftChange(value: String) {
        _state.update { it.copy(draft = value) }
    }

    fun onSend() = sendText(_state.value.draft, clearDraft = true)

    fun onSuggestion(text: String) = sendText(text, clearDraft = false)

    fun retryReply() {
        val text = lastUserText ?: return
        if (_state.value.typing) return
        requestReply(text)
    }

    private fun start() {
        startJob?.cancel()
        _state.value = ChatState()
        startJob = viewModelScope.launch {
            chat.greeting()
                .onSuccess { greeting ->
                    _state.update { it.copy(start = UiState.Success(Unit), messages = listOf(greeting.cleaned())) }
                }
                .onFailure { e ->
                    _state.update { it.copy(start = UiState.Error(e.message ?: "Couldn't start the chat")) }
                }
        }
    }

    private fun sendText(raw: String, clearDraft: Boolean) {
        val text = raw.trim()
        val s = _state.value
        // One question at a time: ignore sends while Ora is "typing" or before the chat has started.
        if (text.isEmpty() || s.typing || s.start !is UiState.Success) return

        val userMessage = ChatMessage(UUID.randomUUID().toString(), text, fromUser = true, timestamp = clock.millis())
        _state.update {
            it.copy(
                messages = it.messages + userMessage,
                draft = if (clearDraft) "" else it.draft,
            )
        }
        lastUserText = text
        requestReply(text)
    }

    private fun requestReply(text: String) {
        _state.update { it.copy(typing = true, replyError = null) }
        replyJob = viewModelScope.launch {
            chat.reply(text)
                .onSuccess { reply ->
                    _state.update { it.copy(messages = it.messages + reply.cleaned(), typing = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(typing = false, replyError = e.message ?: "Couldn't get a reply") }
                }
        }
    }

    private fun ChatMessage.cleaned() = copy(text = MarkdownStripper.plain(text))
}
