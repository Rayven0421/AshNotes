package com.rayven0421.ashnotes.feature.settings

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.BiometricUtils
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.AshSmallShape
import com.rayven0421.ashnotes.core.designsystem.TabScreenLayout
import com.rayven0421.ashnotes.core.designsystem.dialog.AshConfirmDialog
import com.rayven0421.ashnotes.core.designsystem.dialog.AshDialog
import com.rayven0421.ashnotes.core.designsystem.dialog.AshTextInputDialog

tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

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
    var showWorkspacesDialog by remember { mutableStateOf(false) }
    var showNewWorkspaceDialog by remember { mutableStateOf(false) }

    val dataClearedMsg = stringResource(R.string.snackbar_data_deleted)
    val errorEmptyMsg = stringResource(R.string.error_workspace_empty)
    val errorTooLongMsg = stringResource(R.string.error_workspace_too_long)
    val errorDuplicateMsg = stringResource(R.string.error_workspace_duplicate)

    val context = LocalContext.current
    val fragmentActivity = context.findFragmentActivity()
    val biometricNotAvailableMsg = stringResource(R.string.biometric_not_available)
    val promptTitle = stringResource(R.string.biometric_prompt_title)
    val promptSubtitle = stringResource(R.string.biometric_prompt_subtitle)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settingsContent = (uiState as? UiState.Success)?.data ?: SettingsContent()
    val syncDialogState by viewModel.syncDialogState.collectAsStateWithLifecycle()

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

    if (showWorkspacesDialog) {
        AshDialog(onDismissRequest = { showWorkspacesDialog = false }) {
            Text(
                text = stringResource(R.string.settings_workspaces_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.none)
            ) {
                settingsContent.workspaces.forEach { workspace ->
                    val isActive = workspace.id == settingsContent.activeWorkspaceId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectWorkspace(workspace.id)
                                showWorkspacesDialog = false
                            }
                            .padding(vertical = AshNotesTheme.spacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = workspace.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isActive) AshNotesTheme.colors.ember else MaterialTheme.colorScheme.onBackground
                        )
                        if (isActive) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.titleMedium,
                                color = AshNotesTheme.colors.ember
                            )
                        }
                    }
                    HorizontalDivider(
                        color = AshNotesTheme.colors.line,
                        thickness = AshNotesTheme.spacing.hairline
                    )
                }
            }
            Button(
                onClick = {
                    showWorkspacesDialog = false
                    showNewWorkspaceDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AshNotesTheme.colors.ember,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = AshSmallShape
            ) {
                Text(text = stringResource(R.string.settings_workspaces_new_action))
            }
        }
    }

    if (showNewWorkspaceDialog) {
        AshTextInputDialog(
            title = stringResource(R.string.dialog_new_workspace_title),
            label = stringResource(R.string.field_workspace_name_label),
            confirmLabel = stringResource(R.string.action_create),
            cancelLabel = stringResource(R.string.action_cancel),
            maxLength = 40,
            allowBlank = false,
            blankFallbackValue = "",
            onConfirm = { name ->
                if (name.isNotBlank()) {
                    viewModel.createWorkspace(name, errorEmptyMsg, errorTooLongMsg, errorDuplicateMsg)
                    showNewWorkspaceDialog = false
                }
            },
            onDismiss = { showNewWorkspaceDialog = false }
        )
    }

    when (val state = syncDialogState) {
        SyncDialogState.None -> {}
        SyncDialogState.ChooseAction -> {
            AshDialog(onDismissRequest = { viewModel.dismissSyncDialog() }) {
                Text(
                    text = stringResource(R.string.settings_sync_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.settings_sync_signed_in_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshNotesTheme.colors.textMuted
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AshNotesTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small)
                ) {
                    Button(
                        onClick = {
                            viewModel.dismissSyncDialog()
                            viewModel.backupToCloud()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AshNotesTheme.colors.ember,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = AshSmallShape
                    ) {
                        Text(text = stringResource(R.string.action_backup_now))
                    }
                    Button(
                        onClick = {
                            viewModel.dismissSyncDialog()
                            viewModel.requestRestoreConfirmation()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = AshSmallShape
                    ) {
                        Text(text = stringResource(R.string.action_restore_cloud))
                    }
                }
            }
        }
        SyncDialogState.ConfirmRestore -> {
            AshConfirmDialog(
                title = stringResource(R.string.dialog_restore_title),
                message = stringResource(R.string.dialog_restore_message),
                confirmLabel = stringResource(R.string.action_restore_cloud),
                cancelLabel = stringResource(R.string.action_cancel),
                onConfirm = { viewModel.restoreFromCloud() },
                onDismiss = { viewModel.dismissSyncDialog() }
            )
        }
        SyncDialogState.InProgress -> {
            AshDialog(onDismissRequest = {}) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AshNotesTheme.spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.medium)
                ) {
                    CircularProgressIndicator(color = AshNotesTheme.colors.ember)
                    Text(
                        text = stringResource(R.string.dialog_sync_in_progress),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        is SyncDialogState.Success -> {
            AshConfirmDialog(
                title = stringResource(state.titleResId),
                message = state.message,
                confirmLabel = stringResource(R.string.action_done),
                cancelLabel = "",
                onConfirm = { viewModel.dismissSyncDialog() },
                onDismiss = { viewModel.dismissSyncDialog() }
            )
        }
        is SyncDialogState.Error -> {
            AshConfirmDialog(
                title = stringResource(R.string.settings_sync_title),
                message = stringResource(state.messageResId),
                confirmLabel = stringResource(R.string.action_done),
                cancelLabel = "",
                onConfirm = { viewModel.dismissSyncDialog() },
                onDismiss = { viewModel.dismissSyncDialog() }
            )
        }
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
                    description = settingsContent.activeWorkspaceName,
                    isAvailable = true,
                    onClick = { showWorkspacesDialog = true }
                )
                HorizontalDivider(
                    color = AshNotesTheme.colors.line,
                    thickness = AshNotesTheme.spacing.hairline
                )

                // 4. Sync
                if (settingsContent.currentUser == null) {
                    SettingRow(
                        title = stringResource(R.string.settings_sync_title),
                        description = stringResource(R.string.settings_sync_sign_in),
                        isAvailable = true,
                        onClick = onNavigateToLogin
                    )
                } else {
                    SettingRow(
                        title = stringResource(R.string.settings_sync_title),
                        description = stringResource(R.string.settings_sync_signed_in_desc),
                        isAvailable = true,
                        onClick = { viewModel.requestChooseAction() }
                    )
                }
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

                // 6. Security (Biometric App Lock)
                SettingRow(
                    title = stringResource(R.string.settings_security_title),
                    description = stringResource(R.string.settings_security_desc),
                    isAvailable = true,
                    trailingContent = {
                        Switch(
                            checked = settingsContent.isBiometricEnabled,
                            onCheckedChange = { targetState ->
                                if (!BiometricUtils.canAuthenticateWithBiometrics(context)) {
                                    viewModel.showUserMessage(biometricNotAvailableMsg)
                                } else if (fragmentActivity != null) {
                                    BiometricUtils.showBiometricPrompt(
                                        activity = fragmentActivity,
                                        title = promptTitle,
                                        subtitle = promptSubtitle,
                                        onSuccess = {
                                            viewModel.setBiometricEnabled(targetState)
                                        },
                                        onError = { error ->
                                            viewModel.showUserMessage(error)
                                        }
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = AshNotesTheme.colors.ember,
                                uncheckedThumbColor = AshNotesTheme.colors.textMuted,
                                uncheckedTrackColor = AshNotesTheme.colors.raised
                            )
                        )
                    },
                    onClick = {
                        val targetState = !settingsContent.isBiometricEnabled
                        if (!BiometricUtils.canAuthenticateWithBiometrics(context)) {
                            viewModel.showUserMessage(biometricNotAvailableMsg)
                        } else if (fragmentActivity != null) {
                            BiometricUtils.showBiometricPrompt(
                                activity = fragmentActivity,
                                title = promptTitle,
                                subtitle = promptSubtitle,
                                onSuccess = {
                                    viewModel.setBiometricEnabled(targetState)
                                },
                                onError = { error ->
                                    viewModel.showUserMessage(error)
                                }
                            )
                        }
                    }
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
    trailingContent: (@Composable () -> Unit)? = null,
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
        if (trailingContent != null) {
            trailingContent()
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
