package com.gruppe7.wanderly.pages

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.gruppe7.wanderly.TripObject
import com.gruppe7.wanderly.TripsViewModel
import okio.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun TripPage(tripsViewModel: TripsViewModel, userId: String) {
    var showCreateTripPage by remember { mutableStateOf(false) }
    var showPopularTripsPage by remember { mutableStateOf(false) }
    var showFindMoreTripsPage by remember { mutableStateOf(false) }
    var showSearchPage by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<TripObject?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var savedTrips by remember { mutableStateOf<List<TripObject>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        fetchSavedTrips(db) { trips ->
            savedTrips = trips
        }
    }

    when {
        showCreateTripPage -> {
            CreateTripPage(onBack = { showCreateTripPage = false }, userId = userId)
        }
        showPopularTripsPage -> {
            PopularTripsPage(tripsViewModel = tripsViewModel, onBack = { showPopularTripsPage = false })
        }
        showFindMoreTripsPage -> {
            FindMoreTripsPage(tripsViewModel = tripsViewModel, onBack = { showFindMoreTripsPage = false })
        }
        showSearchPage -> {
            SearchPage(tripsViewModel, searchQuery) { showSearchPage = false }
        }
        else -> {
            Scaffold(
                floatingActionButton = { AddTripButton { showCreateTripPage = true } }
            ) { padding ->
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .fillMaxHeight()
                        .padding(padding)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SearchSection{ searchText ->
                        searchQuery = searchText
                        showSearchPage = true
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TripSections(
                        savedTrips = savedTrips,
                        onTripClick = { trip -> selectedTrip = trip },
                        navigateToPopularTrips = { showPopularTripsPage = true },
                        navigateToFindMoreTrips = { showFindMoreTripsPage = true }
                    )
                }
            }
        }
    }

    selectedTrip?.let { trip ->
        TripDialog(
            trip = trip,
            onDismiss = { selectedTrip = null },
            onSaveTrip = {
                saveTripToFirebase(trip)
                selectedTrip = null
            }
        )
    }
}

fun saveTripToFirebase(trip: TripObject) {
    val db = FirebaseFirestore.getInstance()
    db.collection("savedTrips")
        .add(trip)
        .addOnSuccessListener { Log.d("SaveTrip", "Trip saved successfully") }
        .addOnFailureListener { e -> Log.e("SaveTrip", "Error saving trip", e) }
}

fun fetchSavedTrips(db: FirebaseFirestore, onResult: (List<TripObject>) -> Unit) {
    db.collection("savedTrips")
        .addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("FetchSavedTrips", "Listen failed.", e)
                onResult(emptyList())
                return@addSnapshotListener
            }

            val trips = snapshots?.mapNotNull { document ->
                document.toObject(TripObject::class.java).copy(id = document.id)
            } ?: emptyList()

            onResult(trips)
        }
}

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
            enabled = searchText.isNotEmpty(),
            modifier = Modifier.height(56.dp)
        ) {
            Text("Search")
        }
    }
}

@Composable
fun TripSections(
    savedTrips: List<TripObject>,
    onTripClick: (TripObject) -> Unit,
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

        savedTrips.forEach { trip ->
            SavedTripCard(trip = trip, onClick = { onTripClick(trip) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
suspend fun newGetAdressFromGeoPoint(geocoder: Geocoder, position: GeoPoint): String {
    return try {
        suspendCoroutine { continuation ->
            geocoder.getFromLocation(position.latitude, position.longitude, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    val address = addresses.firstOrNull()?.let { address ->
                        val subAdminArea = address.subAdminArea ?: ""
                        val adminArea = address.adminArea ?: ""
                        if (subAdminArea.isNotEmpty() && adminArea.isNotEmpty()) {
                            "$subAdminArea, $adminArea"
                        } else {
                            subAdminArea + adminArea
                        }
                    } ?: "Unknown"
                    continuation.resume(address)
                }

                override fun onError(errorMessage: String?) {
                    continuation.resume("Unknown")
                }
            })
        }
    } catch (e: IOException) {
        "Unknown"
    }
}

fun oldGetAdressFromGeoPoint(geocoder: Geocoder, position: GeoPoint): String {
    return try {
        val addresses = geocoder.getFromLocation(
            position.latitude,
            position.longitude,
            1
        )
        addresses?.firstOrNull()?.getAddressLine(0) ?: "unknown"
    }catch (e: IOException){
        "unknown"
    }
}

@Composable
fun SavedTripCard(trip: TripObject, onClick: () -> Unit) {
    val context = LocalContext.current
    val geocoder = Geocoder(context, Locale.getDefault())

    var startAddress by remember(trip.startPoint) { mutableStateOf("Loading...") }
    var endAddress by remember(trip.endPoint) { mutableStateOf(("Loading...")) }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(trip.startPoint) {
        startAddress = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            newGetAdressFromGeoPoint(geocoder, trip.startPoint)
        }else{
            oldGetAdressFromGeoPoint(geocoder, trip.startPoint)
        }
    }

    LaunchedEffect(trip.endPoint) {
        endAddress = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            newGetAdressFromGeoPoint(geocoder, trip.endPoint)
        }else{
            oldGetAdressFromGeoPoint(geocoder, trip.endPoint)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                if(!trip.savedLocally){
                    db.collection("trips")
                        .document(trip.id)
                        .update("clickCounter", com.google.firebase.firestore.FieldValue.increment(1))
                }
                onClick()
                       },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(trip.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Description: ${trip.description}")
            Text("Start point: $startAddress, $endAddress", fontSize = 14.sp)
            Text("Length: ${trip.lengthInKm} Km")
        }
    }
}

@Composable
fun TripDialog(trip: TripObject, onDismiss: () -> Unit, onSaveTrip: () -> Unit) {
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

                if (trip.waypoints?.isNotEmpty() == true) {
                    GoogleMapTripView(
                        startPoint = trip.startPoint,
                        endPoint = trip.endPoint,
                        wayPoints = trip.waypoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else {
                    GoogleMapTripView(
                        startPoint = trip.startPoint,
                        endPoint = trip.endPoint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSaveTrip()
                onDismiss()
            }) {
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
