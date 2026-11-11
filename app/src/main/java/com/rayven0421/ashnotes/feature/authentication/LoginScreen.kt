package com.rayven0421.ashnotes.feature.authentication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formData = (uiState as? UiState.Success)?.data ?: LoginFormData()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val onGoogleSignInClick: () -> Unit = {
        coroutineScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.signInWithGoogle(googleIdTokenCredential.idToken, onLoginSuccess)
                }
            } catch (e: GetCredentialCancellationException) {
                // User cancelled — do nothing
            } catch (e: NoCredentialException) {
                viewModel.setGeneralError(R.string.auth_error_no_google_account)
            } catch (e: Exception) {
                viewModel.setGeneralError(R.string.auth_error_network)
            }
        }
    }

    LoginContent(
        formData = formData,
        onEmailChanged = viewModel::updateEmail,
        onPasswordChanged = viewModel::updatePassword,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onSignInClick = { viewModel.signIn(onLoginSuccess) },
        onGoogleSignInClick = onGoogleSignInClick,
        onNavigateToRegister = onNavigateToRegister,
        onContinueWithoutAccount = { viewModel.continueWithoutAccount(onContinueWithoutAccount) },
        modifier = modifier
    )
}

@Composable
private fun LoginContent(
    formData: LoginFormData,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSignInClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = AshNotesTheme.spacing.screenHorizontal)
                .padding(top = 40.dp, bottom = AshNotesTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.default)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)
            ) {
                Text(
                    text = stringResource(R.string.auth_login_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.auth_login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshNotesTheme.colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(AshNotesTheme.spacing.small))

            OutlinedTextField(
                value = formData.email,
                onValueChange = onEmailChanged,
                label = { Text(stringResource(R.string.auth_email_label)) },
                placeholder = { Text(stringResource(R.string.auth_email_hint)) },
                isError = formData.emailErrorResId != null,
                supportingText = formData.emailErrorResId?.let { resId ->
                    { Text(text = stringResource(resId), color = AshNotesTheme.colors.over) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AshNotesTheme.colors.raised,
                    unfocusedContainerColor = AshNotesTheme.colors.raised,
                    focusedBorderColor = AshNotesTheme.colors.ember,
                    unfocusedBorderColor = AshNotesTheme.colors.line
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formData.password,
                onValueChange = onPasswordChanged,
                label = { Text(stringResource(R.string.auth_password_label)) },
                placeholder = { Text(stringResource(R.string.auth_password_hint)) },
                isError = formData.passwordErrorResId != null,
                supportingText = formData.passwordErrorResId?.let { resId ->
                    { Text(text = stringResource(resId), color = AshNotesTheme.colors.over) }
                },
                singleLine = true,
                visualTransformation = if (formData.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (formData.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = stringResource(
                                if (formData.isPasswordVisible) R.string.auth_hide_password else R.string.auth_show_password
                            ),
                            tint = AshNotesTheme.colors.textMuted
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        onSignInClick()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AshNotesTheme.colors.raised,
                    unfocusedContainerColor = AshNotesTheme.colors.raised,
                    focusedBorderColor = AshNotesTheme.colors.ember,
                    unfocusedBorderColor = AshNotesTheme.colors.line
                ),
                modifier = Modifier.fillMaxWidth()
            )

            formData.generalErrorResId?.let { resId ->
                Text(
                    text = stringResource(resId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshNotesTheme.colors.over,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AshNotesTheme.spacing.extraSmall)
                )
            }

            Spacer(modifier = Modifier.height(AshNotesTheme.spacing.small))

            Button(
                onClick = onSignInClick,
                enabled = !formData.isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AshNotesTheme.colors.ember,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                if (formData.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.auth_action_sign_in),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            OutlinedButton(
                onClick = onGoogleSignInClick,
                enabled = !formData.isSubmitting,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AshNotesTheme.colors.raised,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                border = BorderStroke(AshNotesTheme.spacing.hairline, AshNotesTheme.colors.line),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.auth_continue_with_google),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.auth_switch_to_register),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshNotesTheme.colors.ember
                )
            }

            Spacer(modifier = Modifier.height(AshNotesTheme.spacing.small))

            TextButton(
                onClick = onContinueWithoutAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.auth_action_continue_local),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshNotesTheme.colors.textMuted
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121116)
@Composable
private fun LoginScreenPreview() {
    AshNotesTheme {
        LoginContent(
            formData = LoginFormData(),
            onEmailChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onSignInClick = {},
            onGoogleSignInClick = {},
            onNavigateToRegister = {},
            onContinueWithoutAccount = {}
        )
    }
}
