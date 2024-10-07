package com.minzy.minmin_v2.viewmodel

import android.app.usage.UsageStats
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minzy.minmin_v2.data.AppUsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppUsageViewModel(private val repository: AppUsageRepository) : ViewModel() {
    private val _appUsageStats = MutableStateFlow<List<UsageStats>>(emptyList())
    val appUsageStats: StateFlow<List<UsageStats>> = _appUsageStats

    fun fetchAppUsageStats() {
        viewModelScope.launch {
            _appUsageStats.value = repository.getAppUsageStats()
        }
    }
}

class AppUsageViewModelFactory(private val repository: AppUsageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppUsageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppUsageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
