package com.gruppe7.wanderly.pages

import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.widget.Toast
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gruppe7.wanderly.TripObject
import com.gruppe7.wanderly.TripsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindMoreTripsPage(tripsViewModel: TripsViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val allTrips by tripsViewModel.trips.collectAsState(initial = emptyList())
    var selectedTrip by remember { mutableStateOf<TripObject?>(null) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    Log.d("FindMoreTripsPage", "User ID: $userId")

    if (userId == null) {
        Log.e("FindMoreTripsPage", "User is not logged in.")
        return
    }

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
                TripCard(
                    trip = trip,
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
                    Toast.makeText(context, "Trip deleted", Toast.LENGTH_SHORT).show()
                }else {
                    tripsViewModel.saveTripLocally(context, userId, trip)
                    Toast.makeText(context, "Trip saved", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}