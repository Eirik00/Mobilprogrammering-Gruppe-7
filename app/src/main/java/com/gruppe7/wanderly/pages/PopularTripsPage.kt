package com.gruppe7.wanderly.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularTripsPage(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Popular trips") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scrollState)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(6) { // Create 6 PopularTripRow composables
                        PopularTripRow("Oslo Sightseeing")
                    }

                    Button(
                        onClick = { Log.d("State", "Popular Trips Clicked!") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "See More",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PopularTripRow(tripName: String) {
    var isFavorited by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .clickable { Log.d("STATE", "$tripName Clicked!") }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            tripName,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSecondary
        )

        Icon(
            imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Favorite Icon",
            tint = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.clickable { isFavorited = !isFavorited }
        )
    }
}

@Composable
fun CurrentTripRow(tripName: String, tripProgress: String, startedBool: Boolean = false) {
    Row(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (startedBool) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.secondary
            )
            .clickable { Log.d("STATE", "$tripName Clicked!") }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            tripName,
            modifier = Modifier.weight(1f),
            color = if (startedBool) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSecondary
        )
        Text(
            tripProgress,
            modifier = Modifier.align(Alignment.CenterVertically),
            color = if (startedBool) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSecondary
        )
    }
}
