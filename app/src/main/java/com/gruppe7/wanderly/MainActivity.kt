package com.gruppe7.wanderly

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.compose.AppTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
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

            val db = Firebase.firestore

            db.collection("trips")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }

            val isDarkTheme = isSystemInDarkTheme() // Henter ut telefonens darkmode bool
            val settingsViewModel = remember { SettingsViewModel(isDarkTheme = isDarkTheme) }
            val authViewModel = remember { AuthViewModel()}

            AppTheme(
                darkTheme = settingsViewModel.isDarkTheme.collectAsState().value,
                highContrast = settingsViewModel.highContrast.collectAsState().value
            ) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    MainLayout(authViewModel) { innerPadding, selectedIndex ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (selectedIndex) {
                                0 -> LandingPage(innerPadding)
                                1 -> TripPage()
                                2 -> MapPage(innerPadding)
                                3 -> ProfilePage()
                                4 -> SettingsPage(settingsViewModel)
                                5 -> Login(authViewModel)
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
fun MainLayout(authViewModel: AuthViewModel? = null, content: @Composable (PaddingValues, Int) -> Unit) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }

    fun navigateToLogin() {
        selectedItem = 5
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
        content(innerPadding, selectedItem)
    }
}

