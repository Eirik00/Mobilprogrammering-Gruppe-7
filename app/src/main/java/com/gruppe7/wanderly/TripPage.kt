package com.gruppe7.wanderly

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun TripPage() {
    var showCreateTripPage by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) } // Holds selected trip details

    if (showCreateTripPage) {
        CreateTripPage(onBack = { showCreateTripPage = false })
    } else {
        Scaffold(
            floatingActionButton = { AddTripButton { showCreateTripPage = true } }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                SearchSection()
                Spacer(modifier = Modifier.height(16.dp))
                TripSections(onTripClick = { trip -> selectedTrip = trip })
            }
        }
    }

    // Show dialog if a trip is selected
    selectedTrip?.let { trip ->
        SavedTripDialog(
            trip = trip,
            onDismiss = { selectedTrip = null })
    }
}

data class Trip(
    val name: String,
    val type: String,
    val description: String,
    val packingList: String,
    val destination: String,
    val imageUrl: String
)

@Composable
fun SearchSection() {
    var searchText by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Type location") },
            modifier = Modifier
                .weight(1f) // Makes the TextField take available space
                .padding(end = 8.dp) // Adds spacing between TextField and Button
        )

        Button(
            onClick = { /* Search action */ },
            modifier = Modifier
                .height(56.dp) // Matches the TextField's height
        ) {
            Text("Search")
        }
    }
}

@Composable
fun TripSections(onTripClick: (Trip) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Most popular trips", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Find more trips", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Saved trips", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3A3A))

        Spacer(modifier = Modifier.height(8.dp))

        val trips = listOf(
            Trip(
                name = "Midgard vikingsenter - 1 day",
                type = "Historical",
                description = "Explore Viking history with a full day at Midgard.",
                packingList = "Camera, Water Bottle, Snacks",
                destination = "Midgard vikingsenter",
                imageUrl = "drawable/https://vestfoldmuseene.no/midgard-vikingsenter/utstillinger"
            ),
            Trip(
                name = "Vansjø - 3 days",
                type = "Adventure",
                description = "Enjoy scenic views and outdoor activities over three days.",
                packingList = "Tent, Sleeping Bag, Food Supplies",
                destination = "Vansjø",
                imageUrl = ""
            )
        )

        trips.forEach { trip ->
            SavedTripCard(trip = trip, onClick = { onTripClick(trip) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SavedTripCard(trip: Trip , onClick: () -> Unit ) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),

        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(trip.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Type: ${trip.type}", fontSize = 14.sp)
            Text("Destination: ${trip.destination}", fontSize = 14.sp)
        }
    }
}

@Composable
fun SavedTripDialog(trip: Trip, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = trip.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                /* Bilde fra internett
                Image(
                    painter = rememberImagePainter(trip.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                )*/

                Text("Type: ${trip.type}", fontSize = 16.sp)
                Text("Description: ${trip.description}", fontSize = 16.sp)
                Text("Packing List: ${trip.packingList}", fontSize = 16.sp)
                Text("Destination: ${trip.destination}", fontSize = 16.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Save Trip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun AddTripButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Text("+", color = Color.White)
    }
}