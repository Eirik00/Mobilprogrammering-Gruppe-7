package com.gruppe7.wanderly.pages


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.compose.AppTheme
import com.gruppe7.wanderly.AuthViewModel
import com.gruppe7.wanderly.MainLayout
import kotlinx.coroutines.launch


@Composable
fun ProfilePage(authViewModel: AuthViewModel) {
    val userInfo = authViewModel.userData.collectAsState().value
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    // Launcher to select image from gallery
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            profileImageUri = uri
        }
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Profile image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    // Open image picker when clicked
                    launcher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, profileImageUri)
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(120.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(120.dp),
                    tint = Color.White
                )
            }
        }


        Spacer(modifier = Modifier.height(16.dp))


        // Name
        Text(
            text = userInfo.username,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )


        Spacer(modifier = Modifier.height(8.dp))


        // Email
        Text(
            text = userInfo.email,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )


        Spacer(modifier = Modifier.height(8.dp))


        // Description
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
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, bottom = 8.dp),
            textAlign = TextAlign.Start
        )


        // Example published trips
        PublishedTripRow("Oslo Sightseeing")
        PublishedTripRow("Sarpsborg Lysløype")
        PublishedTripRow("Sarpsborg Lysløype")
    }
}


@Composable
fun PublishedTripRow(tripName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable {
                Log.d("STATE", "$tripName Clicked!")
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tripName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilePagePreview() {
    AppTheme(highContrast = true) {
        MainLayout { innerPadding, selectedItem, _ ->
            ProfilePage(AuthViewModel())
        }
    }
}
