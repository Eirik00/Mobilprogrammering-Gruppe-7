package com.gruppe7.wanderly.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripPage(onBack: () -> Unit) {
    var tripName by remember { mutableStateOf("") }
    var tripType by remember { mutableStateOf("") }
    var tripStartPoint by remember { mutableStateOf("") }
    var tripEndPoint by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var packingList by remember { mutableStateOf("") }

    var lengthInKm by remember { mutableStateOf<Double?>(null) }
    var minutesToWalkByFoot by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create new trip") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TextField(
                value = tripName,
                onValueChange = { tripName = it },
                label = { Text("Trip name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = tripType,
                onValueChange = { tripType = it },
                label = { Text("Trip type") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = tripStartPoint,
                onValueChange = { tripStartPoint = it },
                label = { Text("Start destination") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = tripEndPoint,
                onValueChange = { tripEndPoint = it },
                label = { Text("End destination") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = packingList,
                onValueChange = { packingList = it },
                label = { Text("Packing list") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = lengthInKm?.toString() ?: "",
                onValueChange = { input ->
                    lengthInKm = input.toDoubleOrNull()
                },
                label = { Text("Length in km") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = minutesToWalkByFoot?.toString() ?: "",
                onValueChange = { input ->
                    minutesToWalkByFoot = input.toIntOrNull()
                },
                label = { Text("Minutes to walk by foot") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (lengthInKm != null && minutesToWalkByFoot != null) {
                        saveTrip(
                            context = context,
                            tripName = tripName,
                            tripType = tripType,
                            tripStartPoint = tripStartPoint,
                            description = description,
                            packingList = packingList,
                            tripEndPoint = tripEndPoint,
                            lengthInKm = lengthInKm ?: 0.0,
                            minutesToWalkByFoot = minutesToWalkByFoot ?: 0
                        )
                    } else {
                        Toast.makeText(context, "Please enter valid data", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

fun saveTrip(
    context: Context,
    tripName: String,
    tripType: String,
    tripStartPoint: String,
    description: String,
    packingList: String,
    tripEndPoint: String,
    lengthInKm: Double,
    minutesToWalkByFoot: Int
) {
    val sharedPreferences = context.getSharedPreferences("Trips", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    editor.putString("tripName", tripName)
    editor.putString("tripType", tripType)
    editor.putString("tripStartPoint", tripStartPoint)
    editor.putString("description", description)
    editor.putString("packingList", packingList)
    editor.putString("tripEndPoint", tripEndPoint)
    editor.putFloat("lengthInKm", lengthInKm.toFloat())
    editor.putInt("minutesToWalkByFoot", minutesToWalkByFoot)
    editor.apply()

    Toast.makeText(context, "Trip saved!", Toast.LENGTH_SHORT).show()
}
