package com.saurabharunima.superhumanraceassignment.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabharunima.superhumanraceassignment.data.model.SocialProvider
import com.saurabharunima.superhumanraceassignment.data.repository.AuthRepository
import com.saurabharunima.superhumanraceassignment.domain.AuthValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    /** Server-side failure (bad credentials, email taken). Field errors live in the *Error fields. */
    val error: String? = null,
)

/**
 * Success is not signalled here: AuthRepository.sessionUserId flips, SessionViewModel emits
 * SignedIn, and the root swaps the nav graph.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthFormState())
    val state: StateFlow<AuthFormState> = _state.asStateFlow()

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null, error = null) }
    }

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, emailError = null, error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, passwordError = null, error = null) }
    }

    fun login() {
        if (_state.value.isLoading || !validate(requireName = false)) return
        val s = _state.value
        submit { auth.login(s.email, s.password) }
    }

    fun signUp() {
        if (_state.value.isLoading || !validate(requireName = true)) return
        val s = _state.value
        submit { auth.signUp(s.name, s.email, s.password) }
    }

    fun loginWithProvider(provider: SocialProvider) {
        if (_state.value.isLoading) return
        submit { auth.loginWithProvider(provider) }
    }

    private fun validate(requireName: Boolean): Boolean {
        val s = _state.value
        val nameError = if (requireName) AuthValidator.name(s.name) else null
        val emailError = AuthValidator.email(s.email)
        val passwordError = AuthValidator.password(s.password)
        _state.update {
            it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError)
        }
        return nameError == null && emailError == null && passwordError == null
    }

    private fun submit(call: suspend () -> Result<*>) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            call().onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Something went wrong") }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
