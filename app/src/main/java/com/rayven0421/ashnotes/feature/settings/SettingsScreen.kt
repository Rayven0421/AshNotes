package com.rayven0421.ashnotes.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.TabScreenLayout
import com.rayven0421.ashnotes.core.designsystem.dialog.AshConfirmDialog

@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    val dataClearedMsg = stringResource(R.string.snackbar_data_deleted)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settingsContent = (uiState as? UiState.Success)?.data ?: SettingsContent()

    LaunchedEffect(viewModel) {
        viewModel.userMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (showDeleteDialog) {
        AshConfirmDialog(
            title = stringResource(R.string.dialog_delete_data_title),
            message = stringResource(R.string.dialog_delete_data_message),
            confirmLabel = stringResource(R.string.action_delete_all),
            cancelLabel = stringResource(R.string.action_cancel),
            isDestructive = true,
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteLocalData(dataClearedMsg)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showSignOutDialog) {
        AshConfirmDialog(
            title = stringResource(R.string.dialog_sign_out_title),
            message = stringResource(R.string.dialog_sign_out_message),
            confirmLabel = stringResource(R.string.action_sign_out),
            cancelLabel = stringResource(R.string.action_cancel),
            isDestructive = false,
            onConfirm = {
                showSignOutDialog = false
                viewModel.signOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    TabScreenLayout(
        title = stringResource(R.string.settings_screen_title),
        modifier = modifier,
        snackbarHostState = snackbarHostState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = AshNotesTheme.spacing.listBottomPadding),
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.large)
        ) {
            // Grouped plain setting rows
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.none)
            ) {
                // 1. Appearance (Active)
                SettingRow(
                    title = stringResource(R.string.settings_appearance_title),
                    description = stringResource(R.string.settings_appearance_desc),
                    isAvailable = true
                )
                HorizontalDivider(
                    color = AshNotesTheme.colors.line,
                    thickness = AshNotesTheme.spacing.hairline
                )

                // 2. Account
                val accountDesc = if (settingsContent.currentUser != null && !settingsContent.currentUser.email.isNullOrEmpty()) {
                    stringResource(R.string.settings_account_signed_in, settingsContent.currentUser.email)
                } else {
                    stringResource(R.string.settings_account_local)
                }
                SettingRow(
                    title = stringResource(R.string.settings_account_title),
                    description = accountDesc,
                    isAvailable = true,
                    onClick = {
                        if (settingsContent.currentUser != null) {
                            showSignOutDialog = true
                        } else {
                            onNavigateToLogin()
                        }
                    }
                )
                HorizontalDivider(
                    color = AshNotesTheme.colors.line,
                    thickness = AshNotesTheme.spacing.hairline
                )

                // 3. Workspaces
                SettingRow(
                    title = stringResource(R.string.settings_workspaces_title),
                    description = stringResource(R.string.settings_workspaces_desc),
                    isAvailable = false
                )
                HorizontalDivider(
                    color = AshNotesTheme.colors.line,
                    thickness = AshNotesTheme.spacing.hairline
                )

                // 4. Sync
                SettingRow(
                    title = stringResource(R.string.settings_sync_title),
                    description = stringResource(R.string.settings_sync_desc),
                    isAvailable = false
                )
                HorizontalDivider(
                    color = AshNotesTheme.colors.line,
                    thickness = AshNotesTheme.spacing.hairline
                )

                // 5. Backup & restore
                SettingRow(
                    title = stringResource(R.string.settings_backup_title),
                    description = stringResource(R.string.settings_backup_desc),
                    isAvailable = false
                )
                HorizontalDivider(
                    color = AshNotesTheme.colors.line,
                    thickness = AshNotesTheme.spacing.hairline
                )

                // 6. Security
                SettingRow(
                    title = stringResource(R.string.settings_security_title),
                    description = stringResource(R.string.settings_security_desc),
                    isAvailable = false
                )
                HorizontalDivider(
                    color = AshNotesTheme.colors.line,
                    thickness = AshNotesTheme.spacing.hairline
                )

                // 7. About (Active / Informational)
                SettingRow(
                    title = stringResource(R.string.settings_about_title),
                    description = stringResource(
                        R.string.settings_about_desc,
                        com.rayven0421.ashnotes.BuildConfig.VERSION_NAME,
                        com.rayven0421.ashnotes.BuildConfig.BUILD_TYPE
                    ),
                    isAvailable = true
                )
            }

            // Destructive Action Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.none)
            ) {
                SettingRow(
                    title = stringResource(R.string.settings_delete_data_title),
                    description = stringResource(R.string.settings_delete_data_desc),
                    isAvailable = true,
                    isDestructive = true,
                    onClick = { showDeleteDialog = true }
                )
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    isAvailable: Boolean,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val titleColor = when {
        !isAvailable -> AshNotesTheme.colors.textMuted
        isDestructive -> AshNotesTheme.colors.over
        else -> MaterialTheme.colorScheme.onBackground
    }
    val descriptionColor = AshNotesTheme.colors.textMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isAvailable, onClick = onClick)
            .padding(vertical = AshNotesTheme.spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.extraSmall)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = descriptionColor
            )
        }
    }
}

@Preview(name = "Settings - Default", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun SettingsScreenPreview() {
    AshNotesTheme {
        SettingsScreen()
    }
}

