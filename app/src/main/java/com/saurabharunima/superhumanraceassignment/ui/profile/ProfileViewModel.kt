package com.saurabharunima.superhumanraceassignment.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.core.toUiState
import com.saurabharunima.superhumanraceassignment.data.model.Profile
import com.saurabharunima.superhumanraceassignment.data.model.Report
import com.saurabharunima.superhumanraceassignment.data.model.ReportPeriod
import com.saurabharunima.superhumanraceassignment.data.repository.ReportRepository
import com.saurabharunima.superhumanraceassignment.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProfileTab(val label: String) { PERFORMANCE("Performance"), IMPACT("Impact") }

data class ProfileState(
    val profile: UiState<Profile> = UiState.Loading,
    val tab: ProfileTab = ProfileTab.PERFORMANCE,
    val period: ReportPeriod = ReportPeriod.WEEKLY,
    /** Loads independently of the profile so the Impact tab has its own loading/error states. */
    val report: UiState<Report> = UiState.Loading,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val users: UserRepository,
    private val reports: ReportRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private var reportJob: Job? = null

    init {
        loadProfile()
        loadReport()
    }

    fun loadProfile() {
        _state.update { it.copy(profile = UiState.Loading) }
        viewModelScope.launch {
            val result = users.getProfile().toUiState()
            _state.update { it.copy(profile = result) }
        }
    }

    /** Re-fetch without a spinner (e.g. after editing the name in Settings). Keeps current data on failure. */
    fun refreshProfileSilently() {
        if (_state.value.profile !is UiState.Success) return
        viewModelScope.launch {
            users.getProfile().onSuccess { p -> _state.update { it.copy(profile = UiState.Success(p)) } }
        }
    }

    fun loadReport() {
        // Cancel any in-flight request so a slow Weekly response can't overwrite a newer Monthly one.
        reportJob?.cancel()
        val period = _state.value.period
        _state.update { it.copy(report = UiState.Loading) }
        reportJob = viewModelScope.launch {
            val result = reports.getReport(period).toUiState()
            _state.update { it.copy(report = result) }
        }
    }

    fun selectTab(tab: ProfileTab) {
        _state.update { it.copy(tab = tab) }
    }

    fun selectPeriod(period: ReportPeriod) {
        if (period == _state.value.period) return
        _state.update { it.copy(period = period) }
        loadReport()
    }
}
