package com.example.gamelibrary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gamelibrary.R

/**
 * Profile tab — TODO: user profile screen (display name/avatar, account
 * details, maybe a shortcut into the Linked Accounts section of Settings).
 * Wire up its own ViewModel + repository the same way [SettingsScreen]
 * does once the backing data source is ready.
 */
@Composable
fun ProfileScreen() {

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.nav_profile), style = MaterialTheme.typography.headlineMedium)
        Text("TODO: profile screen", style = MaterialTheme.typography.bodyMedium)
    }
}
