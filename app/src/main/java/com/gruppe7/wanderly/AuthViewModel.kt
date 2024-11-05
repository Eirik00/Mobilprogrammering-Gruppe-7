package com.gruppe7.wanderly

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

data class UserData(
    val username: String = "",
    val email: String = "",
    val uuid: String = ""
)

class AuthViewModel : ViewModel() {
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
        }
    }

    fun login(email: String, password: String): Boolean {
        var result = false
        _firebaseAuth.value.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener() { task -> if (task.isSuccessful) {
                Log.d("TAG", "loginUserWithEmail:Success")
                result = true
            }else{
                Log.w("TAG", "loginUserWithEmail:failure", task.exception)
            }
        }
        return result
    }

    fun register(username: String, email: String, password: String): Boolean{
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
    }
}