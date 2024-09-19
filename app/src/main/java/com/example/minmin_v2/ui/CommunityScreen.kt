// File: CommunityScreen.kt
package com.example.minmin_v2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.minmin_v2.ui.components.TopBar
import com.example.minmin_v2.utils.Post
import com.example.minmin_v2.ui.components.PostItem
import com.example.minmin_v2.utils.likePost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val listenerRegistration = db.collection("Posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle the error
                    return@addSnapshotListener
                }
                posts = snapshot?.toObjects(Post::class.java) ?: emptyList()
            }
        // Clean up listener when no longer needed
        onDispose {
            listenerRegistration.remove()
        }
    }

    Scaffold(
        topBar = {
            TopBar(title = "Community", navController = navController, refreshTrigger = false)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("postCreation") }) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        },
        content = { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier
                    .background(Color(0xFFF5F5F5))
                    .fillMaxSize()
            ) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        navController = navController,
                        onLikeClick = {
                            coroutineScope.launch { likePost(post) }
                        }
                    )
                }
            }
        }
    )
}
