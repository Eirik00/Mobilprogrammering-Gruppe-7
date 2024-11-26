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
import androidx.compose.ui.graphics.ColorFilter
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import java.util.Locale

// Header Composable
@Composable
fun Header(authViewModel: AuthViewModel?, onLoginRegisterClicked: (String) -> Unit) {
    if(authViewModel == null){
        return
    }

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    var profileExpanded by remember { mutableStateOf(false) }

    val menuList = if(isLoggedIn){
        listOf("Log Out")
    }else{
        listOf("Log in", "Register")
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
                    modifier = Modifier
                        .width(250.dp)
                        .offset(x = 16.dp),
                    expanded = profileExpanded,
                    onDismissRequest = {
                        profileExpanded = false
                    }
                ) {
                    menuList.forEach {
                        DropdownMenuItem(
                            onClick = {
                                if(it == "Log in"|| it == "Register"){
                                    onLoginRegisterClicked(it)
                                }else{
                                    authViewModel.signOut()
                                } },
                            text = {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}



// Navbar Composable
@Composable
fun Navbar(
    navController: NavController,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        val items = listOf(
            "home" to R.drawable.ic_home,
            "trips" to R.drawable.ic_trip,
            "map" to R.drawable.ic_map,
            "profile" to R.drawable.ic_profile_navbar,
            "settings" to R.drawable.ic_settings
        )

        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStackEntry?.destination?.route

        items.forEach { (route, icon) ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = icon), contentDescription = route) },
                label = { Text(route.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }) },
                selected = currentRoute == route || currentRoute?.startsWith(route) == true,
                onClick = { onNavigate(route) },
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
