package com.gruppe7.wanderly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import com.google.firebase.FirebaseApp
import com.gruppe7.wanderly.pages.LandingPage
import com.gruppe7.wanderly.pages.MapPage
import com.gruppe7.wanderly.pages.ProfilePage
import com.gruppe7.wanderly.pages.SettingsPage
import com.gruppe7.wanderly.pages.TripPage


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            val navController = rememberNavController()
            val isDarkTheme = isSystemInDarkTheme() // Henter ut telefonens darkmode bool
            val settingsViewModel = remember { SettingsViewModel(isDarkTheme = isDarkTheme) }
            val authViewModel = remember { AuthViewModel(applicationContext) }
            val tripsViewModel = remember { TripsViewModel() }

            val userId = authViewModel.user.collectAsState().value?.uid ?: "defaultUserId"

            AppTheme(
                darkTheme = settingsViewModel.isDarkTheme.collectAsState().value,
                highContrast = settingsViewModel.highContrast.collectAsState().value
            ) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    MainLayout(authViewModel) { innerPadding, selectedIndex, loginMode ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (selectedIndex) {
                                0 -> LandingPage(innerPadding)
                                1 -> TripPage(tripsViewModel, userId)
                                2 -> MapPage(innerPadding)
                                3 -> ProfilePage(authViewModel, tripsViewModel)
                                4 -> SettingsPage(settingsViewModel)
                                5 -> Login(loginMode ?: "none", authViewModel)
                                else -> Text("No page for index: $selectedIndex")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainLayout(authViewModel: AuthViewModel? = null, content: @Composable (PaddingValues, Int, String?) -> Unit) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    var mode by rememberSaveable { mutableStateOf("none") }

    fun navigateToLogin(modestr: String) {
        selectedItem = 5
        mode = modestr
    }

    Scaffold(
        topBar = { if(selectedItem != 2){ Header(authViewModel, onLoginRegisterClicked = ::navigateToLogin) }},
        bottomBar = {
            Navbar(
                selectedItem = selectedItem,
                onItemSelected = {index -> selectedItem = index}
            )
        }
    ) { innerPadding ->
        content(innerPadding, selectedItem, mode)
    }
}

