package com.example.wanderly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainLayout { innerPadding, selectedIndex ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Handle screen navigation based on selected index
                            when (selectedIndex) {
                                0 -> LandingPage(innerPadding)  // Home screen
                                1 -> CreateTripPage()           // Create trip page
                                2 -> MapPage(innerPadding)      // Map screen
                                3 -> ProfilePage()  // Profile Page
                                4 -> SettingsPage() // Settings Page

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
    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { Header() },
        bottomBar = {
            Navbar(
                selectedItem = selectedItem,
                onItemSelected = { index -> selectedItem = index }
            )
        }
    ) { innerPadding ->
        content(innerPadding, selectedItem)
    }
}
