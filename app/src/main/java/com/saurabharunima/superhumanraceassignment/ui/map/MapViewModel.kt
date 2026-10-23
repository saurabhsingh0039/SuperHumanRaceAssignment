package com.saurabharunima.superhumanraceassignment.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.core.toUiState
import com.saurabharunima.superhumanraceassignment.data.local.GeoPoint
import com.saurabharunima.superhumanraceassignment.data.local.LocationProvider
import com.saurabharunima.superhumanraceassignment.data.model.Friend
import com.saurabharunima.superhumanraceassignment.data.repository.FriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MapStage {
    data object Checking : MapStage
    data object NeedsPermission : MapStage
    data object Locating : MapStage
    data class LocationFailed(val message: String) : MapStage
    data object Ready : MapStage
}

data class MapState(
    val stage: MapStage = MapStage.Checking,
    /** The user's position (device or demo). Distances are measured from here. */
    val center: GeoPoint? = null,
    val permissionGranted: Boolean = false,
    /** Active friends only. */
    val friends: UiState<List<Friend>> = UiState.Loading,
    val selected: Friend? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val friendsRepo: FriendsRepository,
    private val location: LocationProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    private var friendsJob: Job? = null
    private var lastSearch: GeoPoint? = null

    /** Safe to call repeatedly (e.g. after rotation): it never restarts a map that is already Ready or Locating. */
    fun onPermissionResult(granted: Boolean) {
        val stage = _state.value.stage
        if (granted) {
            _state.update { it.copy(permissionGranted = true) }
            if (stage is MapStage.Checking || stage is MapStage.NeedsPermission || stage is MapStage.LocationFailed) {
                startLocating()
            }
        } else if (stage !is MapStage.Ready) {
            _state.update { it.copy(stage = MapStage.NeedsPermission, permissionGranted = false) }
        }
    }

    fun startLocating() {
        _state.update { it.copy(stage = MapStage.Locating) }
        viewModelScope.launch {
            location.current()
                .onSuccess { point ->
                    _state.update { it.copy(stage = MapStage.Ready, center = point) }
                    loadFriends(point)
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(stage = MapStage.LocationFailed(e.message ?: "Couldn't get your location"))
                    }
                }
        }
    }

    /** Lets the map work without permission (and on emulators with no GPS fix). */
    fun useDemoLocation() {
        _state.update { it.copy(stage = MapStage.Ready, center = DEMO_LOCATION) }
        loadFriends(DEMO_LOCATION)
    }

    fun searchArea(lat: Double, lng: Double) = loadFriends(GeoPoint(lat, lng))

    fun retryFriends() {
        (lastSearch ?: _state.value.center)?.let(::loadFriends)
    }

    fun select(friend: Friend?) {
        _state.update { it.copy(selected = friend) }
    }

    private fun loadFriends(at: GeoPoint) {
        // Cancel the previous request so a slow older search can't overwrite a newer one.
        friendsJob?.cancel()
        lastSearch = at
        _state.update { it.copy(friends = UiState.Loading, selected = null) }
        friendsJob = viewModelScope.launch {
            val result = friendsRepo.getNearby(at.lat, at.lng)
                .map { list -> list.filter { it.active } }
                .toUiState()
            _state.update { it.copy(friends = result) }
        }
    }

    companion object {
        /** Neutral fallback centre (New Delhi) used by "Use demo location". */
        val DEMO_LOCATION = GeoPoint(28.6139, 77.2090)
    }
}
