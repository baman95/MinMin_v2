package com.example.minmin_v2.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.minmin_v2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "Anonymous",
    val profileImage: String? = null,
    val textContent: String = "",
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun CommunityScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var postText by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        videoUri = uri
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        db.collection("Posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                posts = snapshot.toObjects(Post::class.java)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Light gray background for a modern look
            .padding(16.dp)
    ) {
        Text(
            text = "Share your thoughts",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, color = Color(0xFF333333)),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(auth.currentUser?.photoUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = postText,
                onValueChange = { postText = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RectangleShape)
                    .padding(8.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Icon(painterResource(id = R.drawable.ic_image), contentDescription = "Pick Image")
            }
            IconButton(onClick = { videoPickerLauncher.launch("video/*") }) {
                Icon(painterResource(id = R.drawable.ic_video), contentDescription = "Pick Video")
            }
            Button(onClick = {
                coroutineScope.launch {
                    uploadPost(
                        textContent = postText,
                        imageUri = imageUri,
                        videoUri = videoUri,
                        context = context
                    )
                    postText = ""
                    imageUri = null
                    videoUri = null
                    db.collection("Posts")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            posts = snapshot.toObjects(Post::class.java)
                        }
                }
            }) {
                Text("Publish Post")
            }
        }
        Divider(color = Color.Gray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(posts) { post ->
                PostItem(post)
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(post.profileImage),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = post.userName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Text(text = getTimeAgo(post.timestamp), style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = post.textContent, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp))
        Spacer(modifier = Modifier.height(8.dp))
        post.imageUrl?.let { imageUrl ->
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            )
        }
        post.videoUrl?.let { videoUrl ->
            VideoPlayer(videoUrl = videoUrl)
        }
        //Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun VideoPlayer(videoUrl: String) {
    // Implement a video player composable, e.g., using ExoPlayer
}

fun getTimeAgo(time: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - time

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        seconds < 60 -> "just now"
        minutes < 60 -> "$minutes min${if (minutes == 1L) "" else "s"} ago"
        hours < 24 -> "$hours hour${if (hours == 1L) "" else "s"} ago"
        days < 7 -> "$days day${if (days == 1L) "" else "s"} ago"
        days < 30 -> "${days / 7} week${if (days / 7 == 1L) "" else "s"} ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(time))
    }
}

suspend fun uploadPost(textContent: String, imageUri: Uri?, videoUri: Uri?, context: Context) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser ?: return
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val dateFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val dateTimeString = dateFormatter.format(Date())
    val postId = "${user.email}_$dateTimeString"
    val userId = user.email!!
    val userName = user.displayName ?: "Anonymous"
    val profileImageUrl = user.photoUrl?.toString()

    var imageUrl: String? = null
    var videoUrl: String? = null

    if (imageUri != null) {
        val imageRef = storage.reference.child("posts/$postId/images.jpg")
        imageRef.putFile(imageUri).await()
        imageUrl = imageRef.downloadUrl.await().toString()
    }

    if (videoUri != null) {
        val videoRef = storage.reference.child("posts/$postId/videos.mp4")
        videoRef.putFile(videoUri).await()
        videoUrl = videoRef.downloadUrl.await().toString()
    }

    val post = Post(
        id = postId,
        userId = userId,
        userName = userName,
        profileImage = profileImageUrl,
        textContent = textContent,
        imageUrl = imageUrl,
        videoUrl = videoUrl,
        timestamp = System.currentTimeMillis()
    )

    db.collection("Posts").document(postId).set(post).addOnSuccessListener {
        Toast.makeText(context, "Post uploaded successfully!", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener {
        Toast.makeText(context, "Failed to upload post: ${it.message}", Toast.LENGTH_SHORT).show()
    }
}
