// File: PostDetailScreen.kt
package com.minzy.minmin_v2.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
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
import com.minzy.minmin_v2.R
import com.minzy.minmin_v2.utils.Comment
import com.minzy.minmin_v2.utils.Post
import com.minzy.minmin_v2.utils.getTimeAgo
import com.minzy.minmin_v2.utils.likePost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(navController: NavController, postId: String) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val currentUserId = currentUser?.uid
    val coroutineScope = rememberCoroutineScope()

    var post by remember { mutableStateOf<Post?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var replyToComment by remember { mutableStateOf<Comment?>(null) }
    var isLiked by remember { mutableStateOf(false) }

    var commentText by remember { mutableStateOf("") }
    var editCommentText by remember { mutableStateOf("") }
    var editingComment by remember { mutableStateOf<Comment?>(null) }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Fetch post details
    DisposableEffect(postId) {
        val postRef = db.collection("Posts").document(postId)
        val commentsRef = postRef.collection("Comments")

        val postListener = postRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("PostDetailScreen", "Error fetching post: ${error.message}")
                errorMessage = "Error fetching post: ${error.message}"
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val postData = snapshot.toObject(Post::class.java)
                postData?.isLikedByCurrentUser = postData?.likedBy?.contains(currentUserId) == true
                isLiked = postData?.isLikedByCurrentUser == true
                post = postData
            }
        }

        val commentsListener = commentsRef.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PostDetailScreen", "Error fetching comments: ${error.message}")
                    errorMessage = "Error fetching comments: ${error.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val commentsList = snapshot.toObjects(Comment::class.java)
                    comments = commentsList
                }
            }

        onDispose {
            postListener.remove()
            commentsListener.remove()
        }
    }

    Scaffold(
        topBar = {
            TopBarWithBackButton(navController = navController, title = "Post Details")
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
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                post?.let { postData ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Post Content
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                PostDetailContent(
                                    post = postData,
                                    isLiked = isLiked,
                                    onLikeClick = {
                                        coroutineScope.launch {
                                            likePost(postData)
                                        }
                                    },
                                    onDeleteClick = {
                                        showDeleteConfirmation = true
                                    },
                                    onReportClick = {
                                        // Handle report functionality
                                        navController.navigate("reportPost/${postData.id}")
                                    }
                                )
                            }

                            item {
                                Divider()
                                Text(
                                    text = "Comments",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            // Organize comments and replies
                            val commentsByParent = comments.groupBy { it.parentCommentId }
                            val topLevelComments = commentsByParent[null] ?: emptyList()

                            itemsIndexed(topLevelComments) { index, comment ->
                                CommentItem(
                                    comment = comment,
                                    onReplyClick = { selectedComment ->
                                        replyToComment = selectedComment
                                    },
                                    indentLevel = 0,
                                    commentsByParent = commentsByParent,
                                    onDeleteClick = { selectedComment ->
                                        coroutineScope.launch {
                                            deleteComment(postId, selectedComment)
                                        }
                                    },
                                    onEditClick = { selectedComment ->
                                        editCommentText = selectedComment.text
                                        editingComment = selectedComment
                                    },
                                    onReportClick = { selectedComment ->
                                        // Handle report comment
                                        navController.navigate("reportComment/${selectedComment.id}")
                                    }
                                )
                            }
                        }

                        // Comment Input at the bottom
                        Divider()
                        if (replyToComment != null) {
                            Text(
                                text = "Replying to ${replyToComment?.userName}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else if (editingComment != null) {
                            Text(
                                text = "Editing your comment",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = if (editingComment != null) editCommentText else commentText,
                                onValueChange = {
                                    if (editingComment != null) {
                                        editCommentText = it
                                    } else {
                                        commentText = it
                                    }
                                },
                                placeholder = { Text(if (editingComment != null) "Edit your comment" else "Add a comment") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (editingComment != null) {
                                        // Update comment
                                        val updatedText = editCommentText
                                        coroutineScope.launch {
                                            updateComment(postId, editingComment!!.id, updatedText)
                                            editingComment = null
                                            editCommentText = ""
                                        }
                                    } else if (commentText.isNotEmpty()) {
                                        // Add new comment or reply
                                        coroutineScope.launch {
                                            addCommentToPost(postId, commentText, replyToComment)
                                            commentText = ""
                                            replyToComment = null
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Send, contentDescription = "Send")
                            }
                        }
                    }

                    // Delete Confirmation Dialog
                    if (showDeleteConfirmation) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirmation = false },
                            title = { Text("Delete Post") },
                            text = { Text("Are you sure you want to delete this post? This action cannot be undone.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    coroutineScope.launch {
                                        deletePost(postId, navController)
                                    }
                                    showDeleteConfirmation = false
                                }) {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirmation = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                } ?: run {
                    // Loading Indicator
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    )
}

@Composable
fun PostDetailContent(
    post: Post,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Post Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image and User Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val profileImagePainter = if (post.profileImage != null) {
                    rememberAsyncImagePainter(post.profileImage)
                } else {
                    rememberAsyncImagePainter(R.drawable.ic_profile)
                }
                Image(
                    painter = profileImagePainter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
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

            // Report Button
            IconButton(onClick = onReportClick) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Report Post",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Post Category
        Text(
            text = "Category: ${post.categoryName}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Post Text Content
        Text(
            text = post.textContent,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                    // Display video thumbnail or video player
                    Text(
                        text = "Video content is not yet implemented",
                        modifier = Modifier.padding(8.dp),
                        color = Color.Gray
                    )
                }
                "pdf" -> {
                    // Display PDF icon or open in external app
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
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
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
            Spacer(modifier = Modifier.weight(1f))
            // Delete option for post owner
            val auth = FirebaseAuth.getInstance()
            val currentUserId = auth.currentUser?.uid
            if (post.userId == currentUserId) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Post",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onReplyClick: (Comment) -> Unit,
    indentLevel: Int,
    commentsByParent: Map<String?, List<Comment>>,
    onDeleteClick: (Comment) -> Unit,
    onEditClick: (Comment) -> Unit,
    onReportClick: (Comment) -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (indentLevel * 16).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Profile Image
            val profileImagePainter = if (comment.profileImage != null) {
                rememberAsyncImagePainter(comment.profileImage)
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${comment.userName} • ${getTimeAgo(comment.timestamp)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                // Include the username being replied to
                if (comment.parentCommentUserName != null && indentLevel > 0) {
                    Text(
                        text = "@${comment.parentCommentUserName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                    )
                }
                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Reply",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable {
                                onReplyClick(comment)
                            }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    if (comment.userId == currentUserId) {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable {
                                    onEditClick(comment)
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable {
                                    onDeleteClick(comment)
                                }
                        )
                    } else {
                        Text(
                            text = "Report",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable {
                                    onReportClick(comment)
                                }
                        )
                    }
                }
            }
        }

        // Display replies
        val replies = commentsByParent[comment.id] ?: emptyList()
        replies.forEach { reply ->
            CommentItem(
                comment = reply,
                onReplyClick = onReplyClick,
                indentLevel = indentLevel + 1,
                commentsByParent = commentsByParent,
                onDeleteClick = onDeleteClick,
                onEditClick = onEditClick,
                onReportClick = onReportClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithBackButton(navController: NavController, title: String) {
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
                    Log.e("TopBar", "Error fetching user data: ${error.message}")
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
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            // Display coin balance
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
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

suspend fun addCommentToPost(postId: String, text: String, replyToComment: Comment? = null) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser ?: return
    val userId = currentUser.uid
    val userRef = db.collection("users").document(userId)
    val postRef = db.collection("Posts").document(postId)

    try {
        val userSnapshot = userRef.get().await()
        val userName = userSnapshot.getString("name") ?: "Anonymous"
        val profileImageUri = userSnapshot.getString("profileImageUri")

        val commentId = UUID.randomUUID().toString()
        val parentCommentId = replyToComment?.id
        val parentCommentUserName = replyToComment?.userName

        db.runTransaction { transaction ->
            // Read the post document first
            val postSnapshot = transaction.get(postRef)
            val post = postSnapshot.toObject(Post::class.java)
            val postAuthorId = post?.userId

            // Prepare the comment
            val comment = Comment(
                id = commentId,
                postId = postId,
                userId = userId,
                userName = userName,
                profileImage = profileImageUri,
                text = text,
                timestamp = System.currentTimeMillis(),
                parentCommentId = parentCommentId,
                parentCommentUserName = parentCommentUserName
            )

            val commentsRef = postRef.collection("Comments")

            // Now perform writes
            // Add comment to Comments subcollection
            transaction.set(commentsRef.document(commentId), comment)
            // Increment commentsCount in Post document
            transaction.update(postRef, "commentsCount", FieldValue.increment(1))
            // Award coins to the post author
            if (postAuthorId != null) {
                val postAuthorRef = db.collection("users").document(postAuthorId)
                transaction.update(postAuthorRef, "coinBalance", FieldValue.increment(5))
            }
        }.await()
    } catch (e: Exception) {
        Log.e("addCommentToPost", "Error adding comment: ${e.message}")
    }
}

suspend fun deleteComment(postId: String, comment: Comment) {
    val db = FirebaseFirestore.getInstance()
    val postRef = db.collection("Posts").document(postId)
    val commentRef = postRef.collection("Comments").document(comment.id)
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return

    if (comment.userId != currentUserId) {
        // User is not authorized to delete this comment
        return
    }

    try {
        db.runTransaction { transaction ->
            // Delete the comment
            transaction.delete(commentRef)
            // Decrement commentsCount in Post document
            transaction.update(postRef, "commentsCount", FieldValue.increment(-1))
        }.await()
    } catch (e: Exception) {
        Log.e("deleteComment", "Error deleting comment: ${e.message}")
    }
}

suspend fun updateComment(postId: String, commentId: String, newText: String) {
    val db = FirebaseFirestore.getInstance()
    val postRef = db.collection("Posts").document(postId)
    val commentRef = postRef.collection("Comments").document(commentId)
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return

    try {
        val commentSnapshot = commentRef.get().await()
        val comment = commentSnapshot.toObject(Comment::class.java) ?: return

        if (comment.userId != currentUserId) {
            // User is not authorized to edit this comment
            return
        }

        commentRef.update("text", newText).await()
    } catch (e: Exception) {
        Log.e("updateComment", "Error updating comment: ${e.message}")
    }
}

suspend fun deletePost(postId: String, navController: NavController) {
    // Same as before
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return
    val postRef = db.collection("Posts").document(postId)

    try {
        val postSnapshot = postRef.get().await()
        val post = postSnapshot.toObject(Post::class.java) ?: return
        if (post.userId == currentUserId) {
            // Delete the post and its comments
            val batch = db.batch()
            batch.delete(postRef)
            val commentsRef = postRef.collection("Comments")
            val commentsSnapshot = commentsRef.get().await()
            for (commentDoc in commentsSnapshot.documents) {
                batch.delete(commentDoc.reference)
            }
            batch.commit().await()
            // Navigate back to the previous screen
            navController.popBackStack()
        }
    } catch (e: Exception) {
        Log.e("deletePost", "Error deleting post: ${e.message}")
    }
}
