package com.example.wanderly

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.AppTheme




@Composable
fun LandingPage(paddingValues: PaddingValues){
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {

            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                onClick = { Log.d("STATE","Create New Trip Clicked!") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                shape = RoundedCornerShape(10)
            ){
                Text("Create New Trip", fontSize = 20.sp)
            }
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                onClick = { Log.d("State", "Popular Trips Clicked!") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                shape = RoundedCornerShape(10)
            ){
                Text("Popular Trips", fontSize = 20.sp)
            }
        }
        // Current Trips
        Column(){
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.secondary)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Current Trips",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        textDecoration = TextDecoration.Underline
                    )
                    CurrentTripRow("Sarpsborg Lysløype", "Not Started", true)
                    for( i in 1..5) {
                        CurrentTripRow("Sarpsborg Lysløype", "Not Started", false)
                    }
                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        onClick = { Log.d("State", "Popular Trips Clicked!") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                        shape = RoundedCornerShape(10)
                    ){
                        Text("See More", fontSize = 20.sp)
                    }
                }
            }
        }
        // Popular Trips
        Column(){
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.secondary)
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
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                        shape = RoundedCornerShape(10)
                    ){
                        Text("See More", fontSize = 20.sp)
                    }
                }
            }
        }

    }
}

@Composable
fun PopularTripRow(tripName: String) {
    Row(modifier = Modifier
        .padding(5.dp)
        .fillMaxWidth()
        .background(Color(227, 195, 79, 255))
        .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(tripName, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.FavoriteBorder,
            contentDescription = "Favourite Icon",
            tint = Color.Black, // Må endres til theme farger
        )//modifier = Modifier.align(Alignment.CenterVertically)
    }
}

@Composable
fun CurrentTripRow(tripName: String, tripProgress: String, startedBool: Boolean = false) {
    Row(modifier = Modifier
        .padding(5.dp)
        .fillMaxWidth()
        .background(if (startedBool) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.tertiaryContainer)
        .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(tripName, modifier = Modifier.weight(1f))
        Text(tripProgress, modifier = Modifier.align(Alignment.CenterVertically))
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LandingPagePreview() {
    AppTheme(highContrast = true) {
        MainLayout { innerPadding, selectedItem ->
            LandingPage(innerPadding)
        }
    }
}
