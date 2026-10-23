package com.saurabharunima.superhumanraceassignment.domain

import com.saurabharunima.superhumanraceassignment.data.model.TransportMode

/**
 * Emission factors in kg CO2e per passenger-km.
 * ASSUMPTION: approximate average values chosen for the mock app; verify against a published
 * source (e.g. national GHG conversion factors) before citing them in the README.
 * Savings are measured against a car baseline.
 */
object Co2Calculator {
    private val kgPerKm = mapOf(
        TransportMode.WALK to 0.0,
        TransportMode.CYCLE to 0.0,
        TransportMode.BUS to 0.089,
        TransportMode.TRAIN to 0.035,
        TransportMode.CAR to 0.171,
    )

    fun emissionsKg(mode: TransportMode, km: Double): Double = kgPerKm.getValue(mode) * km

    fun savedKg(mode: TransportMode, km: Double): Double =
        (kgPerKm.getValue(TransportMode.CAR) - kgPerKm.getValue(mode)) * km
}
