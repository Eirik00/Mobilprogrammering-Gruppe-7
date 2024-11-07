package com.gruppe7.wanderly.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gruppe7.wanderly.TripsFetchState
import com.gruppe7.wanderly.TripsViewModel

private const val TAG = "SearchPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(tripsViewModel: TripsViewModel, searchText: String, onBack: () -> Unit) {
    val trips = tripsViewModel.trips.collectAsState().value
    val isLoading = tripsViewModel.tripsState.collectAsState().value == TripsFetchState.Loading


    val filteredTrips = trips.filter { trip ->
        trip.name.contains(searchText, ignoreCase = true) || trip.type.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Results for \"$searchText\"") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxHeight()
                .padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else if (filteredTrips.isEmpty()) {
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
