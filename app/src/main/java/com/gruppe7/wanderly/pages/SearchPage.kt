package com.gruppe7.wanderly.pages

import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gruppe7.wanderly.TripObject
import com.gruppe7.wanderly.TripsFetchState
import com.gruppe7.wanderly.TripsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(tripsViewModel: TripsViewModel, searchText: String, onBack: () -> Unit) {
    val trips = tripsViewModel.trips.collectAsState().value
    val isLoading = tripsViewModel.tripsState.collectAsState().value == TripsFetchState.Loading

    val filteredTrips = trips.filter { trip ->
        trip.name.contains(searchText, ignoreCase = true) || trip.type.contains(searchText, ignoreCase = true)
    }

    var selectedTrip by remember { mutableStateOf<TripObject?>(null) }

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
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxHeight()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else if (filteredTrips.isEmpty()) {
                Text("No trips found for \"$searchText\"", modifier = Modifier.padding(16.dp))
            } else {
                filteredTrips.forEach { trip ->
                    SavedTripCard(trip = trip, onClick = { selectedTrip = trip })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    selectedTrip?.let { trip ->
        SearchDialog(trip = trip, onDismiss = { selectedTrip = null })
    }
}

@Composable
fun SearchDialog(trip: TripObject, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val geocoder = Geocoder(context, Locale.getDefault())

    var startAddress by remember { mutableStateOf("Loading...") }
    var endAddress by remember { mutableStateOf("Loading...") }

    LaunchedEffect(trip.startPoint) {
        startAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            newGetAdressFromGeoPoint(geocoder, trip.startPoint)
        } else {
            oldGetAdressFromGeoPoint(geocoder, trip.startPoint)
        }
    }

    LaunchedEffect(trip.endPoint) {
        endAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            newGetAdressFromGeoPoint(geocoder, trip.endPoint)
        } else {
            oldGetAdressFromGeoPoint(geocoder, trip.endPoint)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = trip.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Description: ${trip.description}", fontSize = 14.sp)
                Text("Start point: $startAddress", fontSize = 14.sp)
                Text("End point: $endAddress", fontSize = 14.sp)
                Text("Packing list: ${trip.packingList}", fontSize = 14.sp)
                Text("Length: ${trip.lengthInKm} Km", fontSize = 14.sp)
                Text("Minutes to walk by foot: ${trip.minutesToWalkByFoot}", fontSize = 14.sp)
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
