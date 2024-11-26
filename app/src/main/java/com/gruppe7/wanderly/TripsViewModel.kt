package com.gruppe7.wanderly

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
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
    var savedLocally: Boolean = false,
    var started: Boolean = false,
)

sealed class TripsFetchState {
    data object Loading : TripsFetchState()
    data class Success(val trips: List<TripObject>) : TripsFetchState()
    data class Error(val exception: Exception) : TripsFetchState()
}

class TripsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _tripsState = MutableStateFlow<TripsFetchState>(TripsFetchState.Loading)
    val tripsState: StateFlow<TripsFetchState> = _tripsState

    private val _trips = MutableStateFlow<List<TripObject>>(emptyList())
    val trips: StateFlow<List<TripObject>> = _trips

    private val _savedTrips = MutableStateFlow<Map<String, List<TripObject>>>(emptyMap())
    val savedTrips: StateFlow<Map<String, List<TripObject>>> = _savedTrips

    fun loadSavedTripsLocally(context: Context, userId: String) {
        val sharedPreferences = context.getSharedPreferences("savedLocallyTrips", Context.MODE_PRIVATE)
        val gson = Gson()

        val userTrips = sharedPreferences.all
            .filter { (key, value) ->
                key.startsWith("${userId}_") && value is String
            }
            .map { (_, value) ->
                gson.fromJson(value as String, TripObject::class.java)
            }

        _savedTrips.value = mapOf(userId to userTrips)
    }

    fun saveTripLocally(context: Context, userId: String, trip: TripObject) {
        trip.savedLocally = true
        val sharedPreferences = context.getSharedPreferences("savedLocallyTrips", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val tripJson = Gson().toJson(trip)
        editor.putString("${userId}_${trip.id}", tripJson)
        editor.apply()
        Log.d("TripsViewModel", "Saved trip(${userId}_${trip.id}) locally: $trip")

        loadSavedTripsLocally(context, userId)
    }

    fun deleteTripLocally(context: Context, userId: String, tripId: String){
        val sharedPreferences = context.getSharedPreferences("savedLocallyTrips", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("${userId}_$tripId")
        editor.apply()
        loadSavedTripsLocally(context, userId)
    }

    fun deleteTripFromFirebase(context: Context, userId: String, tripId: String) {
        deleteTripLocally(context, userId, tripId)

        val tripRef = firestore.collection("trips").document(tripId)
        tripRef.delete()
            .addOnSuccessListener {
                Log.d("TripsViewModel", "Trip deleted successfully from Firebase")
            }
            .addOnFailureListener { e ->
                Log.e("TripsViewModel", "Error deleting trip: $e")
            }
    }

    fun startOrStopTrip(context: Context, trip: TripObject, userId: String): Boolean {
        trip.started = !trip.started
        saveTripLocally(context = context, userId, trip)
        loadSavedTripsLocally(context, userId)

        return trip.started
    }

    init {
        fetchTrips()
    }

    fun fetchTrips() {
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

    suspend fun createTrip(trip: TripObject){
        try {
            val db = FirebaseFirestore.getInstance()
            val result = db.collection("trips")
                .add(trip)
                .await()
            if(result != null){
                Log.d("STATE", "Successfully created trip with id: ${result.id}")
            }
        } catch (e: Exception)  {
            Log.e("ERROR", "Error creating trip", e)
        }
    }
}

