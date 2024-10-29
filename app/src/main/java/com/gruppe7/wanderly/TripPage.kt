package com.gruppe7.wanderly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun TripPage() {
    var showCreateTripPage by remember { mutableStateOf(false) }


    if (showCreateTripPage) {
        CreateTripPage()
    } else {
        Scaffold(
            floatingActionButton = { AddTripButton { showCreateTripPage = true } }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {

                Spacer(modifier = Modifier.height(8.dp))
                SearchSection()
                Spacer(modifier = Modifier.height(16.dp))
                TripSections()
            }
        }
    }
}

@Composable
fun SearchSection() {
    var searchText by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Type location") },
            modifier = Modifier
                .weight(1f) // Makes the TextField take available space
                .padding(end = 8.dp) // Adds spacing between TextField and Button
        )

        Button(
            onClick = { /* Search action */ },
            modifier = Modifier
                .height(56.dp) // Matches the TextField's height
        ) {
            Text("Search")
        }
    }
}

@Composable
fun TripSections() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Most popular trips", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Find more trips", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Saved trips", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3A3A))

        Spacer(modifier = Modifier.height(8.dp))

        SavedTripCard("Midgard vikingsenter - 1 day", "Explore Viking history with a full day at Midgard.")
        SavedTripCard("Vansjø - 3 days", "Enjoy scenic views and outdoor activities over three days.")
    }
}

@Composable
fun SavedTripCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 14.sp)
        }
    }
}

@Composable
fun AddTripButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Text("+", color = Color.White)
    }
}