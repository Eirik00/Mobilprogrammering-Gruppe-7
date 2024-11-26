package com.gruppe7.wanderly

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(isDarkTheme: Boolean, private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) : ViewModel() {
    private val _isDarkTheme = MutableStateFlow(isDarkTheme)
    val isDarkTheme: StateFlow<Boolean> get() = _isDarkTheme

    private val _highContrast = MutableStateFlow(false)
    val highContrast: StateFlow<Boolean> get() = _highContrast

    fun toggleDarkTheme() {
        viewModelScope.launch {
            _isDarkTheme.value = !_isDarkTheme.value
        }
    }

    fun toggleHighContrast() {
        viewModelScope.launch {
            _highContrast.value = !_highContrast.value
        }
    }

    fun deleteAllUserTrips(context: Context, userId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            firestore.collection("trips")
                .whereEqualTo("ownerID", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val batch = firestore.batch()
                    val sharedPreferences =
                        context.getSharedPreferences("savedLocallyTrips", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    // Marker alle dokumenter for sletting
                    querySnapshot.documents.forEach { document ->
                        batch.delete(document.reference)
                        editor.remove("${userId}_${document.id}")
                    }

                    editor.apply()

                    // Utfør batch-sletting
                    batch.commit()
                        .addOnSuccessListener {
                            onComplete(true)
                        }
                        .addOnFailureListener {
                            onComplete(false)
                        }
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }
    }
}