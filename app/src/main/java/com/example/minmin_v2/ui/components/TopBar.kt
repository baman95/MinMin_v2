package com.example.minmin_v2.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.minmin_v2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, navController: NavController,  refreshTrigger: Boolean) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(user, refreshTrigger) {
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(user.email!!.replace(".", "_"))
            try {
                val snapshot = docRef.get().await()
                if (snapshot.exists()) {
                    profileImageUri = snapshot.getString("profileImageUri")
                    Log.d("TopBar", "Profile image URI fetched successfully: $profileImageUri")
                } else {
                    Log.w("TopBar", "No profile image URI found for user: ${user.email}")
                }
            } catch (e: Exception) {
                Log.e("TopBar", "Error fetching profile image URI: ${e.message}", e)
            }
        } else {
            Log.w("TopBar", "User is not authenticated")
        }
    }

    TopAppBar(
        title = { Text(title) },
        actions = {
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
    )
}
