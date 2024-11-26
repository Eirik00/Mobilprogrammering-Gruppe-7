@file:Suppress("DEPRECATION")

package com.gruppe7.wanderly.pages

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gruppe7.wanderly.AuthViewModel
import com.gruppe7.wanderly.TripObject
import com.gruppe7.wanderly.TripsViewModel
import kotlinx.coroutines.launch


@Composable
fun ProfilePage(authViewModel: AuthViewModel, tripsViewModel: TripsViewModel, navController: NavController) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userInfo = authViewModel.userData.collectAsState().value
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var description by remember {
        mutableStateOf(loadProfileDescription(context, userInfo.uuid))
    }
    var isEditingDescription by remember { mutableStateOf(false) }

    var userPubTrips by remember { mutableStateOf(listOf<TripObject>()) }

    LaunchedEffect(userInfo.uuid) {
        if (isLoggedIn) {
            Log.d("STATE", "user id: ${userInfo.uuid}")
            userPubTrips = tripsViewModel.fetchTripsByUser(userInfo.uuid)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                Log.e("ProfilePage", "Failed to load image: ${e.message}")
                null
            }
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isLoggedIn) {
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
                        painter = BitmapPainter(profileBitmap.asImageBitmap()),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isEditingDescription) {
                    BasicTextField(
                        value = description,
                        onValueChange = { description = it },
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    )
                    IconButton(
                        onClick = {
                            isEditingDescription = false
                            saveProfileDescription(context, userInfo.uuid, description)
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Save Description")
                    }
                } else {
                    Text(
                        text = description.ifEmpty { "Utforsker. Naturelsker. Alltid klar for et eventyr!" },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isEditingDescription = true }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Description")
                    }
                }
            }

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
        } else {
            // Content for guest users
            Text(
                text = "Please log in to access your profile.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { navController.navigate("home/login/Log in") },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Login")
            }
        }

        // Trip Dialog
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
                        tripsViewModel.deleteTripLocally(context, userInfo.uuid, trip.id)
                    } else {
                        tripsViewModel.saveTripLocally(context, userInfo.uuid, trip)
                    }
                    selectedTrip = null
                    userPubTrips = tripsViewModel.fetchTripsByUser(userInfo.uuid)
                }
            },
            onDeleteFromFirebase = {
                coroutineScope.launch {
                    if (trip.ownerID == userInfo.uuid) {
                        tripsViewModel.deleteTripFromFirebase(context, userInfo.uuid, trip.id)
                        Toast.makeText(context, "Trip deleted from Firebase", Toast.LENGTH_SHORT).show()
                        userPubTrips = tripsViewModel.fetchTripsByUser(userInfo.uuid)
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

fun saveProfileDescription(context: Context, userId: String, description: String) {
    val sharedPreferences = context.getSharedPreferences("ProfileDescriptions", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(userId, description).apply()
}

fun loadProfileDescription(context: Context, userId: String): String {
    val sharedPreferences = context.getSharedPreferences("ProfileDescriptions", Context.MODE_PRIVATE)
    return sharedPreferences.getString(userId, "") ?: ""
}