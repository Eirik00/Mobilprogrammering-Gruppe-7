package com.gruppe7.wanderly.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.compose.AppTheme
import com.gruppe7.wanderly.MainLayout
import com.gruppe7.wanderly.TripsViewModel


@Composable
fun LandingPage(navController: NavController, tripsViewModel: TripsViewModel, userId: String) {
    val scrollState = rememberScrollState()
    val currentTrips = tripsViewModel.savedTrips.collectAsState().value[userId]?.filter { it.started } ?: emptyList()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Welcome to Wanderly!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(16.dp)

        )

        Text(
            text = "What will your next trip be? Here are some currently popular trips:",
            fontSize = 16.sp,
            modifier = Modifier
                .padding(16.dp)
        )

        // Current Trips
        Column() {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Current Trips",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    currentTrips.forEach { trip ->
                        TripCard(
                            trip = trip,
                            onClick = { Log.d("STATE", "trip clicked ${trip.name}") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        onClick = { navController.navigate("trips") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                        ),
                        shape = RoundedCornerShape(10)
                    ) {
                        Text(
                            "See More", fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}
