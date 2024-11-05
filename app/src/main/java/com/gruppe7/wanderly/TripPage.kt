package com.gruppe7.wanderly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.GeoPoint

@Composable
fun TripPage() {
    var showCreateTripPage by remember { mutableStateOf(false) }
    var showPopularTripsPage by remember { mutableStateOf(false) }
    var showFindMoreTripsPage by remember { mutableStateOf(false) }
    var showSearchPage by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    when {
        showCreateTripPage -> {
            CreateTripPage(onBack = { showCreateTripPage = false })
        }
        showPopularTripsPage -> {
            PopularTripsPage(onBack = { showPopularTripsPage = false })
        }
        showFindMoreTripsPage -> {
            FindMoreTripsPage(onBack = { showFindMoreTripsPage = false })
        }
        showSearchPage -> {
            SearchPage(searchQuery) { showSearchPage = false }
        }
        else -> {
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
                    SearchSection{ searchText ->
                        searchQuery = searchText
                        showSearchPage = true
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TripSections(
                        onTripClick = { trip -> selectedTrip = trip },
                        navigateToPopularTrips = { showPopularTripsPage = true },
                        navigateToFindMoreTrips = { showFindMoreTripsPage = true }
                    )
                }
            }
        }
    }

    selectedTrip?.let { trip ->
        SavedTripDialog(
            trip = trip,
            onDismiss = { selectedTrip = null }
        )
    }
}

data class Trip(
    val name: String,
    val type: String,
    val start: GeoPoint,
    val description: String,
    val packingList: String,
    val endPoint: GeoPoint,
    val imageUrl: String
)

@Composable
fun SearchSection(navigateToSearchPage: (String) -> Unit) {
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
                .weight(1f)
                .padding(end = 8.dp)
        )

        Button(
            onClick = { navigateToSearchPage(searchText) },
            modifier = Modifier.height(56.dp)
        ) {
            Text("Search")
        }
    }
}

@Composable
fun TripSections(
    onTripClick: (Trip) -> Unit,
    navigateToPopularTrips: () -> Unit,
    navigateToFindMoreTrips: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .clickable { navigateToPopularTrips() },
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
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .clickable { navigateToFindMoreTrips() },
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
                start = GeoPoint(59.3870247516921, 10.466289217259918),
                description = "Explore Viking history with a full day at Midgard.",
                packingList = "Camera, Water Bottle, Snacks",
                endPoint = GeoPoint(59.30765675697069, 11.087157826950184),
                imageUrl = "https://vestfoldmuseene.no/midgard-vikingsenter/utstillinger"
            ),
            Trip(
                name = "Vansjø - 3 days",
                type = "Adventure",
                start = GeoPoint(59.354477808278475, 10.923720027315635),
                description = "Enjoy scenic views and outdoor activities over three days.",
                packingList = "Tent, Sleeping Bag, Food Supplies",
                endPoint = GeoPoint(59.444123, 10.694452),
                imageUrl = ""
            )
        )

        trips.forEach { trip ->
            SavedTripCard(trip = TripObject(), onClick = { onTripClick(trip) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SavedTripCard(trip: TripObject, onClick: () -> Unit) {
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
            Text("Description: ${trip.description}")
            Text("Start point: ${trip.startPoint}, ${trip.startPoint}", fontSize = 14.sp)
            Text("Length: ${trip.lengthInKm}")
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
                Text("Type: ${trip.type}", fontSize = 16.sp)
                Text("Start: ${trip.start.latitude}, ${trip.start.longitude}", fontSize = 16.sp)
                Text("Description: ${trip.description}", fontSize = 16.sp)
                Text("Packing List: ${trip.packingList}", fontSize = 16.sp)
                Text("Destination: ${trip.endPoint.latitude}, ${trip.endPoint.longitude}", fontSize = 16.sp)
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
