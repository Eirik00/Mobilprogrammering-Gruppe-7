package com.gruppe7.wanderly

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation



@Composable
fun Login(mode: String,authViewModel: AuthViewModel){
    var email by remember{ mutableStateOf("") }
    var password by remember{ mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var shouldNavigateBack by remember { mutableStateOf(false) }

    when(mode){
        "Log in" -> {
            Column {
                Text("Login/Register")
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("email") }
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Row{
                    Button(
                        onClick = { authViewModel.login(email, password)}
                    ) {
                        Text("Login")
                    }
                }
            }
        }
        "Register" ->{
            Column {
                Text("Register")
                TextField(
                    value = username,
                    onValueChange = {username = it},
                    label = { Text("Username") }
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") }
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Row{
                    Button(
                        onClick = { authViewModel.register(username, email, password) }
                    ) {
                        Text("Register")
                    }
                }
            }
        }
        else -> {
            Text("How did you get here?")
        }
    }

}

