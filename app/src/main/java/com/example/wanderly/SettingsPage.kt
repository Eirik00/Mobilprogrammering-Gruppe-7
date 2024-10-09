package com.example.wanderly

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.util.Log

const val AppVersion = "Pre Alpha"

@Composable
fun SettingsPage(
    isDarkThemeEnabled: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Dark Mode Toggle
        DarkModeToggle(isDarkThemeEnabled = isDarkThemeEnabled, onToggle = onThemeChange)

        Spacer(modifier = Modifier.height(32.dp))
        
        AppVersionDisplay(AppVersion)
    }
}

@Composable
fun DarkModeToggle(isDarkThemeEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Dark Mode", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isDarkThemeEnabled,
            onCheckedChange = { onToggle(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun AppVersionDisplay(AppVersion: String) {
    Text(
        text = "App Version: $AppVersion",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 16.dp)
    )
}
