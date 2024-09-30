package com.example.wanderly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.landingpage.ui.theme.LandingPageTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.MarkerOptions


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LandingPageTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainLayout { innerPadding, selectedIndex ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Text("Current selected index: $selectedIndex")
                            GoogleMapView(savedInstanceState = null)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun GoogleMapView(savedInstanceState: Bundle?) {
    // Remember the MapView so it isn't recreated on recomposition
    val mapView = rememberMapViewWithLifecycle()

    AndroidView({ mapView }) { map ->
        map.onCreate(savedInstanceState)
        map.getMapAsync { googleMap ->
            // Configure the Google Map
            val initialPosition = LatLng(0.0, 0.0)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 10f))
            googleMap.addMarker(MarkerOptions().position(initialPosition).title("Marker"))
        }
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }

    // Make the MapView follow the Compose lifecycle
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val callbacks = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> throw IllegalStateException()
            }
        }

        lifecycle.addObserver(callbacks)
        onDispose {
            lifecycle.removeObserver(callbacks)
        }
    }

    return mapView
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LandingPageTheme {
        MainLayout { innerPadding, selectedIndex ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text("Current selected index: $selectedIndex")
            }
        }
    }
}