package com.saurabharunima.superhumanraceassignment.ui.theme

import androidx.compose.runtime.compositionLocalOf
import com.saurabharunima.superhumanraceassignment.data.model.DistanceUnit

/** Provided once in MainActivity from Settings, so any screen can format distances without plumbing. */
val LocalDistanceUnit = compositionLocalOf { DistanceUnit.KM }
