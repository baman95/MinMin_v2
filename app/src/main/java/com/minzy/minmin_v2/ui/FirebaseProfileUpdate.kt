package com.minzy.minmin_v2.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

suspend fun updateProfileInFirebase(
    name: String,
    gender: String,
    location: String,
    schoolName: String,
    interest: String,
    classLevel: String,
    profileImageUri: String?,
    context: Context,
    email: String
) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val userProfileData = mutableMapOf<String, Any>()

    if (name.isNotEmpty()) userProfileData["name"] = name
    if (gender.isNotEmpty()) userProfileData["gender"] = gender
    if (location.isNotEmpty()) userProfileData["location"] = location
    if (schoolName.isNotEmpty()) userProfileData["schoolName"] = schoolName
    if (interest.isNotEmpty()) userProfileData["interest"] = interest
    if (classLevel.isNotEmpty()) userProfileData["classLevel"] = classLevel

    try {
        // Upload profile image to Firebase Storage
        profileImageUri?.let {
            val imageUri = Uri.parse(it)
            val storageRef = storage.reference.child("profileImages/$email")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            userProfileData["profileImageUri"] = downloadUrl.toString()
        }

        // Update user profile data in Firestore
        if (userProfileData.isNotEmpty()) {
            db.collection("users").document(email).set(userProfileData, SetOptions.merge()).await()
        }
        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
