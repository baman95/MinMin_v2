package com.example.minmin_v2.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.minmin_v2.ui.components.ProfileImagePicker
import com.example.minmin_v2.ui.components.DarkModeButton
import com.example.minmin_v2.viewmodel.UserProfileViewModel

@Composable
fun UserProfileScreen(navController: NavController) {
    val viewModel: UserProfileViewModel = viewModel()
    val user = viewModel.user
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("User Profile")
        ProfileImagePicker()
        Spacer(modifier = Modifier.height(16.dp))
        DarkModeButton()
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("home") }) {
            Text("Save Profile")
        }
    }
}
