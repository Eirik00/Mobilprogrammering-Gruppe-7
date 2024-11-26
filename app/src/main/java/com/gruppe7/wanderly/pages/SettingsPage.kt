package com.gruppe7.wanderly.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gruppe7.wanderly.BuildConfig
import com.gruppe7.wanderly.SettingsViewModel

const val AppVersion = BuildConfig.VERSION_NAME

@Composable
fun SettingsPage(
    settingsViewModel: SettingsViewModel,
    context: Context,
    userId: String
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
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(8.dp)
        ) {
            // Dark Mode Toggle
            SettingToggle(
                titleName = "Dark Mode",
                settingToggle = settingsViewModel.isDarkTheme.collectAsState().value,
                onToggle = {
                    settingsViewModel.toggleDarkTheme()
                }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp
            )
            // High Contrast Toggle
            SettingToggle(
                titleName = "High Contrast",
                settingToggle = settingsViewModel.highContrast.collectAsState().value,
                onToggle = {
                    settingsViewModel.toggleHighContrast()
                }
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface,
                thickness = 1.dp
            )
            // Delete All Trips Button
            Button(
                onClick = {
                    settingsViewModel.deleteAllUserTrips(context, userId) { success ->
                        if (success) {
                            Toast.makeText(context, "All trips deleted successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to delete trips.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Delete All My Trips")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // App Version Display
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
        Text(
            text = titleName,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = settingToggle,
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
