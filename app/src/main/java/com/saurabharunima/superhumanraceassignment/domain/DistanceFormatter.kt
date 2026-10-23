package com.saurabharunima.superhumanraceassignment.domain

import com.saurabharunima.superhumanraceassignment.data.model.DistanceUnit

/** Distances are stored in km everywhere; convert only at the display edge. Pure, so unit-testable. */
object DistanceFormatter {
    private const val MILES_PER_KM = 0.621371

    fun convert(km: Double, unit: DistanceUnit): Double = when (unit) {
        DistanceUnit.KM -> km
        DistanceUnit.MILES -> km * MILES_PER_KM
    }

    fun short(km: Double, unit: DistanceUnit): String = when (unit) {
        DistanceUnit.KM -> "%.1f km".format(convert(km, unit))
        DistanceUnit.MILES -> "%.1f mi".format(convert(km, unit))
    }

    /** Spoken form for contentDescriptions. */
    fun spoken(km: Double, unit: DistanceUnit): String = when (unit) {
        DistanceUnit.KM -> "%.1f kilometres".format(convert(km, unit))
        DistanceUnit.MILES -> "%.1f miles".format(convert(km, unit))
    }
}
