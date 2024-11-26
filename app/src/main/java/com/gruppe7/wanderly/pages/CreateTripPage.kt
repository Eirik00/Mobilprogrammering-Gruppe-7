package com.gruppe7.wanderly.pages

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.android.identity.util.UUID
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.GeoPoint
import com.gruppe7.wanderly.TripObject
import com.gruppe7.wanderly.TripsViewModel
import kotlinx.coroutines.launch

enum class TransportationMode(val displayName: String) {
    CAR("Car"),
    BIKE("Bike"),
    WALK("Walk")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripPage(tripsViewModel: TripsViewModel, onBack: () -> Unit, userId: String) {
    var tripName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var packingList by remember { mutableStateOf(listOf<String>()) }
    var lengthInKm by remember { mutableStateOf<Double?>(null) }
    var tripDurationInMinutes by remember { mutableStateOf<Int?>(null) }
    var images by remember { mutableStateOf(listOf<String>()) }
    var selectedMode by remember { mutableStateOf(TransportationMode.WALK) }
    var startLatLng by remember { mutableStateOf<LatLng?>(null) }
    var endLatLng by remember { mutableStateOf<LatLng?>(null) }

    var waypoints by remember { mutableStateOf(listOf<LatLng>()) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create new trip") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
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
                    TransportationMode.entries.forEach { mode ->
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
                value = images.joinToString(", "),
                onValueChange = { input ->
                    images = input.split(", ").toMutableList()
                },
                label = { Text("Images (comma-separated URLs)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            GoogleMapView(
                startLatLng = startLatLng,
                endLatLng = endLatLng,
                waypoints = waypoints,
                onMapClick = { latLng ->
                    if (startLatLng == null) {
                        startLatLng = latLng
                    } else if (endLatLng == null) {
                        endLatLng = latLng
                    } else {
                        waypoints = waypoints + latLng
                    }
                },
                onRemoveLastMarker = {
                    if (waypoints.isNotEmpty()) {
                        waypoints = waypoints.dropLast(1)
                    } else if (endLatLng != null) {
                        endLatLng = null
                    } else if (startLatLng != null) {
                        startLatLng = null
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (startLatLng != null && endLatLng != null &&
                        lengthInKm != null && tripDurationInMinutes != null) {
                        coroutineScope.launch {
                            tripsViewModel.createTrip(TripObject(
                                id = UUID.randomUUID().toString(),
                                ownerID = userId,
                                name = tripName,
                                startPoint = GeoPoint(startLatLng!!.latitude, startLatLng!!.longitude),
                                endPoint = GeoPoint(endLatLng!!.latitude, endLatLng!!.longitude),
                                description = description,
                                packingList = packingList,
                                lengthInKm = lengthInKm!!,
                                tripDurationInMinutes = tripDurationInMinutes!!,
                                waypoints = waypoints.map { GeoPoint(it.latitude, it.longitude) },
                                images = images,
                                transportationMode = selectedMode.displayName
                            ))
                            Toast.makeText(context, "Trip saved successfully!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
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

@Composable
fun GoogleMapView(
    startLatLng: LatLng?,
    endLatLng: LatLng?,
    waypoints: List<LatLng>,
    onMapClick: (LatLng) -> Unit,
    onRemoveLastMarker: () -> Unit
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        Log.d("permission", perms.toString())
    }

    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        mapView.getMapAsync { googleMap ->
                            val tapPoint = googleMap.projection.fromScreenLocation(android.graphics.Point(offset.x.toInt(), offset.y.toInt()))
                            onMapClick(LatLng(tapPoint.latitude, tapPoint.longitude))
                        }
                    }
                )
            },
        factory = { mapView }
    ) { mv ->
        mv.getMapAsync { googleMap ->
            // Clear existing markers and redraw
            fun updateMarkers() {
                googleMap.clear()
                startLatLng?.let {
                    googleMap.addMarker(MarkerOptions().position(it).title("Start Point"))
                }
                endLatLng?.let {
                    googleMap.addMarker(MarkerOptions().position(it).title("End Point"))
                }
                waypoints.forEachIndexed { index, waypoint ->
                    googleMap.addMarker(MarkerOptions().position(waypoint).title("Stop ${index + 1}"))
                }

                // Redraw polyline
                val allPoints = listOfNotNull(startLatLng) + waypoints + listOfNotNull(endLatLng)
                if (allPoints.size > 1) {
                    googleMap.addPolyline(
                        PolylineOptions().addAll(allPoints).color(Color.BLUE).width(5f)
                    )
                }
            }

            // Initial marker setup
            updateMarkers()

            // Map click listener
            googleMap.setOnMapClickListener { latLng ->
                onMapClick(latLng)
                updateMarkers()
            }

            // Optional: Long press to remove last marker
            googleMap.setOnMapLongClickListener {
                onRemoveLastMarker()
                updateMarkers()
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.isMyLocationEnabled = true
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        val cameraPosition = CameraPosition.Builder()
                            .target(userLatLng)
                            .zoom(15f)
                            .build()
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    }
                }
            }
        }
    }
}