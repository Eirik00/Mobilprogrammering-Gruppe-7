package com.gruppe7.wanderly.pages

import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.google.firebase.firestore.FirebaseFirestore
import com.gruppe7.wanderly.TripObject
import com.gruppe7.wanderly.TripsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindMoreTripsPage(tripsViewModel: TripsViewModel, onBack: () -> Unit) {
    LaunchedEffect(Unit) {
        tripsViewModel.loadTrips()
    }

    val allTrips by tripsViewModel.trips.collectAsState(initial = emptyList())
    var selectedTrip by remember { mutableStateOf<TripObject?>(null) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find more trips") },
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
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("All trips in database", fontWeight = FontWeight.Bold, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(8.dp))

            allTrips.forEach { trip ->
                FindMoreTripsCard(trip = trip, onClick = { selectedTrip = trip })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    selectedTrip?.let { trip ->
        FindMoreTripsDialog(trip = trip, onDismiss = { selectedTrip = null })
    }
}

@Composable
fun FindMoreTripsCard(trip: TripObject, onClick: () -> Unit) {
    val context = LocalContext.current
    val geocoder = Geocoder(context, Locale.getDefault())
    val db = FirebaseFirestore.getInstance()

    var startAddress by remember(trip.startPoint) { mutableStateOf("Loading...") }
    var endAddress by remember(trip.endPoint) { mutableStateOf("Loading...") }

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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable{
                db.collection("trips")
                    .document(trip.id)
                    .update("clickCounter", com.google.firebase.firestore.FieldValue.increment(1))

                onClick()
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(trip.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Description: ${trip.description}", fontSize = 14.sp)
            Text("Start point: $startAddress", fontSize = 14.sp)
            Text("End point: $endAddress", fontSize = 14.sp)
            Text("Length: ${trip.lengthInKm} Km", fontSize = 14.sp)
        }
    }
}

@Composable
fun FindMoreTripsDialog(trip: TripObject, onDismiss: () -> Unit) {
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
                Text("Trip duration in minutes: ${trip.tripDurationInMinutes}", fontSize = 14.sp)
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
