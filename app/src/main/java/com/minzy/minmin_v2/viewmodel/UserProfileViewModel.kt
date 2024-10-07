package com.minzy.minmin_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserProfile(
    val email: String = "",
    val name: String = "",
    val age: String = "",
    val gender: String = "",
    val classLevel: String = "",
    val interests: String = "",
    val schoolName: String = "",
    val favoriteGame: String = "",
    val favoriteSubject: String = ""
)

class UserProfileViewModel : ViewModel() {
    private val _user = MutableStateFlow(UserProfile())
    val user: StateFlow<UserProfile> = _user

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    fun fetchUserProfile() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val docRef = db.collection("users").document(user.uid)
            viewModelScope.launch {
                docRef.get().addOnSuccessListener { document ->
                    if (document != null) {
                        _user.value = document.toObject(UserProfile::class.java) ?: UserProfile()
                    }
                }
            }
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val docRef = db.collection("users").document(user.uid)
            viewModelScope.launch {
                docRef.set(profile)
            }
        }
    }
}
