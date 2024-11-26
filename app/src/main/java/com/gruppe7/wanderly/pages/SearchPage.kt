package com.gruppe7.wanderly.pages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.gruppe7.wanderly.TripObject
import com.gruppe7.wanderly.TripsFetchState
import com.gruppe7.wanderly.TripsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(tripsViewModel: TripsViewModel, searchText: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val trips = tripsViewModel.trips.collectAsState().value
    val isLoading = tripsViewModel.tripsState.collectAsState().value == TripsFetchState.Loading

    val filteredTrips = trips.filter { trip ->
        trip.name.contains(searchText, ignoreCase = true) || trip.type.contains(searchText, ignoreCase = true)
    }

    var selectedTrip by remember { mutableStateOf<TripObject?>(null) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    Log.d("FindMoreTripsPage", "User ID: $userId")

    if (userId == null) {
        Log.e("FindMoreTripsPage", "User is not logged in.")
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
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else if (filteredTrips.isEmpty()) {
                Text("No trips found for \"$searchText\"", modifier = Modifier.padding(16.dp))
            } else {
                filteredTrips.forEach { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { selectedTrip = trip }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if(userId !== null) {
        selectedTrip?.let { trip ->
            TripDialog(
                trip = trip,
                onDismiss = { selectedTrip = null },
                onSaveOrDelete = {
                    if(trip.savedLocally) {
                        tripsViewModel.deleteTripLocally(context, userId, trip.id)
                        Toast.makeText(context, "Trip deleted", Toast.LENGTH_SHORT).show()
                    }else {
                        tripsViewModel.saveTripLocally(context, userId, trip)
                        Toast.makeText(context, "Trip saved", Toast.LENGTH_SHORT).show()
                    }
                },
                onDeleteFromFirebase =  {
                    if (trip.ownerID == userId) {
                        tripsViewModel.deleteTripFromFirebase(context, userId, trip.id)
                        Toast.makeText(context, "Trip deleted from Firebase", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "You are not the owner of this trip", Toast.LENGTH_SHORT).show()
                    }
                },
                showDeleteButton = false
            )
        }
    }
}