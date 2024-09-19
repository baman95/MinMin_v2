// File: TopBar.kt
package com.example.minmin_v2.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.minmin_v2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, navController: NavController, refreshTrigger: Boolean) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var coinBalance by remember { mutableStateOf(0L) }

    LaunchedEffect(user, refreshTrigger) {
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("Users").document(user.uid)
            try {
                val snapshot = userRef.get().await()
                if (snapshot.exists()) {
                    profileImageUri = snapshot.getString("profileImageUri")
                    coinBalance = snapshot.getLong("coinBalance") ?: 0L
                    Log.d("TopBar", "Profile image and coin balance fetched successfully")
                } else {
                    Log.w("TopBar", "No user data found for user: ${user.uid}")
                }
            } catch (e: Exception) {
                Log.e("TopBar", "Error fetching user data: ${e.message}", e)
            }
        } else {
            Log.w("TopBar", "User is not authenticated")
        }
    }

    TopAppBar(
        title = { Text(title) },
        actions = {
            // Coin Display
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(

                    painter = painterResource(id = R.drawable.ic_monetization),
                    contentDescription = "Skillcoin",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$coinBalance",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                // Profile Image
                profileImageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                navController.navigate("settings")
                            }
                    )
                } ?: IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    )
}
