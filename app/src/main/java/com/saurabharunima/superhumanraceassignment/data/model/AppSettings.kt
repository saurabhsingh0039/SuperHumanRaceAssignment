package com.saurabharunima.superhumanraceassignment.data.model

enum class ThemeMode(val label: String) { SYSTEM("System"), LIGHT("Light"), DARK("Dark") }

enum class DistanceUnit(val label: String) { KM("Kilometres"), MILES("Miles") }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    /** Stored preference only: the mock app sends no real notifications. */
    val notifications: Boolean = true,
)
