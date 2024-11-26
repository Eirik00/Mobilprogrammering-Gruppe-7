package com.gruppe7.wanderly.pages

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.firebase.firestore.GeoPoint
import com.gruppe7.wanderly.TripObject
import com.gruppe7.wanderly.TripsViewModel
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Main Navigation Setup
fun NavGraphBuilder.tripNavigationGraph(
    navController: NavController,
    tripsViewModel: TripsViewModel,
    userId: String
) {
    composable("trips") {
        TripPage(navController, tripsViewModel, userId)
    }
    composable("trips/create") {
        CreateTripPage(
            tripsViewModel = tripsViewModel,
            onBack = { navController.navigateUp() },
            userId = userId
        )
    }
    composable("trips/popular") {
        PopularTripsPage(
            tripsViewModel = tripsViewModel,
            onBack = { navController.navigateUp() }
        )
    }
    composable("trips/discover") {
        FindMoreTripsPage(
            tripsViewModel = tripsViewModel,
            onBack = { navController.navigateUp() }
        )
    }
    composable("trips/search/{query}") { backStackEntry ->
        val query = backStackEntry.arguments?.getString("query") ?: ""
        SearchPage(
            tripsViewModel = tripsViewModel,
            searchText = query,
            onBack = { navController.navigateUp() }
        )
    }
}

@Composable
fun TripPage(
    navController: NavController,
    tripsViewModel: TripsViewModel,
    userId: String
) {
    var selectedTrip by remember { mutableStateOf<TripObject?>(null) }
    val savedTrips = tripsViewModel.savedTrips.collectAsState().value[userId] ?: emptyList()
    val context = LocalContext.current

    tripsViewModel.fetchTrips()
    tripsViewModel.loadSavedTripsLocally(context, userId)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (userId == "defaultUserId") {
                        Toast.makeText(
                            context,
                            "You must be logged in to create a trip.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        navController.navigate("trips/create")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                onSearch = { query ->
                    navController.navigate("trips/search/$query")
                }
            )

            TripSectionButtons(
                onPopularClick = { navController.navigate("trips/popular") },
                onDiscoverClick = { navController.navigate("trips/discover") }
            )

            SavedTripsSection(
                savedTrips = savedTrips,
                onTripClick = { selectedTrip = it }
            )
        }
    }

    // Trip Dialog
    selectedTrip?.let { trip ->
        TripDialog(
            trip = trip,
            onDismiss = { selectedTrip = null },
            onSaveOrDelete = {
                if (trip.savedLocally) {
                    tripsViewModel.deleteTripLocally(context, userId, trip.id)
                } else {
                    tripsViewModel.saveTripLocally(context, userId, trip)
                }
                selectedTrip = null
            },
            onDeleteFromFirebase = {
                tripsViewModel.deleteTripFromFirebase(context, userId, trip.id)
                selectedTrip = null
            },
            startOrStopTrip = {
                tripsViewModel.startOrStopTrip(context, trip, userId)
            },
            showDeleteButton = false
        )
    }
}

@Composable
private fun SearchBar(onSearch: (String) -> Unit) {
    var searchText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search location") },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )

        Button(
            onClick = { onSearch(searchText) },
            enabled = searchText.isNotEmpty()
        ) {
            Text("Search")
        }
    }
}

@Composable
private fun TripSectionButtons(
    onPopularClick: () -> Unit,
    onDiscoverClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = onPopularClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Popular Trips")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onDiscoverClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Discover More Trips")
        }
    }
}

@Composable
private fun SavedTripsSection(
    savedTrips: List<TripObject>,
    onTripClick: (TripObject) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Saved Trips",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        savedTrips.forEach { trip ->
            TripCard(
                trip = trip,
                onClick = { onTripClick(trip) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TripDialog(
    trip: TripObject,
    onSaveOrDelete: () -> Unit,
    startOrStopTrip: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onDeleteFromFirebase: () -> Unit,
    showDeleteButton: Boolean

) {
    val context = LocalContext.current
    val geocoder = Geocoder(context, Locale.getDefault())
    var isStarted by remember { mutableStateOf(trip.started) }


    var startAddress by remember { mutableStateOf("Loading...") }
    var endAddress by remember { mutableStateOf("Loading...") }

    LaunchedEffect(trip.startPoint) {
        startAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            newGetAddressFromGeoPoint(geocoder, trip.startPoint)
        } else {
            oldGetAddressFromGeoPoint(geocoder, trip.startPoint)
        }
    }

    LaunchedEffect(trip.endPoint) {
        endAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            newGetAddressFromGeoPoint(geocoder, trip.endPoint)
        } else {
            oldGetAddressFromGeoPoint(geocoder, trip.endPoint)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = trip.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text("Description: ${trip.description}", fontSize = 14.sp)
            Text("Start point: $startAddress", fontSize = 14.sp)
            Text("End point: $endAddress", fontSize = 14.sp)
            Text("Packing list: ${trip.packingList}", fontSize = 14.sp)
            Text("Length: ${trip.lengthInKm} Km", fontSize = 14.sp)
            Text("Trip duration: ${trip.tripDurationInMinutes} minutes", fontSize = 14.sp)

            if (trip.waypoints.isNotEmpty()) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    onSaveOrDelete()
                    Log.d("TripDialog", "Save/Delete Trip Clicked!")
                }) {
                    Text(if (trip.savedLocally) "Delete Trip" else "Save Trip")
                }

                if (showDeleteButton) {
                    Button(onClick = {
                        onDeleteFromFirebase()
                        Log.d("TripDialog", "Delete Trip from Firebase Clicked!")
                    }) {
                        Text("Delete from Firebase")
                    }
                }

                if (startOrStopTrip != null) {
                    if (trip.savedLocally) {
                        Button(onClick = {
                                startOrStopTrip.invoke()
                                isStarted = trip.started

                            val message = if(trip.started){
                                "Trip started: ${trip.name}"
                            }else{
                                "Trip stopped: ${trip.name}"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        }) {
                            Text(if (isStarted) "Stop Trip" else "Start Trip")
                        }
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun TripCard(
    trip: TripObject,
    onClick: () -> Unit,
) {
    val locations = rememberLocationAddresses(trip)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = trip.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(trip.description)
            Text("From: ${locations.startAddress}")
            Text("To: ${locations.endAddress}")
            Text("Distance: ${trip.lengthInKm}km")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
suspend fun newGetAddressFromGeoPoint(geocoder: Geocoder, position: GeoPoint): String {
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

fun oldGetAddressFromGeoPoint(geocoder: Geocoder, position: GeoPoint): String {
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
private fun rememberLocationAddresses(trip: TripObject): LocationAddresses {
    val context = LocalContext.current
    var startAddress by remember { mutableStateOf("Loading...") }
    var endAddress by remember { mutableStateOf("Loading...") }



    LaunchedEffect(trip) {
        val geocoder = Geocoder(context, Locale.getDefault())
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            startAddress = newGetAddressFromGeoPoint(geocoder, trip.startPoint)
            endAddress = newGetAddressFromGeoPoint(geocoder, trip.endPoint)
        }else{
            startAddress = oldGetAddressFromGeoPoint(geocoder, trip.startPoint)
            endAddress = oldGetAddressFromGeoPoint(geocoder, trip.endPoint)
        }
    }

    return LocationAddresses(startAddress, endAddress)
}

private data class LocationAddresses(
    val startAddress: String,
    val endAddress: String
)