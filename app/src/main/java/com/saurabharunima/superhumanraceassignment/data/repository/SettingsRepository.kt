package com.saurabharunima.superhumanraceassignment.data.repository

import com.saurabharunima.superhumanraceassignment.data.model.AppSettings
import com.saurabharunima.superhumanraceassignment.data.model.DistanceUnit
import com.saurabharunima.superhumanraceassignment.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDistanceUnit(unit: DistanceUnit)
    suspend fun setNotifications(enabled: Boolean)
}
