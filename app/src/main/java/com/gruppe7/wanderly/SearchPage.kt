package com.gruppe7.wanderly

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.GeoPoint


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(searchText: String, onBack: () -> Unit) {
    val trips = listOf(
        Trip("Midgard vikingsenter - 1 day", "Historical", GeoPoint(59.3870247516921, 10.466289217259918),
            "Explore Viking history with a full day at Midgard.", "Camera, Water Bottle, Snacks",
            GeoPoint(59.30765675697069, 11.087157826950184), "https://vestfoldmuseene.no/midgard-vikingsenter/utstillinger"
        ),
        Trip("Vansjø - 3 days", "Adventure", GeoPoint(59.354477808278475, 10.923720027315635),
            "Enjoy scenic views and outdoor activities over three days.", "Tent, Sleeping Bag, Food Supplies",
            GeoPoint(59.444123, 10.694452), ""
        ),
        Trip("Moss - Historic Tour", "Culture", GeoPoint(59.434, 10.658),
            "Discover the rich history of Moss.", "Map, Camera, Comfortable Shoes",
            GeoPoint(59.440, 10.650), ""
        )
    )

    // Filter trips based on the location parameter
    val filteredTrips = trips.filter { trip ->
        trip.name.contains(searchText, ignoreCase = true) || trip.type.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Results for \"$searchText\"") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (filteredTrips.isEmpty()) {
                Text("No trips found for \"$searchText\"", modifier = Modifier.padding(16.dp))
            } else {
                filteredTrips.forEach { trip ->
                    SavedTripCard(trip = trip, onClick = { /* Handle trip selection */ })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
