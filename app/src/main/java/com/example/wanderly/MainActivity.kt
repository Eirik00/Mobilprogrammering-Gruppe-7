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
import com.example.landingpage.ui.theme.LandingPageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LandingPageTheme {
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
                                2 -> MapPage(innerPadding)                 // Map screen
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
