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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import com.google.firebase.FirebaseApp
import com.gruppe7.wanderly.pages.LandingPage
import com.gruppe7.wanderly.pages.MapPage
import com.gruppe7.wanderly.pages.ProfilePage
import com.gruppe7.wanderly.pages.SettingsPage
import com.gruppe7.wanderly.pages.TripPage
import com.gruppe7.wanderly.pages.tripNavigationGraph


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            val navController = rememberNavController()
            val isDarkTheme = isSystemInDarkTheme() // Henter ut telefonens darkmode bool
            val settingsViewModel = remember { SettingsViewModel(isDarkTheme = isDarkTheme) }
            val authViewModel = remember { AuthViewModel(application) }
            val tripsViewModel = remember { TripsViewModel() }

            val userId = authViewModel.user.collectAsState().value?.uid ?: "defaultUserId"

            AppTheme(
                darkTheme = settingsViewModel.isDarkTheme.collectAsState().value,
                highContrast = settingsViewModel.highContrast.collectAsState().value
            ) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    MainLayout(
                        authViewModel = authViewModel,
                        navController = navController
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.padding(innerPadding)
                        ){
                            composable("home") {
                                LandingPage(navController, tripsViewModel, userId)
                            }
                            tripNavigationGraph(navController, tripsViewModel, userId)
                            composable("map") {
                                MapPage()
                            }
                            composable("profile") {
                                ProfilePage(authViewModel, tripsViewModel)
                            }
                            composable("settings") {
                                SettingsPage(settingsViewModel)
                            }
                            composable("home/login/{mode}") { backStackEntry ->
                                Login(
                                    mode = backStackEntry.arguments?.getString("mode") ?: "none",
                                    authViewModel = authViewModel,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainLayout(
    authViewModel: AuthViewModel,
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
) {
    fun navigateToLogin(mode: String) {
        navController.navigate("home/login/$mode")
    }

    Scaffold(
        topBar = {
            if (navController.currentDestination?.route != "map") {
                Header(
                    authViewModel = authViewModel,
                    onLoginRegisterClicked = ::navigateToLogin
                )
            }
        },
        bottomBar = {
            Navbar(
                navController = navController,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("home") { saveState = true }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}