package com.rayven0421.ashnotes.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rayven0421.ashnotes.R

@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AshNotesTheme.colors.base)
            .padding(AshNotesTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = AshNotesTheme.colors.ember,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = AshNotesTheme.spacing.medium)
        )
        Text(
            text = stringResource(R.string.lock_screen_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = AshNotesTheme.spacing.extraSmall)
        )
        Text(
            text = stringResource(R.string.lock_screen_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = AshNotesTheme.colors.textMuted,
            modifier = Modifier.padding(bottom = AshNotesTheme.spacing.large)
        )
        Button(
            onClick = onUnlock,
            colors = ButtonDefaults.buttonColors(
                containerColor = AshNotesTheme.colors.ember,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = AshSmallShape
        ) {
            Text(text = stringResource(R.string.action_unlock))
        }
    }
}
