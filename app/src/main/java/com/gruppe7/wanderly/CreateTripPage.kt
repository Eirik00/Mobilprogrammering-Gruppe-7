package com.gruppe7.wanderly

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripPage(onBack: () -> Unit) {
    var tripName by remember { mutableStateOf("") }
    var selectedTripType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var packingList by remember { mutableStateOf("") }
    var tripDestination by remember { mutableStateOf("") }

    val tripTypes = listOf("Hike in nature", "City trip", "Canoe or kayak", "Climbing")
    var expanded by remember { mutableStateOf(false) }

    // Context for showing a toast and accessing SharedPreferences
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create new trip") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedTripType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text("Trip type") }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    tripTypes.forEach { tripType ->
                        DropdownMenuItem(
                            text = { Text(tripType) },
                            onClick = {
                                selectedTripType = tripType
                                expanded = false
                            }
                        )
                    }
                }
            }

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
                value = tripDestination,
                onValueChange = { tripDestination = it },
                label = { Text("Trip destination") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    saveTrip(
                        context = context,
                        tripName = tripName,
                        tripType = selectedTripType,
                        description = description,
                        packingList = packingList,
                        tripDestination = tripDestination
                    )
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
    description: String,
    packingList: String,
    tripDestination: String
) {
    val sharedPreferences = context.getSharedPreferences("Trips", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    editor.putString("tripName", tripName)
    editor.putString("tripType", tripType)
    editor.putString("description", description)
    editor.putString("packingList", packingList)
    editor.putString("tripDestination", tripDestination)
    editor.apply()

    Toast.makeText(context, "Trip saved!", Toast.LENGTH_SHORT).show()
}