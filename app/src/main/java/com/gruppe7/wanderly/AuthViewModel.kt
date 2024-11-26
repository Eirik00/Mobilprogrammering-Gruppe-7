package com.gruppe7.wanderly

import android.app.Application
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
    val uuid: String = ""
)

class AuthViewModel(private val application: Application) : ViewModel() {
    private val _firebaseAuth = MutableStateFlow(FirebaseAuth.getInstance())

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user

    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData> = _userData

    private val _errorMsg = MutableStateFlow("")
    val errorMsg: StateFlow<String> get() = _errorMsg


    init {
        _firebaseAuth.value.addAuthStateListener { auth ->
            _isLoggedIn.value = auth.currentUser != null
            _user.value = auth.currentUser
            if(_isLoggedIn.value){
                _userData.value = loadUserData(application)
            }
        }
    }

    fun login(context: Context,
              email: String,
              password: String,
              onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val task = withContext(Dispatchers.IO) {
                    _firebaseAuth.value.signInWithEmailAndPassword(email, password).await()
                }

                val userData = withContext(Dispatchers.IO) {
                    fetchUserData(task.user?.uid ?: "")
                }

                _userData.value = userData ?: UserData()
                saveUserData(context, _userData.value)

                Log.d("LOGIN", "USERNAME: ${_userData.value.username} Logged inn!")
                onSuccess()
            }catch (e: Exception) {
                Log.e("LOGIN", "Error logging in", e)
                _errorMsg.value = e.message ?: "Login failed"
            }
        }
    }

    fun register(
        context: Context,
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Create user in Firebase Authentication
                val authResult = withContext(Dispatchers.IO) {
                    _firebaseAuth.value.createUserWithEmailAndPassword(email, password).await()
                }

                val user = authResult.user ?: throw Exception("User creation failed")
                val userId = user.uid

                // Create user document in Firestore
                val userData = hashMapOf(
                    "UUID" to userId,
                    "email" to email,
                    "username" to username
                )

                withContext(Dispatchers.IO) {
                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("users").document(userId)
                    userRef.set(userData).await()
                }

                // Update ViewModel state
                _userData.value = UserData(
                    username = username,
                    email = email,
                    uuid = userId
                )
                saveUserData(context, _userData.value)

                Log.d("TAG", "User created successfully")
                onSuccess()

            } catch (exception: Exception) {
                // Handle potential errors during user creation or Firestore document creation
                Log.w("TAG", "Registration failed", exception)

                // If user was created but Firestore document creation failed, delete the user
                try {
                    _firebaseAuth.value.currentUser?.delete()?.await()
                } catch (deleteException: Exception) {
                    Log.w("TAG", "Error deleting user from firebase auth", deleteException)
                }

                _errorMsg.value = exception.message ?: "Registration Failed"
            }
        }
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
        uuid = sharedPreferences.getString("UUID", "") ?: ""
    )
}

private fun saveUserData(context: Context, userData: UserData) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("userData", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("username", userData.username)
        putString("email", userData.email)
        putString("UUID", userData.uuid)
        apply()
    }
}