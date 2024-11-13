package com.gruppe7.wanderly.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.example.compose.AppTheme
import com.gruppe7.wanderly.MainLayout


@Composable
fun LandingPage(paddingValues: PaddingValues) {

    val scrollState = rememberScrollState()
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
                    CurrentTripRow("Sarpsborg Lysløype", "69%", true)
                    for (i in 1..5) {
                        CurrentTripRow("Sarpsborg Lysløype", "Not Started", false)
                    }
                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        onClick = { Log.d("State", "Popular Trips Clicked!") },
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
        // Popular Trips
        /* Column(){
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Popular Trips",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        textDecoration = TextDecoration.Underline
                    )
                    for(i in 0..5){
                        PopularTripRow("Oslo Sightseeing")
                    }
                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        onClick = { Log.d("State", "Popular Trips Clicked!") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                        ),
                        shape = RoundedCornerShape(10)
                    ){
                        Text("See More", fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }

    }*/
    }

    @Composable
    fun PopularTripRow(tripName: String) {
        var isFavorited by remember { mutableStateOf(false) }

        Row(modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .clickable {
                Log.d("STATE", "$tripName Clicked!") // Reise funksjon
            }
            .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                tripName,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSecondary
            )

            val iconImageVector = if (isFavorited) {
                Icons.Filled.Favorite
            } else {
                Icons.Filled.FavoriteBorder
            }
            Icon(
                imageVector = iconImageVector,
                contentDescription = "Favourite Icon",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { isFavorited = !isFavorited }
            )//modifier = Modifier.align(Alignment.CenterVertically)
        }
    }

    @Composable
    fun CurrentTripRow(tripName: String, tripProgress: String, startedBool: Boolean = false) {
        Row(modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (startedBool) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.secondary
            )
            .clickable {
                Log.d("STATE", "$tripName Clicked!") // Reise funksjon
            }
            .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                tripName, modifier = Modifier.weight(1f), color = if (startedBool)
                    MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSecondary
            )
            Text(
                tripProgress,
                modifier = Modifier.align(Alignment.CenterVertically),
                color = if (startedBool)
                    MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSecondary
            )
        }
    }


    //@Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun LandingPagePreview() {
        AppTheme(highContrast = true) {
            MainLayout { innerPadding, selectedItem,_ ->
                LandingPage(innerPadding)
            }
        }
    }
}

