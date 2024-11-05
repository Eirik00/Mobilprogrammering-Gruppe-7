package com.gruppe7.wanderly

import com.google.firebase.firestore.GeoPoint

data class TripObject(
    val name: String,
    val type: String,
    val description: String,
    val lengthInKm: Double,
    val minutesToWalkByFoot: Int,
    val startPoint: GeoPoint,
    val endPoint: GeoPoint,
    val packingList: List<String> = emptyList(),
    val images: List<String> = emptyList()
)