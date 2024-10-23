package com.gruppe7.wanderly

import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview

import com.example.compose.AppTheme



@Composable
fun Login(authViewModel: AuthViewModel){
    var email by remember{ mutableStateOf("") }
    var password by remember{ mutableStateOf("") }

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
                onClick = { authViewModel.register(email, password) }
            ) {
                Text("Register")
            }
            Button(
                onClick = { authViewModel.login(email, password) }
            ) {
                Text("Login")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    AppTheme {
        MainLayout { innerPadding, selectedItem ->
            Login(AuthViewModel())
        }
    }
}

