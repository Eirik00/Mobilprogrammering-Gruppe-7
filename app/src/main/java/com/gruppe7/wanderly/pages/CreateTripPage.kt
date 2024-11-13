package com.gruppe7.wanderly.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

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
    var minutesToWalkByFoot by remember { mutableStateOf<Int?>(null) }
    var waypoints by remember { mutableStateOf(mutableListOf<String>()) }
    var images by remember { mutableStateOf(mutableListOf<String>()) }

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

            // Start Point Latitude and Longitude
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

            // End Point Latitude and Longitude
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
                value = minutesToWalkByFoot?.toString() ?: "",
                onValueChange = { input ->
                    minutesToWalkByFoot = input.toIntOrNull()
                },
                label = { Text("Minutes to walk by foot") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = waypoints.joinToString(", "),
                onValueChange = { input ->
                    waypoints = input.split(", ").toMutableList()
                },
                label = { Text("Waypoints (comma-separated)") },
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

                    if (lengthInKm != null && minutesToWalkByFoot != null) {
                        saveTrip(
                            context = context,
                            tripName = tripName,
                            tripStartPoint = GeoPoint(startLat ?: 0.0, startLng ?: 0.0),
                            description = description,
                            packingList = packingList,
                            tripEndPoint = GeoPoint(endLat ?: 0.0, endLng ?: 0.0),
                            lengthInKm = lengthInKm ?: 0.0,
                            minutesToWalkByFoot = minutesToWalkByFoot ?: 0,
                            waypoints = waypoints,
                            images = images,
                            ownerID = userId
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
    minutesToWalkByFoot: Int,
    waypoints: List<String>,
    images: List<String>,
    ownerID: String
) {
    val db = FirebaseFirestore.getInstance()

    val sharedPreferences = context.getSharedPreferences("Trips", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("name", tripName)
    //editor.putString("startPoint", tripStartPoint)
    editor.putString("description", description)
    //editor.putString("packingList", packingList)
    //editor.putString("endPoint", tripEndPoint)
    editor.putFloat("lengthInKm", lengthInKm.toFloat())
    editor.putInt("minutesToWalkByFoot", minutesToWalkByFoot)
    //editor.putString("waypoints", waypoints)
    editor.apply()

    Toast.makeText(context, "Trip saved locally!", Toast.LENGTH_SHORT).show()


    val tripData = hashMapOf(
        "name" to tripName,
        "startPoint" to tripStartPoint,
        "description" to description,
        "packingList" to packingList,
        "endPoint" to tripEndPoint,
        "lengthInKm" to lengthInKm,
        "minutesToWalkByFoot" to minutesToWalkByFoot,
        "waypoints" to waypoints,
        "images" to images,
        "ownerID" to ownerID
    )

    db.collection("trips")
        .add(tripData)
        .addOnSuccessListener {
            Toast.makeText(context, "Trip saved to Firebase!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error saving trip: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}
