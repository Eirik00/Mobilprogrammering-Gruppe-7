package com.gruppe7.wanderly

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController

@Composable
fun Login(mode: String, authViewModel: AuthViewModel, navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val usernameMsg = authViewModel.userData.collectAsState().value.username
    val errorMsg = authViewModel.errorMsg.collectAsState().value
    var result by remember { mutableStateOf(false) }

    when (mode) {
        "Log in" -> {
            Column {
                Text("Login/Register")
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = androidx.compose.ui.graphics.Color.Red
                    )
                }
                Row {
                    Button(
                        onClick = {
                            authViewModel.login(context, email, password) {
                                Toast.makeText(context, "Welcome back $usernameMsg!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home")
                            }
                        }
                    ) {
                        Text("Login")
                    }
                }
            }
        }
        "Register" -> {
            Column {
                Text("Register")
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") }
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") }
                )
                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = androidx.compose.ui.graphics.Color.Red
                    )
                }
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Row {
                    Button(
                        onClick = {
                            authViewModel.register(context, username, email, password){
                                Toast.makeText(context, "Welcome $username!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home")
                            }
                        }
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