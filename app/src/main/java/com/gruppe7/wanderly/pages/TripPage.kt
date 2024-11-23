package com.gruppe7.wanderly.pages

import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("trips/create") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
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
        TripDetailsDialog(
            trip = trip,
            onDismiss = { selectedTrip = null },
            onSaveOrDelete = {
                if (trip.savedLocally) {
                    tripsViewModel.deleteTripLocally(context, userId, trip.id)
                } else {
                    tripsViewModel.saveTripLocally(context, userId, trip)
                }
                selectedTrip = null
            }
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
fun TripDetailsDialog(
    trip: TripObject,
    onDismiss: () -> Unit,
    onSaveOrDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(trip.name) },
        text = {
            Column {
                Text("Description: ${trip.description}")
                Text("Length: ${trip.lengthInKm} km")
                Text("Duration: ${trip.tripDurationInMinutes} minutes")
                // Add other trip details as needed
            }
        },
        confirmButton = {
            Button(
                onClick = onSaveOrDelete
            ) {
                Text(if (trip.savedLocally) "Delete Trip" else "Save Trip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TripCard(
    trip: TripObject,
    onClick: () -> Unit
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
                style = MaterialTheme.typography.titleMedium
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