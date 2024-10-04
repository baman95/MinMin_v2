// File: CommunityScreen.kt
package com.example.minmin_v2.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.minmin_v2.R
import com.example.minmin_v2.utils.Post
import com.example.minmin_v2.utils.getTimeAgo
import com.example.minmin_v2.utils.likePost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // State variables for user profile and coins
    var coinBalance by remember { mutableStateOf(0L) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    // Fetch user's coin balance and profile image
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val userRef = db.collection("users").document(currentUserId)
            userRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CommunityScreen", "Error fetching user data: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    coinBalance = snapshot.getLong("coinBalance") ?: 0L
                    profileImageUrl = snapshot.getString("profileImageUri")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val listenerRegistration = db.collection("Posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CommunityScreen", "Error fetching posts: ${error.message}")
                    errorMessage = "Error fetching posts: ${error.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val postList = snapshot.toObjects(Post::class.java)
                    posts = postList.map { post ->
                        post.isLikedByCurrentUser = post.likedBy.contains(currentUserId)
                        post
                    }
                }
            }
        onDispose {
            listenerRegistration.remove()
        }
    }

    Scaffold(
        topBar = {
            TopBar(navController = navController, title = "Community", coinBalance = coinBalance, profileImageUrl = profileImageUrl)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("postCreation") }) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        },
        content = { paddingValues ->
            if (errorMessage.isNotEmpty()) {
                // Display error message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
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
                                coroutineScope.launch {
                                    likePost(post)
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, title: String, coinBalance: Long, profileImageUrl: String?) {
    TopAppBar(
        title = { Text(title) },
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

@Composable
fun PostItem(
    post: Post,
    navController: NavController,
    onLikeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { navController.navigate("postDetail/${post.id}") }
    ) {
        // Post Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            val profileImagePainter = if (post.profileImage != null) {
                rememberAsyncImagePainter(post.profileImage)
            } else {
                rememberAsyncImagePainter(R.drawable.ic_profile)
            }
            Image(
                painter = profileImagePainter,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = post.userName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = getTimeAgo(post.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Post Content
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = post.textContent,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Media Content
        post.mediaUrl?.let { mediaUrl ->
            when (post.mediaType) {
                "image" -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = mediaUrl
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f) // Adjust aspect ratio as needed
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                "video" -> {
                    // Display video thumbnail or placeholder
                    Text(
                        text = "Video content is not yet implemented",
                        modifier = Modifier.padding(8.dp),
                        color = Color.Gray
                    )
                }
                "pdf" -> {
                    // Display PDF icon or placeholder
                    Text(
                        text = "PDF content is not yet implemented",
                        modifier = Modifier.padding(8.dp),
                        color = Color.Gray
                    )
                }
            }
        }

        // Post Actions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (post.isLikedByCurrentUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (post.isLikedByCurrentUser) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${post.likes} Likes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Filled.Comment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${post.commentsCount} Comments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}
