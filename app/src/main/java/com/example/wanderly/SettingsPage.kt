package com.example.wanderly

import SettingsViewModel
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
import androidx.compose.material3.Typography
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ui.theme.AppTypography

const val AppVersion = BuildConfig.VERSION_NAME

@Composable
fun SettingsPage(
    settingsViewModel: SettingsViewModel
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

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            // Dark Mode Toggle
            SettingToggle (titleName = "Dark Mode",
                settingToggle = settingsViewModel.isDarkTheme.collectAsState().value,
                onToggle = {
                settingsViewModel.toggleDarkTheme()
            })
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 2.dp,
            )
            SettingToggle (titleName = "High Contrast",
                settingToggle = settingsViewModel.highContrast.collectAsState().value,
                onToggle = {
                settingsViewModel.toggleHighContrast()
            })

        }

        Spacer(modifier = Modifier.height(32.dp))
        
        AppVersionDisplay(AppVersion)
    }
}

@Composable
fun SettingToggle(titleName: String, settingToggle: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = titleName, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        Switch(
            checked = settingToggle,
            onCheckedChange = { onToggle(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
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
