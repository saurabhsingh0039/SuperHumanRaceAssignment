package com.saurabharunima.superhumanraceassignment.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabharunima.superhumanraceassignment.data.model.AppSettings
import com.saurabharunima.superhumanraceassignment.data.model.DistanceUnit
import com.saurabharunima.superhumanraceassignment.data.model.ThemeMode
import com.saurabharunima.superhumanraceassignment.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * App-wide settings holder. Used by MainActivity (theme + unit for the whole tree) and by the
 * Settings screen (writes), so both see one source of truth.
 */
@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun setThemeMode(mode: ThemeMode) { viewModelScope.launch { repository.setThemeMode(mode) } }
    fun setDistanceUnit(unit: DistanceUnit) { viewModelScope.launch { repository.setDistanceUnit(unit) } }
    fun setNotifications(enabled: Boolean) { viewModelScope.launch { repository.setNotifications(enabled) } }
}
