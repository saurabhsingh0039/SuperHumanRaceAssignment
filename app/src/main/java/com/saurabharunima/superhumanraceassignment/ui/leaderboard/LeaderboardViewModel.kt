package com.saurabharunima.superhumanraceassignment.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.data.model.LeaderEntry
import com.saurabharunima.superhumanraceassignment.data.model.UserStats
import com.saurabharunima.superhumanraceassignment.data.repository.LeaderboardRepository
import com.saurabharunima.superhumanraceassignment.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaderboardData(
    val leagueName: String,
    val advanceTop: Int,
    /** Preformatted, e.g. "5D 15h". Computed at load time, not live-ticking. */
    val timeLeft: String,
    val entries: List<LeaderEntry>,
    val stats: UserStats,
)

data class LeaderboardState(
    val content: UiState<LeaderboardData> = UiState.Loading,
    /** Follow is local-only in the mock app. */
    val following: Set<String> = emptySet(),
    val showInfo: Boolean = false,
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboard: LeaderboardRepository,
    private val users: UserRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardState())
    val state: StateFlow<LeaderboardState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(content = UiState.Loading) }
        viewModelScope.launch {
            val result = runCatching {
                coroutineScope {
                    val board = async { leaderboard.getLeaderboard() }
                    val profile = async { users.getProfile() }
                    val b = board.await().getOrThrow()
                    val p = profile.await().getOrThrow()
                    LeaderboardData(
                        leagueName = b.league.name,
                        advanceTop = b.league.advanceTop,
                        timeLeft = formatTimeLeft(b.league.endsAtMillis - clock.millis()),
                        entries = b.entries,
                        stats = p.stats,
                    )
                }
            }
            _state.update { s ->
                s.copy(
                    content = result.fold(
                        onSuccess = { UiState.Success(it) },
                        onFailure = { UiState.Error(it.message ?: "Something went wrong") },
                    ),
                )
            }
        }
    }

    fun toggleFollow(userId: String) {
        _state.update { s ->
            s.copy(following = if (userId in s.following) s.following - userId else s.following + userId)
        }
    }

    fun showInfo(show: Boolean) {
        _state.update { it.copy(showInfo = show) }
    }

    internal companion object {
        fun formatTimeLeft(millis: Long): String {
            val totalHours = (millis.coerceAtLeast(0) / 3_600_000L)
            return "${totalHours / 24}D ${totalHours % 24}h"
        }
    }
}
