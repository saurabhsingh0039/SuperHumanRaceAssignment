package com.saurabharunima.superhumanraceassignment.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabharunima.superhumanraceassignment.ui.components.PillButton
import com.saurabharunima.superhumanraceassignment.ui.components.StatusBarIcons
import com.saurabharunima.superhumanraceassignment.ui.theme.SuperHumanTheme

@Composable
fun SignUpRoute(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SignUpScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignUp = viewModel::signUp,
        onBack = onBack,
    )
}

@Composable
fun SignUpScreen(
    state: AuthFormState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignUp: () -> Unit,
    onBack: () -> Unit,
) {
    StatusBarIcons(lightBackground = true)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrandHeader()
            Text("Create your account", style = MaterialTheme.typography.titleLarge)
            AuthTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = "Name",
                error = state.nameError,
                enabled = !state.isLoading,
            )
            AuthTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email address",
                error = state.emailError,
                keyboardType = KeyboardType.Email,
                enabled = !state.isLoading,
            )
            AuthTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Password (min 8 characters)",
                error = state.passwordError,
                isPassword = true,
                imeAction = ImeAction.Done,
                enabled = !state.isLoading,
                onDone = onSignUp,
            )
            state.error?.let { AuthErrorText(it) }
            PillButton(
                text = if (state.isLoading) "Creating account…" else "Create account",
                onClick = onSignUp,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            )
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Back to sign in", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SignUpPreview() = SuperHumanTheme(darkTheme = false) {
    SignUpScreen(AuthFormState(nameError = "Enter your name"), {}, {}, {}, {}, {})
}
