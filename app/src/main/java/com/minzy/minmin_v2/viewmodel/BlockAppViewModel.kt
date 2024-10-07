package com.minzy.minmin_v2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BlockAppViewModel : ViewModel() {
    private val _blockedApps = MutableStateFlow<Map<String, Long>>(emptyMap())
    val blockedApps: StateFlow<Map<String, Long>> get() = _blockedApps

    fun toggleAppBlocking(packageName: String, duration: Long) {
        viewModelScope.launch {
            _blockedApps.value = if (_blockedApps.value.containsKey(packageName)) {
                _blockedApps.value - packageName
            } else {
                _blockedApps.value + (packageName to duration)
            }
        }
    }
}
