package com.example.lifetrack.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.lifetrack.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    titleRes: Int,
    descriptionRes: Int,
    modifier: Modifier = Modifier,
    onOpenSettings: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = stringResource(titleRes)) },
            navigationIcon = {
                if (onBack != null) {
                    TextButton(onClick = onBack) {
                        Text(text = stringResource(R.string.action_back))
                    }
                }
            },
            actions = {
                if (onOpenSettings != null) {
                    TextButton(onClick = onOpenSettings) {
                        Text(text = stringResource(R.string.settings_title))
                    }
                }
            },
            windowInsets = WindowInsets(0)
        )
        Text(
            text = stringResource(descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}