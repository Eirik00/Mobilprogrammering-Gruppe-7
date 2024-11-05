package com.gruppe7.wanderly.pages

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.gruppe7.wanderly.TripObject

private const val TAG = "SearchPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(searchText: String, onBack: () -> Unit) {
    var trips by remember { mutableStateOf<List<TripObject>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("trips")
            .get()
            .addOnSuccessListener { result ->
                trips = result.map { document -> document.toObject(TripObject::class.java) }
                Log.d(TAG, "Trips fetched: $trips")
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
                isLoading = false
            }
    }

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
