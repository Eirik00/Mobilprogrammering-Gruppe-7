package com.gruppe7.wanderly

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
import androidx.compose.ui.graphics.ColorFilter

// Header Composable
@Composable
fun Header(authViewModel: AuthViewModel?, onLoginRegisterClicked: () -> Unit) {
    if(authViewModel == null){
        return
    }

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    var profileExpanded by remember { mutableStateOf(false) }
    var menuList = listOf<String>()

    if(isLoggedIn){
        menuList = listOf("Log Out")
    }else{
        menuList = listOf("Log in/Register")
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
            .fillMaxWidth(),
    ) {
        Row {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo (Wanderly)",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Image(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            profileExpanded = !profileExpanded
                        },
                    painter = painterResource(id = R.drawable.default_profile_icon),
                    contentDescription = "Profile Picture",
                )
                DropdownMenu(
                    expanded = profileExpanded,
                    onDismissRequest = { profileExpanded = false }
                ) {
                    menuList.forEach {
                        DropdownMenuItem( onClick = {if(it == "Log in/Register"){onLoginRegisterClicked()} },
                            text = {Text(text=it)})
                    }
                }
            }
        }
    }
}

// Navbar Composable
@Composable
fun Navbar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf("Home", "Trips", "Map", "Profile", "Settings")

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(painterResource(id = getIconResource(item)), contentDescription = item)
                },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// Utility function to get the corresponding icon resource for each navigation item
private fun getIconResource(item: String): Int {
    return when (item) {
        "Home" -> R.drawable.ic_home
        "Trips" -> R.drawable.ic_trip
        "Map" -> R.drawable.ic_map
        "Profile" -> R.drawable.ic_profile_navbar
        "Settings" -> R.drawable.ic_settings
        else -> R.drawable.ic_default
    }
}
