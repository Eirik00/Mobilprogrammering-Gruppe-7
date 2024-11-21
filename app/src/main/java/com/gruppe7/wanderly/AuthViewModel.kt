package com.gruppe7.wanderly

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class UserData(
    val username: String = "",
    val email: String = "",
    val UUID: String = ""
)

class AuthViewModel(private val context: Context) : ViewModel() {
    private val _firebaseAuth = MutableStateFlow(FirebaseAuth.getInstance())
    val firebaseAuth: StateFlow<FirebaseAuth> = _firebaseAuth

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user

    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData> = _userData


    init {
        _firebaseAuth.value.addAuthStateListener { auth ->
            _isLoggedIn.value = auth.currentUser != null
            _user.value = auth.currentUser
            if(_isLoggedIn.value){
                _userData.value = loadUserData(context)
            }
        }
    }

    fun login(context: Context, email: String, password: String): Boolean {
        var result = false
        _firebaseAuth.value.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener() { task -> if (task.isSuccessful) {
                Log.d("TAG", "loginUserWithEmail:Success ${task.result?.user?.uid}")
                viewModelScope.launch {
                    val userData = withContext(Dispatchers.IO) {
                        fetchUserData(task.result?.user?.uid ?: return@withContext null)
                    }
                    _userData.value = userData ?: UserData()
                    saveUserData(context, _userData.value)
                    Log.d("STATE","USERNAME: ${_userData.value.username}")
                    result = true
                }
            }else{
                Log.w("TAG", "loginUserWithEmail:failure", task.exception)
            }
        }
        return result
    }

    fun register(context: Context, username: String, email: String, password: String): Boolean{
        var result = false
        _firebaseAuth.value.createUserWithEmailAndPassword(email, password).addOnCompleteListener()
        {
            task -> if (task.isSuccessful) {
                    val user = task.result?.user
                    val userId = user?.uid ?: return@addOnCompleteListener

                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("users").document(userId)
                    val userData = hashMapOf(
                        "UUID" to userId,
                        "email" to email,
                        "username" to username
                    )
                    userRef.set(userData)
                        .addOnSuccessListener {
                            Log.d("TAG", "User created successfully")
                            _userData.value = UserData(
                                username = userData["username"] as String,
                                email = userData["email"] as String,
                                UUID = userData["UUID"] as String
                            )
                            saveUserData(context, _userData.value)
                            result = true
                        }
                        .addOnFailureListener { exception ->
                            user.delete()
                                .addOnCompleteListener { deleteTask ->
                                    if(deleteTask.isSuccessful){
                                        Log.d("TAG", "User deleted from firebase auth")
                                    }else{
                                        Log.w("TAG", "Error deleting user from firebase auth", deleteTask.exception)
                                    }
                                }
                            Log.w("TAG", "Error adding user to firestore", exception)
                        }
                }else{
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
            }
        }
        return result
    }

    fun signOut() {
        _firebaseAuth.value.signOut()
        _isLoggedIn.value = false
        _userData.value = UserData()
    }

    private suspend fun fetchUserData(userId: String): UserData? {
        return try {
            val snapshot: QuerySnapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("UUID", userId)
                .limit(1)
                .get()
                .await()

            val document: DocumentSnapshot = snapshot.documents.firstOrNull() ?: return null
            document.toObject(UserData::class.java)
        }catch (e: Exception) {
            Log.e("ERROR", "Error fetching user data for userId: $userId", e)
            null
        }
    }
}

private fun loadUserData(context: Context): UserData{
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("userData", Context.MODE_PRIVATE)
    return UserData(
        username = sharedPreferences.getString("username", "") ?: "",
        email = sharedPreferences.getString("email", "") ?: "",
        UUID = sharedPreferences.getString("UUID", "") ?: ""
    )
}

private fun saveUserData(context: Context, userData: UserData) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("userData", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("username", userData.username)
        putString("email", userData.email)
        putString("UUID", userData.UUID)
        apply()
    }
}