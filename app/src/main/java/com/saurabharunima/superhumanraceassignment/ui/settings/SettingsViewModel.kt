package com.saurabharunima.superhumanraceassignment.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabharunima.superhumanraceassignment.core.UiState
import com.saurabharunima.superhumanraceassignment.core.toUiState
import com.saurabharunima.superhumanraceassignment.data.model.Profile
import com.saurabharunima.superhumanraceassignment.data.repository.UserRepository
import com.saurabharunima.superhumanraceassignment.domain.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val profile: UiState<Profile> = UiState.Loading,
    val nameDraft: String = "",
    val nameError: String? = null,
    val saving: Boolean = false,
    /** One-shot confirmation or failure text, cleared by the screen after it is shown. */
    val message: String? = null,
)

/** Account part of Settings (name). Theme/unit/notification toggles live in [AppSettingsViewModel]. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val users: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(profile = UiState.Loading) }
        viewModelScope.launch {
            val result = users.getProfile()
            _state.update { s ->
                s.copy(
                    profile = result.toUiState(),
                    nameDraft = result.getOrNull()?.user?.name ?: s.nameDraft,
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _state.update { it.copy(nameDraft = value, nameError = null) }
    }

    fun saveName() {
        val s = _state.value
        if (s.saving) return
        val error = AuthValidator.name(s.nameDraft)
        if (error != null) {
            _state.update { it.copy(nameError = error) }
            return
        }
        _state.update { it.copy(saving = true, nameError = null) }
        viewModelScope.launch {
            users.updateName(s.nameDraft.trim())
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            saving = false,
                            profile = UiState.Success(profile),
                            nameDraft = profile.user.name,
                            message = "Name updated",
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(saving = false, message = e.message ?: "Couldn't update your name") }
                }
        }
    }

    fun messageShown() {
        _state.update { it.copy(message = null) }
    }
}
