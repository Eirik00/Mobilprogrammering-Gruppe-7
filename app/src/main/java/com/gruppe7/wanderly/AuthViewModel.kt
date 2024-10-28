package com.gruppe7.wanderly

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    fun login(email: String, password: String): Boolean {
        var result = false
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener()
        {
            task -> if (task.isSuccessful) {
                Log.d("TAG", "loginUserWithEmail:Success")
                result=true
                _isLoggedIn.value = true
            }else{
                Log.w("TAG", "loginUserWithEmail:failure", task.exception)
            }
        }
        return result
    }

    fun register(email: String, password: String): Boolean {
        var result = false
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener()
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
                    result=true
                }else{
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
            }
        }
        return result
    }
}