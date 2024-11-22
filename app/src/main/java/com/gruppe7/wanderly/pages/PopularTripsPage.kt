package com.gruppe7.wanderly.pages

import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.compose.foundation.clickable
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
import com.google.firebase.firestore.FirebaseFirestore
import com.gruppe7.wanderly.TripObject
import com.gruppe7.wanderly.TripsViewModel
import java.util.Locale
import androidx.compose.ui.Alignment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularTripsPage(tripsViewModel: TripsViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedTrip by remember { mutableStateOf<TripObject?>(null) }
    val allTrips by tripsViewModel.trips.collectAsState(initial = emptyList())

    var tripsWithClicks by remember { mutableStateOf<List<TripObject>>(emptyList()) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    Log.d("FindMoreTripsPage", "User ID: $userId")

    if (userId == null) {
        Log.e("FindMoreTripsPage", "User is not logged in.")
        return
    }

    LaunchedEffect(allTrips) {
        val tripsWithUpdatedClicks = allTrips.map { trip ->
            val clickCounter = getClickCounterFromFirestore(trip.id)
            trip.copy(clickCounter = clickCounter)
        }

        tripsWithClicks = tripsWithUpdatedClicks
    }

    val sortedTrips = tripsWithClicks.sortedByDescending { it.clickCounter }

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
                .padding(16.dp)
        ) {
            Text("Most popular trips", fontWeight = FontWeight.Bold, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(8.dp))

            sortedTrips.forEachIndexed { index, trip ->
                val medal = when (index) {
                    0 -> "🥇"
                    1 -> "🥈"
                    2 -> "🥉"
                    else -> ""
                }
                PopularTripsCard(
                    trip = trip,
                    medal = medal,
                    onClick = { selectedTrip = trip }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    selectedTrip?.let { trip ->
        TripDialog(
            trip = trip,
            onDismiss = { selectedTrip = null },
            onSaveOrDelete = {
                if(trip.savedLocally) {
                    tripsViewModel.deleteTripLocally(context, userId, trip.id)
                }else {
                    tripsViewModel.saveTripLocally(context, userId, trip)
                }
            }
        )
    }
}

    suspend fun getClickCounterFromFirestore(tripId: String): Int {
    val db = FirebaseFirestore.getInstance()
    val documentSnapshot = db.collection("trips")
        .document(tripId)
        .get()
        .await()
    return documentSnapshot.getLong("clickCounter")?.toInt() ?: 0
}

@Composable
fun PopularTripsCard(trip: TripObject, medal: String, onClick: () -> Unit) {
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
            .clickable {
                db.collection("trips")
                    .document(trip.id)
                    .update("clickCounter", com.google.firebase.firestore.FieldValue.increment(1))
                onClick()
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
            ) {
            Text(trip.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Description: ${trip.description}", fontSize = 14.sp)
            Text("Start point: $startAddress", fontSize = 14.sp)
            Text("End point: $endAddress", fontSize = 14.sp)
            Text("Length: ${trip.lengthInKm} Km", fontSize = 14.sp)
        }

            if (medal.isNotEmpty()) {
                Text(
                    medal,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }
    }
}

