package com.gruppe7.wanderly.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

enum class TransportationMode(val displayName: String) {
    CAR("Car"),
    BIKE("Bike"),
    WALK("Walk")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripPage(onBack: () -> Unit, userId: String) {
    var tripName by remember { mutableStateOf("") }
    var tripStartLatitude by remember { mutableStateOf("") }
    var tripStartLongitude by remember { mutableStateOf("") }
    var tripEndLatitude by remember { mutableStateOf("") }
    var tripEndLongitude by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var packingList by remember { mutableStateOf(mutableListOf<String>()) }
    var lengthInKm by remember { mutableStateOf<Double?>(null) }
    var tripDurationInMinutes by remember { mutableStateOf<Int?>(null) }
    var waypoints by remember { mutableStateOf(mutableListOf<String>()) }
    var images by remember { mutableStateOf(mutableListOf<String>()) }
    var selectedMode by remember { mutableStateOf(TransportationMode.WALK) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create new trip") },
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
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TextField(
                value = tripName,
                onValueChange = { tripName = it },
                label = { Text("Trip name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = tripStartLatitude,
                onValueChange = { tripStartLatitude = it },
                label = { Text("Start Point Latitude") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = tripStartLongitude,
                onValueChange = { tripStartLongitude = it },
                label = { Text("Start Point Longitude") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = tripEndLatitude,
                onValueChange = { tripEndLatitude = it },
                label = { Text("End Point Latitude") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = tripEndLongitude,
                onValueChange = { tripEndLongitude = it },
                label = { Text("End Point Longitude") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = packingList.joinToString(", "),
                onValueChange = { input ->
                    packingList = input.split(", ").map { it.trim() }.toMutableList()
                },
                label = { Text("Packing list") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedMode.displayName,
                    onValueChange = {},
                    label = { Text("Mode of Transport") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    TransportationMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.displayName) },
                            onClick = {
                                selectedMode = mode
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = lengthInKm?.toString() ?: "",
                onValueChange = { input ->
                    lengthInKm = input.toDoubleOrNull()
                },
                label = { Text("Length in km") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = tripDurationInMinutes?.toString() ?: "",
                onValueChange = { input ->
                    tripDurationInMinutes = input.toIntOrNull()
                },
                label = { Text("Trip duration in minutes") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = waypoints.joinToString(", "),
                onValueChange = { input ->
                    waypoints = input.split(", ").map { it.trim() }.toMutableList()
                },
                label = { Text("Waypoints (format: lat,lng)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = images.joinToString(", "),
                onValueChange = { input ->
                    images = input.split(", ").toMutableList()
                },
                label = { Text("Images (comma-separated URLs)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val startLat = tripStartLatitude.toDoubleOrNull()
                    val startLng = tripStartLongitude.toDoubleOrNull()
                    val endLat = tripEndLatitude.toDoubleOrNull()
                    val endLng = tripEndLongitude.toDoubleOrNull()

                    val waypointGeoPoints = waypoints.mapNotNull { waypoint ->
                        val parts = waypoint.split(",")
                        if (parts.size == 2) {
                            val lat = parts[0].toDoubleOrNull()
                            val lng = parts[1].toDoubleOrNull()
                            if (lat != null && lng != null) GeoPoint(lat, lng) else null
                        } else {
                            null
                        }
                    }

                    if (lengthInKm != null && tripDurationInMinutes != null) {
                        saveTrip(
                            context = context,
                            tripName = tripName,
                            tripStartPoint = GeoPoint(startLat ?: 0.0, startLng ?: 0.0),
                            description = description,
                            packingList = packingList,
                            tripEndPoint = GeoPoint(endLat ?: 0.0, endLng ?: 0.0),
                            lengthInKm = lengthInKm ?: 0.0,
                            tripDurationInMinutes = tripDurationInMinutes ?: 0,
                            waypoints = waypointGeoPoints,
                            images = images,
                            ownerID = userId,
                            transportationMode = selectedMode
                        )
                    } else {
                        Toast.makeText(context, "Please enter valid data", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

fun saveTrip(
    context: Context,
    tripName: String,
    tripStartPoint: GeoPoint,
    description: String,
    packingList: List<String>,
    tripEndPoint: GeoPoint,
    lengthInKm: Double,
    tripDurationInMinutes: Int,
    waypoints: List<GeoPoint>,
    images: List<String>,
    ownerID: String,
    transportationMode: TransportationMode
) {
    val db = FirebaseFirestore.getInstance()

   // val sharedPreferences = context.getSharedPreferences("Trips", Context.MODE_PRIVATE)
    //val editor = sharedPreferences.edit()
    //editor.putString("name", tripName)
    //editor.putString("startPoint", tripStartPoint)
   // editor.putString("description", description)
    //editor.putString("packingList", packingList)
    //editor.putString("endPoint", tripEndPoint)
   // editor.putFloat("lengthInKm", lengthInKm.toFloat())
   // editor.putInt("tripDurationInMinutes", tripDurationInMinutes)
    //editor.putString("waypoints", waypoints)
    //editor.putString("transportationMode", transportationMode.name)

   // editor.apply()

   // Toast.makeText(context, "Trip saved locally!", Toast.LENGTH_SHORT).show()


    val tripData = hashMapOf(
        "name" to tripName,
        "startPoint" to tripStartPoint,
        "description" to description,
        "packingList" to packingList,
        "endPoint" to tripEndPoint,
        "lengthInKm" to lengthInKm,
        "tripDurationInMinutes" to tripDurationInMinutes,
        "waypoints" to waypoints,
        "images" to images,
        "ownerID" to ownerID,
        "transportationMode" to transportationMode.name
    )

    db.collection("trips")
        .add(tripData)
        .addOnSuccessListener { documentReference ->
            val generatedId = documentReference.id
            db.collection("trips").document(generatedId)
                .update("id", generatedId)
                .addOnSuccessListener {
                    Toast.makeText(context, "Trip saved to Firebase with ID!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error updating trip ID: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error saving trip: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}
