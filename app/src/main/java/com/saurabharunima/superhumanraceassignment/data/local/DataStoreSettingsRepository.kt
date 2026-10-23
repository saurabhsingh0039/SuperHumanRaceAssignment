package com.saurabharunima.superhumanraceassignment.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.saurabharunima.superhumanraceassignment.data.model.AppSettings
import com.saurabharunima.superhumanraceassignment.data.model.DistanceUnit
import com.saurabharunima.superhumanraceassignment.data.model.ThemeMode
import com.saurabharunima.superhumanraceassignment.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) : SettingsRepository {
    private val store = context.settingsDataStore
    private val themeKey = stringPreferencesKey("theme_mode")
    private val unitKey = stringPreferencesKey("distance_unit")
    private val notificationsKey = booleanPreferencesKey("notifications")

    override val settings: Flow<AppSettings> = store.data.map { p ->
        AppSettings(
            // Unknown stored values (e.g. from a newer build) fall back to defaults instead of crashing.
            themeMode = ThemeMode.entries.firstOrNull { it.name == p[themeKey] } ?: ThemeMode.SYSTEM,
            distanceUnit = DistanceUnit.entries.firstOrNull { it.name == p[unitKey] } ?: DistanceUnit.KM,
            notifications = p[notificationsKey] ?: true,
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) { store.edit { it[themeKey] = mode.name } }
    override suspend fun setDistanceUnit(unit: DistanceUnit) { store.edit { it[unitKey] = unit.name } }
    override suspend fun setNotifications(enabled: Boolean) { store.edit { it[notificationsKey] = enabled } }
}
