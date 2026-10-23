package com.saurabharunima.superhumanraceassignment.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.core.toUiState
import com.saurabharunima.superhumanraceassignment.data.model.Post
import com.saurabharunima.superhumanraceassignment.data.repository.FeedRepository
import com.saurabharunima.superhumanraceassignment.domain.TimeAgo
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedState(
    val content: UiState<List<Post>> = UiState.Loading,
    /** Posts with a like request in flight; taps on them are ignored until the request settles. */
    val likeInFlight: Set<String> = emptySet(),
    val commentPostId: String? = null,
    val draft: String = "",
    val sending: Boolean = false,
    val commentError: String? = null,
    val message: String? = null,
) {
    /** Derived from the live list, so the sheet updates when a comment lands. */
    val commentPost: Post?
        get() = (content as? UiState.Success)?.data?.firstOrNull { it.id == commentPostId }
}

@HiltViewModel
class MissionsViewModel @Inject constructor(
    private val feed: FeedRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(content = UiState.Loading) }
        viewModelScope.launch {
            val result = feed.getPosts().toUiState()
            _state.update { it.copy(content = result) }
        }
    }

    fun timeAgo(timestamp: Long): String = TimeAgo.format(clock.millis(), timestamp)

    /** Optimistic: the UI flips immediately; on failure only the like fields are rolled back. */
    fun toggleLike(postId: String) {
        val current = (_state.value.content as? UiState.Success)?.data ?: return
        if (postId in _state.value.likeInFlight) return
        val old = current.firstOrNull { it.id == postId } ?: return

        val nowLiked = !old.likedByMe
        val optimistic = old.copy(
            likedByMe = nowLiked,
            likeCount = (old.likeCount + if (nowLiked) 1 else -1).coerceAtLeast(0),
        )
        _state.update { s -> s.withPost(optimistic).copy(likeInFlight = s.likeInFlight + postId) }

        viewModelScope.launch {
            feed.toggleLike(postId)
                .onSuccess { server ->
                    _state.update { s -> s.withPost(server).copy(likeInFlight = s.likeInFlight - postId) }
                }
                .onFailure { e ->
                    _state.update { s ->
                        val latest = (s.content as? UiState.Success)?.data?.firstOrNull { it.id == postId }
                        val reverted = latest?.copy(likedByMe = old.likedByMe, likeCount = old.likeCount)
                        (if (reverted != null) s.withPost(reverted) else s).copy(
                            likeInFlight = s.likeInFlight - postId,
                            message = e.message ?: "Couldn't update like",
                        )
                    }
                }
        }
    }

    fun openComments(postId: String) {
        _state.update { it.copy(commentPostId = postId, draft = "", commentError = null) }
    }

    fun closeComments() {
        _state.update { it.copy(commentPostId = null, draft = "", commentError = null, sending = false) }
    }

    fun onDraftChange(value: String) {
        _state.update { it.copy(draft = value, commentError = null) }
    }

    fun sendComment() {
        val s = _state.value
        val postId = s.commentPostId ?: return
        val text = s.draft.trim()
        if (text.isEmpty() || s.sending) return

        _state.update { it.copy(sending = true, commentError = null) }
        viewModelScope.launch {
            feed.addComment(postId, text)
                .onSuccess { server ->
                    _state.update { cur ->
                        // If the sheet was closed meanwhile, still apply the new comment to the list.
                        val stillOpen = cur.commentPostId == postId
                        cur.withPost(server).copy(
                            sending = false,
                            draft = if (stillOpen) "" else cur.draft,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(sending = false, commentError = e.message ?: "Couldn't post comment") }
                }
        }
    }

    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    private fun FeedState.withPost(post: Post): FeedState {
        val list = (content as? UiState.Success)?.data ?: return this
        return copy(content = UiState.Success(list.map { if (it.id == post.id) post else it }))
    }
}
