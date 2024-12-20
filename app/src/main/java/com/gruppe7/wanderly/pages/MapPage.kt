package com.gruppe7.wanderly.pages

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.MarkerOptions
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.GeoPoint
import com.gruppe7.wanderly.BuildConfig
import com.google.maps.GeoApiContext
import com.google.maps.DirectionsApi
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.google.maps.PendingResult




@Composable
fun MapPage(){
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    )
    {
        GoogleMapView(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun GoogleMapView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Remember the MapView so it isn't recreated on recomposition
    val mapView = rememberMapViewWithLifecycle()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        Log.d("PERM", "Permissions: $perms")
    }

    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    AndroidView(factory = { mapView }, modifier = modifier) { mv ->
        mv.getMapAsync { googleMap ->
            if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@getMapAsync
            }

            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                val location = task.result
                location.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    googleMap.addMarker(MarkerOptions().position(latLng).title("You are here"))
                }
            }
        }
    }
}

@Composable
fun GoogleMapTripView(
    startPoint: GeoPoint,
    endPoint: GeoPoint,
    modifier: Modifier = Modifier,
    wayPoints: List<GeoPoint> = emptyList()
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val startLatLng = LatLng(startPoint.latitude, startPoint.longitude)
    val endLatLng = LatLng(endPoint.latitude, endPoint.longitude)

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { mv ->
            mv.getMapAsync { googleMap ->
                // Clear previous markers/routes
                googleMap.clear()

                // Set up the Directions Service
                val directionsService = GeoApiContext.Builder()
                    .apiKey(BuildConfig.API_KEY)
                    .build()

                // Create DirectionsRequest
                val wayPointLatLngs = wayPoints.map {
                    com.google.maps.model.LatLng(it.latitude, it.longitude)
                }.toTypedArray()

                DirectionsApi.newRequest(directionsService)
                    .origin(com.google.maps.model.LatLng(startLatLng.latitude, startLatLng.longitude))
                    .destination(com.google.maps.model.LatLng(endLatLng.latitude, endLatLng.longitude))
                    .waypoints(*wayPointLatLngs)
                    .mode(TravelMode.WALKING)
                    .setCallback(object : PendingResult.Callback<DirectionsResult> {
                        override fun onResult(result: DirectionsResult) {
                            if (result.routes.isNotEmpty()) {
                                // Draw the route on the map
                                val path = result.routes[0].overviewPolyline.decodePath()

                                // Convert to Google Maps LatLng and draw
                                val mapPoints = path.map {
                                    LatLng(it.lat, it.lng)
                                }

                                // Must run UI updates on main thread
                                (context as? Activity)?.runOnUiThread {
                                    // Add markers
                                    googleMap.addMarker(
                                        MarkerOptions()
                                            .position(startLatLng)
                                            .title("Start")
                                    )

                                    googleMap.addMarker(
                                        MarkerOptions()
                                            .position(endLatLng)
                                            .title("End")
                                    )

                                    // Add waypoint markers
                                    wayPoints.forEachIndexed { index, point ->
                                        googleMap.addMarker(
                                            MarkerOptions()
                                                .position(LatLng(point.latitude, point.longitude))
                                                .title("Stop ${index + 1}")
                                        )
                                    }

                                    // Draw route
                                    googleMap.addPolyline(
                                        PolylineOptions()
                                            .addAll(mapPoints)
                                            .color(Color.BLUE)
                                            .width(5f)
                                    )

                                    // Move camera to show the whole route
                                    val bounds = LatLngBounds.Builder().apply {
                                        include(startLatLng)
                                        include(endLatLng)
                                        wayPoints.forEach {
                                            include(LatLng(it.latitude, it.longitude))
                                        }
                                    }.build()

                                    googleMap.moveCamera(
                                        CameraUpdateFactory.newLatLngBounds(bounds, 100)
                                    )
                                }
                            }
                        }

                        override fun onFailure(e: Throwable) {
                            Log.e("DirectionsAPI", "Error getting directions", e)
                        }
                    })
            }
        }
    )
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }

    // Make the MapView follow the Compose lifecycle
    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when(event){
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> throw IllegalStateException()
            }
        }
    }

