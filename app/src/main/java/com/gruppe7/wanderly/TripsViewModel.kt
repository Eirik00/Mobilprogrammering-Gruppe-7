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
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val description: String = "",
    val lengthInKm: Double = 0.0,
    val tripDurationInMinutes: Int = 0,
    val startPoint: GeoPoint = GeoPoint(0.0, 0.0),
    val endPoint: GeoPoint = GeoPoint(0.0, 0.0),
    val packingList: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val waypoints: List<GeoPoint> = emptyList(),
    val transportationMode: String = "",
    val clickCounter: Int = 0,
    val ownerID: String = "",
    val savedLocally: Boolean = false,
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

    private val _savedTrips = MutableStateFlow<List<TripObject>>(emptyList())
    val savedTrips: StateFlow<List<TripObject>> get() = _savedTrips

    fun addSavedTrip(trip: TripObject) {
        if (!_savedTrips.value.contains(trip)) {
            _savedTrips.value = _savedTrips.value + trip
        }
    }

    //Må legge til funksjon
    fun removeSavedTrip(trip: TripObject) {
        _savedTrips.value = _savedTrips.value - trip
    }

    fun saveTripToFirebase(userId: String, trip: TripObject) {
        val db = FirebaseFirestore.getInstance()

        val savedTrip = mapOf(
            "userId" to userId,
            "tripId" to trip.id,
            "name" to trip.name,
            "description" to trip.description,
            "lengthInKm" to trip.lengthInKm,
            "tripDurationInMinutes" to trip.tripDurationInMinutes,
            "startPoint" to trip.startPoint, // GeoPoint
            "endPoint" to trip.endPoint, // GeoPoint
            "packingList" to trip.packingList, // List<String>
            "images" to trip.images, // List<String>
            "waypoints" to trip.waypoints,
            "transportationMode" to trip.transportationMode,
            "clickCounter" to trip.clickCounter,
            "ownerID" to trip.ownerID,
            "savedLocally" to trip.savedLocally
        )

        db.collection("savedTrips").add(savedTrip)
            .addOnSuccessListener {
                Log.d("Firebase", "Trip saved successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error saving trip", e)
            }
    }

    fun fetchSavedTrips(userId: String, onTripsFetched: (List<TripObject>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("savedTrips")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firebase", "Error fetching saved trips", error)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val trips = snapshots.map { document ->
                        document.toObject(TripObject::class.java)
                    }
                    onTripsFetched(trips)
                }
            }
    }

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
                    } catch (e: Exception) {
                        Log.e("ERROR", "Error converting document ${document.id}", e)
                        null
                    }
                }

                _trips.value = fetchedTrips
                _tripsState.value = TripsFetchState.Success(fetchedTrips)
                Log.d("STATE", "Successfully fetched ${fetchedTrips.size} trips!")
            } catch (e: Exception) {
                _tripsState.value = TripsFetchState.Error(e)
                Log.e("ERROR", "Error fetching trips", e)
            }
        }
    }

    suspend fun fetchTripsByUser(uuid: String): List<TripObject> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val result = db.collection("trips")
                .whereEqualTo("ownerID", uuid)
                .get()
                .await()

            val fetchedTrips = result.mapNotNull { document ->
                try {
                    document.toObject(TripObject::class.java).copy(id = document.id)
                } catch (e: Exception) {
                    Log.e("ERROR", "Error converting document ${document.id}", e)
                    null
                }
            }

            _trips.value = fetchedTrips
            _tripsState.value = TripsFetchState.Success(fetchedTrips)
            Log.d("STATE", "Successfully fetched ${fetchedTrips.size} trips from user $uuid!")

            fetchedTrips
        } catch (e: Exception) {
            _tripsState.value = TripsFetchState.Error(e)
            Log.e("ERROR", "Error fetching trips", e)
            emptyList()
        }
    }

    fun loadTrips() {
        FirebaseFirestore.getInstance().collection("trips")
            .get()
            .addOnSuccessListener { result ->
                val tripsList = result.map { document ->
                    document.toObject(TripObject::class.java).copy(id = document.id)
                }
                _trips.value = tripsList
            }
            .addOnFailureListener { e ->
                Log.e("ERROR", "Error loading trips", e)
            }
    }
}

