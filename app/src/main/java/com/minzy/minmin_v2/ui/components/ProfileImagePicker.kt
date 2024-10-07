package com.minzy.minmin_v2.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun ProfileImagePicker(initialUri: String?, onImageSelected: (Uri) -> Unit) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let { onImageSelected(it) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(CircleShape)
            )
        } ?: initialUri?.let {
            Image(
                painter = rememberAsyncImagePainter(Uri.parse(it)),
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            launcher.launch("image/*")
        }) {
            Text("Pick Profile Image")
        }
    }
}
