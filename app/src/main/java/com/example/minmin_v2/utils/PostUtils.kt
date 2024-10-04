// File: PostUtils.kt
package com.example.minmin_v2.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "Anonymous",
    val profileImage: String? = null,
    val categoryId: String = "",
    val categoryName: String = "",
    val category: String = "", // Add this field
    val textContent: String = "",
    val imageUrl: String? = null,
    val mediaUrl: String? = null,
    val mediaType: String? = null, // "image", "video", "pdf"
    var likes: Long = 0,
    var likedBy: MutableList<String> = mutableListOf(),
    val timestamp: Long = System.currentTimeMillis(),
    var commentsCount: Long = 0, // Added this property
    var reportsCount: Long = 0, // New field
    @Transient var isLikedByCurrentUser: Boolean = false
)

data class Category(
    val id: String = "",
    val name: String = "",
    var postCount: Long = 0
)

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "Anonymous",
    val profileImage: String? = null,
    val text: String = "", // Changed from 'commentText' to 'text'
    val timestamp: Long = System.currentTimeMillis(),
    val parentCommentId: String? = null,
    val parentCommentUserName: String? = null  // New field to store the username of the parent comment
)

data class Report(
    val id: String = "",
    val postId: String = "",
    val reportedByUserId: String = "",
    val reportCategory: String = "",
    val additionalDetails: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "just now"
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        days < 7 -> "$days days ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

// File: PostUtils.kt

suspend fun likePost(post: Post) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return
    val postRef = db.collection("Posts").document(post.id)
    val postAuthorRef = db.collection("users").document(post.userId)

    try {
        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val postFromDB = snapshot.toObject(Post::class.java) ?: return@runTransaction
            val likedBy = postFromDB.likedBy.toMutableList()
            var likesCount = postFromDB.likes

            if (likedBy.contains(currentUserId)) {
                // Unlike
                likedBy.remove(currentUserId)
                likesCount -= 1
                transaction.update(postRef, mapOf("likedBy" to likedBy, "likes" to likesCount))
                // Deduct coin from the post author
                transaction.update(postAuthorRef, "coinBalance", FieldValue.increment(-1))
            } else {
                // Like
                likedBy.add(currentUserId)
                likesCount += 1
                transaction.update(postRef, mapOf("likedBy" to likedBy, "likes" to likesCount))
                // Award 1 coin to the post author
                transaction.update(postAuthorRef, "coinBalance", FieldValue.increment(1))
            }
        }.await()
    } catch (e: Exception) {
        Log.e("likePost", "Error updating likes: ${e.message}")
    }
}


suspend fun addCommentToPost(postId: String, text: String, parentCommentId: String? = null) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser ?: return
    val userId = currentUser.uid
    val userRef = db.collection("users").document(userId)

    try {
        val userSnapshot = userRef.get().await()
        val userName = userSnapshot.getString("name") ?: "Anonymous"
        val profileImageUri = userSnapshot.getString("profileImageUri")

        var parentCommentUserName: String? = null
        if (parentCommentId != null) {
            val parentCommentRef = db.collection("Posts").document(postId)
                .collection("Comments").document(parentCommentId)
            val parentCommentSnapshot = parentCommentRef.get().await()
            if (parentCommentSnapshot.exists()) {
                parentCommentUserName = parentCommentSnapshot.getString("userName")
            }
        }

        val commentId = UUID.randomUUID().toString()
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

        val postRef = db.collection("Posts").document(postId)
        val commentsRef = postRef.collection("Comments")

        // Use a transaction to ensure data consistency
        db.runTransaction { transaction ->
            // Add comment to Comments subcollection
            transaction.set(commentsRef.document(commentId), comment)
            // Increment commentsCount in Post document
            transaction.update(postRef, "commentsCount", FieldValue.increment(1))
        }.await()
    } catch (e: Exception) {
        Log.e("addCommentToPost", "Error adding comment: ${e.message}")
    }
}
