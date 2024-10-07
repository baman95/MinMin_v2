package com.minzy.minmin_v2.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var displayName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome, ${user?.email}", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Pick Profile Image")
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                user?.let {
                    val profileUpdates = hashMapOf(
                        "displayName" to displayName,
                        "location" to location,
                        "profileImage" to (imageUri?.toString() ?: "")
                    )
                    db.collection("users").document(it.email!!).set(profileUpdates)
                        .addOnSuccessListener {
                            // Handle success
                        }
                        .addOnFailureListener { e ->
                            // Handle error
                        }
                }
            }
        ) {
            Text("Save Profile")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                }
            }
        ) {
            Text("Logout")
        }
    }
}
