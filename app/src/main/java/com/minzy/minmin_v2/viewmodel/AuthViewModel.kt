package com.minzy.minmin_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minzy.minmin_v2.data.FirebaseAuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: FirebaseAuthRepository) : ViewModel() {

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(email, password)
        }
    }

    fun logout() {
        repository.logout()
    }
}
