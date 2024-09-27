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

@Composable
fun Header() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .padding(4.dp)
            .fillMaxWidth(),

        ) {
        Row() {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo (Wanderly)",
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ){
                Image(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            Log.d("STATE", "Clicked!"); // Currently debug, function to add later
                        },
                    painter = painterResource(id = R.drawable.default_profile_icon),
                    contentDescription = "Profile Picture",
                )
            }
        }
    }
}

@Composable
fun Navbar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf("Home", "Create", "Map", "Profile", "Settings")

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.secondary,

        ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = getIconResource(item)), contentDescription = item) },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) }
            )
        }
    }
}

@Composable
fun MainLayout(content: @Composable (PaddingValues, Int) -> Unit) {
    var selectedItem by remember { mutableIntStateOf(0) }
    Scaffold(
        topBar = { Header() },
        bottomBar = { Navbar(
            selectedItem = selectedItem,
            onItemSelected = { index -> selectedItem = index }
        ) }
    ) { innerPadding ->
        content(innerPadding, selectedItem)
    }
}

private fun getIconResource(item: String): Int {
    return when (item) {
        "Home" -> R.drawable.ic_home
        "Create" -> R.drawable.ic_create
        "Map" -> R.drawable.ic_map
        "Profile" -> R.drawable.ic_profile_navbar
        "Settings" -> R.drawable.ic_settings
        else -> R.drawable.ic_default
    }
}
