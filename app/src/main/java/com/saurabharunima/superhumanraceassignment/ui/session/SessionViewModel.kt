package com.saurabharunima.superhumanraceassignment.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabharunima.superhumanraceassignment.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val userId: String) : SessionState
}

/** Drives the auth gate: restore the saved session once, then follow AuthRepository.sessionUserId. */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {

    private val restored = MutableStateFlow(false)

    val state: StateFlow<SessionState> = combine(restored, auth.sessionUserId) { done, id ->
        when {
            !done -> SessionState.Loading
            id == null -> SessionState.SignedOut
            else -> SessionState.SignedIn(id)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    init {
        viewModelScope.launch {
            auth.restoreSession()
            restored.value = true
        }
    }

    fun logout() {
        viewModelScope.launch { auth.logout() }
    }
}
