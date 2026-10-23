package com.saurabharunima.superhumanraceassignment.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import com.saurabharunima.superhumanraceassignment.core.suspendRunCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

data class GeoPoint(val lat: Double, val lng: Double)

class LocationUnavailableException(message: String) : Exception(message)

/** Seam for tests: fake this instead of touching LocationManager. */
interface LocationProvider {
    /** Caller must hold a location permission; otherwise this returns a failure (SecurityException). */
    suspend fun current(): Result<GeoPoint>
}

/** Uses the platform LocationManager, so no Play Services dependency is needed. */
@Singleton
class DeviceLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun current(): Result<GeoPoint> = suspendRunCatching {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        if (providers.isEmpty()) {
            throw LocationUnavailableException("Location services are off. Turn them on and try again.")
        }

        for (provider in providers) {
            val fix = withTimeoutOrNull(FIX_TIMEOUT_MS) { requestFix(manager, provider) }
            if (fix != null) return@suspendRunCatching GeoPoint(fix.latitude, fix.longitude)
        }
        // No fresh fix in time: fall back to the newest cached location, if any.
        val cached = providers.mapNotNull { manager.getLastKnownLocation(it) }.maxByOrNull { it.time }
            ?: throw LocationUnavailableException("Couldn't get your location. Try again outdoors or check GPS.")
        GeoPoint(cached.latitude, cached.longitude)
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFix(manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { cont ->
            val signal = CancellationSignal()
            cont.invokeOnCancellation { signal.cancel() }
            LocationManagerCompat.getCurrentLocation(
                manager, provider, signal, ContextCompat.getMainExecutor(context),
            ) { location: Location? -> if (cont.isActive) cont.resume(location) }
        }

    private companion object {
        const val FIX_TIMEOUT_MS = 6_000L
    }
}
