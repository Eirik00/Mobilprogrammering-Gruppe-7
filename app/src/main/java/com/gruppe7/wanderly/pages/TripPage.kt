package com.gruppe7.wanderly.pages

import android.location.Address
import android.location.Geocoder
import android.os.Build
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
import com.google.firebase.firestore.GeoPoint
import com.gruppe7.wanderly.TripObject
import com.gruppe7.wanderly.TripsViewModel
import okio.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun TripPage(tripsViewModel: TripsViewModel) {
    var showCreateTripPage by remember { mutableStateOf(false) }
    var showPopularTripsPage by remember { mutableStateOf(false) }
    var showFindMoreTripsPage by remember { mutableStateOf(false) }
    var showSearchPage by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<TripObject?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    when {
        showCreateTripPage -> {
            CreateTripPage(onBack = { showCreateTripPage = false })
        }
        showPopularTripsPage -> {
            PopularTripsPage(onBack = { showPopularTripsPage = false })
        }
        showFindMoreTripsPage -> {
            FindMoreTripsPage(onBack = { showFindMoreTripsPage = false })
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
                        onTripClick = { trip -> selectedTrip = trip },
                        navigateToPopularTrips = { showPopularTripsPage = true },
                        navigateToFindMoreTrips = { showFindMoreTripsPage = true }
                    )
                }
            }
        }
    }

    selectedTrip?.let { trip ->
        SavedTripDialog(
            trip = trip,
            onDismiss = { selectedTrip = null }
        )
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
            modifier = Modifier.height(56.dp)
        ) {
            Text("Search")
        }
    }
}

@Composable
fun TripSections(
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

        val trips = listOf(
            TripObject(
                name = "Midgard vikingsenter - 1 day",
                type = "Historical",
                startPoint = GeoPoint(59.3868641793198, 10.464558706166349),
                description = "Explore Viking history with a full day at Midgard.",
                packingList = listOf<String>("Camera", "Water Bottle", "Snacks"),
                endPoint = GeoPoint(59.30765675697069, 11.087157826950184),
                images = listOf<String>("https://vestfoldmuseene.no/midgard-vikingsenter/utstillinger"),
                lengthInKm = 20.0,
                minutesToWalkByFoot = 2,
            ),
            TripObject(
                name = "Vansjø - 3 days",
                type = "Adventure",
                startPoint = GeoPoint(59.354477808278475, 10.923720027315635),
                description = "Enjoy scenic views and outdoor activities over three days.",
                packingList = listOf<String>("Camera", "Water Bottle", "Snacks"),
                endPoint = GeoPoint(59.444123, 10.694452),
                images = listOf<String>("https://vestfoldmuseene.no/midgard-vikingsenter/utstillinger"),
                lengthInKm = 20.0,
                minutesToWalkByFoot = 2,
            )
        )

        trips.forEach { trip ->
            SavedTripCard(trip = trip, onClick = { onTripClick(trip) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private suspend fun newGetAdressFromGeoPoint(geocoder: Geocoder, position: GeoPoint): String {
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

private fun oldGetAdressFromGeoPoint(geocoder: Geocoder, position: GeoPoint): String {
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(trip.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Type: ${trip.type}", fontSize = 14.sp)
            Text("Description: ${trip.description}")
            Text("Start point: $startAddress, $endAddress", fontSize = 14.sp)
            Text("Length: ${trip.lengthInKm} Km")
        }
    }
}

@Composable
fun SavedTripDialog(trip: TripObject, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = trip.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Type: ${trip.type}", fontSize = 16.sp)
                Text("Start: ${trip.startPoint.latitude}, ${trip.startPoint.longitude}", fontSize = 16.sp)
                Text("Description: ${trip.description}", fontSize = 16.sp)
                Text("Packing List: ${trip.packingList}", fontSize = 16.sp)
                Text("Destination: ${trip.endPoint.latitude}, ${trip.endPoint.longitude}", fontSize = 16.sp)
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

@Composable
fun AddTripButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Text("+", color = Color.White)
    }
}
