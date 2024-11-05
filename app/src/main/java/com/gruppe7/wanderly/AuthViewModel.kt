package com.gruppe7.wanderly

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {
    private val _firebaseAuth = MutableStateFlow(FirebaseAuth.getInstance())
    val firebaseAuth: StateFlow<FirebaseAuth> = _firebaseAuth

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user

    init {
        _firebaseAuth.value.addAuthStateListener { user ->
            _isLoggedIn.value = user != null
            _user.value = user.currentUser
        }
    }

    fun login(email: String, password: String) {
        _firebaseAuth.value.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener() { task -> if (task.isSuccessful) {
                Log.d("TAG", "loginUserWithEmail:Success")
            }else{
                Log.w("TAG", "loginUserWithEmail:failure", task.exception)
            }
        }
    }

    fun register(email: String, password: String){
        _firebaseAuth.value.createUserWithEmailAndPassword(email, password).addOnCompleteListener()
        {
            task -> if (task.isSuccessful) {
                    Log.d("TAG", "createUserWithEmail:Success")
                    val user = task.result?.user
                    user?.sendEmailVerification()?.addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            Log.d("TAG", "sendEmailVerification: Success")
                        }else{
                            Log.w("TAG", "sendEmailVerification: failure", task.exception)
                        }
                    }
                }else{
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
            }
        }
    }

    fun signOut() {
        _firebaseAuth.value.signOut()
    }
}