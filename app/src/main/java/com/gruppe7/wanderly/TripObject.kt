package com.gruppe7.wanderly

import com.google.firebase.firestore.GeoPoint

class TripObject {
    val name: String = ""
    val description: String = ""
    val lengthInKm: Double = 0.0
    val minutesToWalkByFoot: Int = 0
    val startPoint: GeoPoint? = null
    val endPoint: GeoPoint? = null
    val whatToBring: List<String> = emptyList()
    val images: List<String> = emptyList()
}