package com.rayven0421.ashnotes.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.ScreenHeader

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = AshNotesTheme.spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.large)
    ) {
        ScreenHeader(title = stringResource(R.string.settings_screen_title))

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
            SettingRow(
                title = stringResource(R.string.settings_account_title),
                description = stringResource(R.string.settings_account_desc),
                isAvailable = false
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
                description = stringResource(R.string.settings_about_desc),
                isAvailable = true
            )
        }

        Spacer(modifier = Modifier.height(AshNotesTheme.spacing.large))

        // Separated Destructive Action Section (unavailable until Room database exists)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.none)
        ) {
            SettingRow(
                title = stringResource(R.string.settings_delete_data_title),
                description = stringResource(R.string.settings_delete_data_desc),
                isAvailable = false
            )
        }

        // Bottom spacing so last row clears navigation bar comfortably when scrolled
        Spacer(modifier = Modifier.height(AshNotesTheme.spacing.huge))
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    isAvailable: Boolean
) {
    val titleColor = if (isAvailable) MaterialTheme.colorScheme.onBackground else AshNotesTheme.colors.textMuted
    val descriptionColor = AshNotesTheme.colors.textMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isAvailable, onClick = {})
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
