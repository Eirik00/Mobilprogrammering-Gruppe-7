package com.gruppe7.wanderly.pages

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gruppe7.wanderly.AuthViewModel
import com.gruppe7.wanderly.TripObject
import com.gruppe7.wanderly.TripsViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfilePage(authViewModel: AuthViewModel, tripsViewModel: TripsViewModel) {
    val userInfo = authViewModel.userData.collectAsState().value
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var userPubTrips by remember { mutableStateOf(listOf<TripObject>()) }

    LaunchedEffect(userInfo.UUID) {
        coroutineScope.launch {
            userPubTrips = tripsViewModel.fetchTripsByUser(userInfo.UUID)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> profileImageUri = uri }
    )

    var selectedTrip by remember { mutableStateOf<TripObject?>(null) }

    val profileBitmap = remember(profileImageUri) {
        profileImageUri?.let { uri ->
            try {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)?.asImageBitmap()
            } catch (e: Exception) {
                Log.e("ProfilePage", "Failed to load image: ${e.message}")
                null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (profileBitmap != null) {
                Image(
                    painter = BitmapPainter(profileBitmap),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(120.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier.size(120.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Information
        Text(
            text = userInfo.username,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = userInfo.email,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Utforsker. Naturelsker. Alltid klar for et eventyr!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Published Trips Section
        Text(
            text = "Published Trips",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, bottom = 8.dp),
            textAlign = TextAlign.Start
        )

        userPubTrips.forEach { trip ->
            PublishedTripRow(
                trip = trip,
                onTripClick = { selectedTrip = trip }
            )
        }
    }

    // Trip Dialog
    selectedTrip?.let { trip ->
        TripDialog(
            trip = trip,
            onDismiss = { selectedTrip = null },
            onSaveOrDelete = {
                coroutineScope.launch {
                    if (trip.savedLocally) {
                        tripsViewModel.deleteTripLocally(context, userInfo.UUID, trip.id)
                    } else {
                        tripsViewModel.saveTripLocally(context, userInfo.UUID, trip)
                    }
                    selectedTrip = null
                    userPubTrips = tripsViewModel.fetchTripsByUser(userInfo.UUID)
                }
            },
            onDeleteFromFirebase = {
                coroutineScope.launch {
                    if (trip.ownerID == userInfo.UUID) {
                        tripsViewModel.deleteTripFromFirebase(context, userInfo.UUID, trip.id)
                        Toast.makeText(context, "Trip deleted from Firebase", Toast.LENGTH_SHORT).show()
                        userPubTrips = tripsViewModel.fetchTripsByUser(userInfo.UUID)
                    } else {
                        Toast.makeText(context, "You are not the owner of this trip", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            showDeleteButton = true
        )
    }
}

@Composable
fun PublishedTripRow(trip: TripObject, onTripClick: (TripObject) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onTripClick(trip) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = trip.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
