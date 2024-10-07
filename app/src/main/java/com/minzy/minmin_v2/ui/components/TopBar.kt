// File: TopBar.kt
package com.minzy.minmin_v2.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, title: String, showBackButton: Boolean = false) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid
    var coinBalance by remember { mutableStateOf(0L) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    // Fetch user's coin balance and profile image
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val userRef = db.collection("users").document(currentUserId)
            userRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    coinBalance = snapshot.getLong("coinBalance") ?: 0L
                    profileImageUrl = snapshot.getString("profileImageUri")
                }
            }
        }
    }

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showBackButton) {
                run {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            } else null
        },
        actions = {
            // Display coin balance
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = "$coinBalance")
                Spacer(modifier = Modifier.width(16.dp))
                // Display profile image or account icon
                IconButton(onClick = { /* Navigate to profile screen */ }) {
                    if (profileImageUrl != null) {
                        // Display profile image
                        val painter = rememberAsyncImagePainter(profileImageUrl)
                        Image(
                            painter = painter,
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        // Display default icon
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    )
}
