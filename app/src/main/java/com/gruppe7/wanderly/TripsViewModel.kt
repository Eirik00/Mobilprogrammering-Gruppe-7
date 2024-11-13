package com.gruppe7.wanderly

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class TripObject(
    val name: String = "",
    val type: String = "",
    val description: String = "",
    val lengthInKm: Double = 0.0,
    val minutesToWalkByFoot: Int = 0,
    val startPoint: GeoPoint = GeoPoint(0.0, 0.0),
    val endPoint: GeoPoint = GeoPoint(0.0, 0.0),
    val packingList: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val waypoints: List<GeoPoint>? = emptyList(),
    var clickCount: Int = 0,
    var id: String = ""
)

sealed class TripsFetchState {
    data object Loading : TripsFetchState()
    data class Success(val trips: List<TripObject>) : TripsFetchState()
    data class Error(val exception: Exception) : TripsFetchState()
}

class TripsViewModel : ViewModel() {
    private val _tripsState = MutableStateFlow<TripsFetchState>(TripsFetchState.Loading)
    val tripsState: StateFlow<TripsFetchState> = _tripsState

    private val _trips = MutableStateFlow<List<TripObject>>(emptyList())
    val trips: StateFlow<List<TripObject>> = _trips

    init {
        fetchTrips()
    }

    private fun fetchTrips() {
        viewModelScope.launch {
            try {
                _tripsState.value = TripsFetchState.Loading
                val db = FirebaseFirestore.getInstance()
                val result = db.collection("trips")
                    .get()
                    .await()

                val fetchedTrips = result.mapNotNull { document ->
                    try {
                        document.toObject(TripObject::class.java)
                    }catch(e: Exception) {
                        Log.e("ERROR", "Error converting document ${document.id}", e)
                        null
                    }
                }

                _trips.value = fetchedTrips
                _tripsState.value = TripsFetchState.Success(fetchedTrips)
                Log.d("STATE", "Successfully fetched ${fetchedTrips.size} trips!")
            }catch(e: Exception){
                _tripsState.value = TripsFetchState.Error(e)
                Log.e("ERROR", "Error fetching trips", e)
            }
        }
    }

    fun loadTrips() {
        FirebaseFirestore.getInstance().collection("trips")
            .get()
            .addOnSuccessListener { result ->
                val tripsList = result.map { document -> document.toObject(TripObject::class.java) }
                _trips.value = tripsList
            }
    }
}

