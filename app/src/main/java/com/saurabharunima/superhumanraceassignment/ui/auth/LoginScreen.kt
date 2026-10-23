package com.saurabharunima.superhumanraceassignment.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saurabharunima.superhumanraceassignment.data.model.SocialProvider
import com.saurabharunima.superhumanraceassignment.ui.components.PillButton
import com.saurabharunima.superhumanraceassignment.ui.components.PillStyle
import com.saurabharunima.superhumanraceassignment.ui.components.StatusBarIcons
import com.saurabharunima.superhumanraceassignment.ui.theme.AppTheme
import com.saurabharunima.superhumanraceassignment.ui.theme.SuperHumanTheme
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    onGoToSignUp: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = viewModel::login,
        onSocial = viewModel::loginWithProvider,
        onGoToSignUp = onGoToSignUp,
    )
}

@Composable
fun LoginScreen(
    state: AuthFormState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onSocial: (SocialProvider) -> Unit,
    onGoToSignUp: () -> Unit,
) {
    StatusBarIcons(lightBackground = true)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
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
            Spacer(Modifier.height(8.dp))

            SocialProvider.entries.forEach { provider ->
                PillButton(
                    text = "Log in with ${provider.label}",
                    onClick = { onSocial(provider) },
                    modifier = Modifier.fillMaxWidth(),
                    style = PillStyle.Light,
                    enabled = !state.isLoading,
                )
            }

            HorizontalDivider(color = AppTheme.colors.cardBorder)
            Text(
                "Sign in with Email",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
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
                label = "Password",
                error = state.passwordError,
                isPassword = true,
                imeAction = ImeAction.Done,
                enabled = !state.isLoading,
                onDone = onLogin,
            )
            state.error?.let { AuthErrorText(it) }
            PillButton(
                text = if (state.isLoading) "Signing in…" else "Sign in with Email",
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Filled.Email,
                enabled = !state.isLoading,
            )

            HorizontalDivider(color = AppTheme.colors.cardBorder)
            Text(
                "Do you work for an organization that uses SSO?",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            PillButton(
                text = "Log in with SSO",
                onClick = { scope.launch { snackbar.showSnackbar("SSO is not available in this demo") } },
                modifier = Modifier.fillMaxWidth(),
                style = PillStyle.Light,
                leadingIcon = Icons.Filled.Lock,
                enabled = !state.isLoading,
            )

            Text(
                "Demo account: ava@example.com / password123",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(onClick = onGoToSignUp, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Go to Registration", style = MaterialTheme.typography.labelLarge)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LoginPreview() = SuperHumanTheme(darkTheme = false) {
    LoginScreen(AuthFormState(emailError = "Enter a valid email address"), {}, {}, {}, {}, {})
}
