package com.gruppe7.wanderly

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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

                    querySnapshot.documents.forEach { document ->
                        batch.delete(document.reference)
                        editor.remove("${userId}_${document.id}")
                    }

                    editor.apply()

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

    fun deleteUserProfile(context: Context, onSuccess: (Boolean) -> Unit) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            user?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(true)
                } else {
                    onSuccess(false)
                }
            }
        } catch (e: Exception) {
            onSuccess(false)
            Toast.makeText(context, "Failed to delete profile: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
