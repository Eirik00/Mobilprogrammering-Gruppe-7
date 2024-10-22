package com.example.wanderly

import SettingsViewModel
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.AppTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val settingsViewModel = remember { SettingsViewModel(isDarkTheme = isDarkTheme)}

            AppTheme(
                darkTheme = settingsViewModel.isDarkTheme.collectAsState().value,
                highContrast = settingsViewModel.highContrast.collectAsState().value
            ) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    MainLayout {innerPadding, selectedIndex ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (selectedIndex) {
                                0 -> LandingPage(innerPadding)
                                1 -> CreateTripPage()
                                2 -> MapPage(innerPadding)
                                3 -> ProfilePage()
                                4 -> SettingsPage(settingsViewModel)
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
fun MainLayout(content: @Composable (PaddingValues, Int) -> Unit) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = { if(selectedItem != 2){ Header() }},
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
