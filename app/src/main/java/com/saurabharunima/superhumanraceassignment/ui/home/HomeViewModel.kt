package com.saurabharunima.superhumanraceassignment.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.core.toUiState
import com.saurabharunima.superhumanraceassignment.data.model.Activity
import com.saurabharunima.superhumanraceassignment.data.model.ActivityType
import com.saurabharunima.superhumanraceassignment.data.model.ReportPeriod
import com.saurabharunima.superhumanraceassignment.data.model.UserStats
import com.saurabharunima.superhumanraceassignment.data.repository.ActivityRepository
import com.saurabharunima.superhumanraceassignment.data.repository.ReportRepository
import com.saurabharunima.superhumanraceassignment.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DayCircle(val label: String, val isToday: Boolean, val done: Boolean)

data class HomeData(
    val stats: UserStats,
    val activities: List<Activity>,
    val completedToday: Boolean,
    val week: List<DayCircle>,
)

data class MobilitySummary(val km: Double, val savedKg: Double)

data class HomeState(
    val content: UiState<HomeData> = UiState.Loading,
    /** Loads independently so the Mobility card shows its own spinner, then resolves. */
    val mobility: UiState<MobilitySummary> = UiState.Loading,
    val sheet: Activity? = null,
    val message: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val users: UserRepository,
    private val activities: ActivityRepository,
    private val reports: ReportRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        load(showLoading = true)
        loadMobility()
    }

    fun onActivityClick(activity: Activity) {
        when {
            activity.type == ActivityType.MOBILITY && _state.value.mobility is UiState.Error -> loadMobility()
            activity.needsActivation -> activate(activity)
            else -> _state.update { it.copy(sheet = activity) }
        }
    }

    /** START: open the first activity the user can actually do. */
    fun onStart() {
        val data = (_state.value.content as? UiState.Success)?.data ?: return
        data.activities
            .firstOrNull { !it.completed && !it.needsActivation && it.type != ActivityType.MOBILITY }
            ?.let { first -> _state.update { it.copy(sheet = first) } }
    }

    fun onDismissSheet() {
        _state.update { it.copy(sheet = null) }
    }

    fun onComplete(activity: Activity) {
        _state.update { it.copy(sheet = null) }
        viewModelScope.launch {
            activities.complete(activity.id)
                .onSuccess {
                    _state.update { s -> s.copy(message = "+${activity.xpReward} XP") }
                    load(showLoading = false)
                }
                .onFailure { e -> _state.update { s -> s.copy(message = e.message ?: "Couldn't complete activity") } }
        }
    }

    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    private fun activate(activity: Activity) {
        viewModelScope.launch {
            activities.activate(activity.id)
                .onSuccess { load(showLoading = false) }
                .onFailure { e -> _state.update { s -> s.copy(message = e.message ?: "Couldn't activate") } }
        }
    }

    private fun load(showLoading: Boolean) {
        viewModelScope.launch {
            if (showLoading) _state.update { it.copy(content = UiState.Loading) }
            fetchHome().fold(
                onSuccess = { data -> _state.update { it.copy(content = UiState.Success(data)) } },
                onFailure = { e ->
                    val msg = e.message ?: "Something went wrong"
                    _state.update { s ->
                        // Background refresh failures keep the screen and show a message instead.
                        if (s.content is UiState.Success && !showLoading) s.copy(message = msg)
                        else s.copy(content = UiState.Error(msg))
                    }
                },
            )
        }
    }

    private suspend fun fetchHome(): Result<HomeData> = coroutineScope {
        val profile = async { users.getProfile() }
        val today = async { activities.getToday() }
        val p = profile.await().getOrElse { return@coroutineScope Result.failure(it) }
        val t = today.await().getOrElse { return@coroutineScope Result.failure(it) }
        Result.success(HomeData(p.stats, t.activities, t.completedToday, weekFor(t.completedToday)))
    }

    private fun loadMobility() {
        viewModelScope.launch {
            _state.update { it.copy(mobility = UiState.Loading) }
            val result = reports.getReport(ReportPeriod.WEEKLY).map { r ->
                MobilitySummary(km = r.modeUsage.sumOf { it.km }, savedKg = r.totalSavedKg)
            }
            _state.update { it.copy(mobility = result.toUiState()) }
        }
    }

    /** Seven circles starting today (Tue..Mon style, like the reference). */
    private fun weekFor(doneToday: Boolean): List<DayCircle> {
        val today = LocalDate.now(clock)
        return (0..6).map { i ->
            val day = today.plusDays(i.toLong())
            DayCircle(
                label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                isToday = i == 0,
                done = i == 0 && doneToday,
            )
        }
    }
}
