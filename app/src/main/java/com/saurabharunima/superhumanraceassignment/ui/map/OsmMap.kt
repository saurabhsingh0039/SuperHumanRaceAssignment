package com.saurabharunima.superhumanraceassignment.ui.map

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.saurabharunima.superhumanraceassignment.data.local.GeoPoint
import com.saurabharunima.superhumanraceassignment.data.model.Friend
import java.io.File
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/** Lets the screen ask "where is the map pointing?" without knowing which map library is used. */
class MapTargetReader {
    var read: () -> GeoPoint? = { null }
}

/**
 * OpenStreetMap map (osmdroid): no API key, no billing. To use Google Maps instead, replace this
 * composable with one of the same signature (see docs/alt/GoogleFriendsMapView.kt.txt).
 *
 * Tiles come from OpenStreetMap's public servers: keep the attribution visible (MapScreen does)
 * and don't ship it as a high-traffic production map.
 */
@Composable
fun FriendsMapView(
    center: GeoPoint?,
    friends: List<Friend>,
    selected: Friend?,
    onSelect: (Friend?) -> Unit,
    targetReader: MapTargetReader,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentOnSelect by rememberUpdatedState(onSelect)
    val mapView = remember {
        createMapView(context, center) { currentOnSelect(null) }
    }

    // MapView needs resume/pause for tile loading and must be detached to release its caches.
    val lifecycleOwner = remember(context) { context.findComponentActivity() }
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    DisposableEffect(mapView) {
        targetReader.read = {
            val c = mapView.mapCenter
            GeoPoint(c.latitude, c.longitude)
        }
        onDispose { targetReader.read = { null } }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            // Rebuild only our markers; the tap-to-deselect overlay stays at the bottom of the stack.
            map.overlays.removeAll { it is Marker }
            val density = map.context.resources.displayMetrics.density

            center?.let { me ->
                map.overlays.add(
                    pin(map, OsmGeoPoint(me.lat, me.lng), "You are here", 0xFF0B5FE0.toInt(), 22, density, null),
                )
            }
            friends.forEach { friend ->
                val isSelected = friend.userId == selected?.userId
                map.overlays.add(
                    pin(
                        map, OsmGeoPoint(friend.lat, friend.lng), friend.name,
                        if (isSelected) 0xFFDC2F4B.toInt() else 0xFF2E9E5B.toInt(),
                        if (isSelected) 30 else 24, density,
                    ) { currentOnSelect(friend) },
                )
            }

            // Pan to a newly selected friend once, not on every recomposition.
            if (selected?.userId != map.tag) {
                map.tag = selected?.userId
                selected?.let { map.controller.animateTo(OsmGeoPoint(it.lat, it.lng)) }
            }
            map.invalidate()
        },
    )
}

private fun createMapView(context: Context, center: GeoPoint?, onMapTap: () -> Unit): MapView {
    // Must run before the first MapView: OSM's tile policy requires an identifying user agent,
    // and the cache lives in app-private storage so no storage permission is needed.
    Configuration.getInstance().apply {
        userAgentValue = context.packageName
        val base = File(context.cacheDir, "osmdroid")
        osmdroidBasePath = base
        osmdroidTileCache = File(base, "tiles")
    }
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        minZoomLevel = 3.0
        maxZoomLevel = 19.0
        controller.setZoom(14.0)
        center?.let { controller.setCenter(OsmGeoPoint(it.lat, it.lng)) }
        overlays.add(
            MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: OsmGeoPoint?): Boolean {
                    onMapTap()
                    return false
                }

                override fun longPressHelper(p: OsmGeoPoint?): Boolean = false
            }),
        )
    }
}

/** A coloured dot with a white ring, drawn in code so no image resources are needed. */
private fun pin(
    map: MapView,
    position: OsmGeoPoint,
    title: String,
    color: Int,
    sizeDp: Int,
    density: Float,
    onClick: (() -> Unit)?,
): Marker = Marker(map).apply {
    this.position = position
    this.title = title
    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    icon = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke((3 * density).toInt(), AndroidColor.WHITE)
        val px = (sizeDp * density).toInt()
        setSize(px, px)
    }
    // We show our own details card, so suppress osmdroid's info bubble.
    setInfoWindow(null)
    setOnMarkerClickListener { _, _ ->
        onClick?.invoke()
        true
    }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
