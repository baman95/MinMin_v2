package com.minzy.minmin_v2.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.minzy.minmin_v2.ui.components.ProfileImagePicker
import com.minzy.minmin_v2.ui.updateProfileInFirebase
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun SettingsScreen(navController: NavController, onProfileUpdated: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    var interest by remember { mutableStateOf("") }
    var classLevel by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(user.email!!.replace(".", "_"))
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                name = snapshot.getString("name") ?: ""
                gender = snapshot.getString("gender") ?: ""
                location = snapshot.getString("location") ?: ""
                schoolName = snapshot.getString("schoolName") ?: ""
                interest = snapshot.getString("interest") ?: ""
                classLevel = snapshot.getString("classLevel") ?: ""
                profileImageUri = snapshot.getString("profileImageUri")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isEditing) {
            EditProfileForm(
                name = name,
                gender = gender,
                location = location,
                schoolName = schoolName,
                interest = interest,
                classLevel = classLevel,
                profileImageUri = profileImageUri,
                onNameChange = { name = it },
                onGenderChange = { gender = it },
                onLocationChange = { location = it },
                onSchoolNameChange = { schoolName = it },
                onInterestChange = { interest = it },
                onClassLevelChange = { classLevel = it },
                onProfileImageUriChange = { profileImageUri = it },
                onSave = {
                    scope.launch {
                        try {
                            updateProfileInFirebase(
                                name,
                                gender,
                                location,
                                schoolName,
                                interest,
                                classLevel,
                                profileImageUri,
                                context,
                                user!!.email!!
                            )
                            isEditing = false
                            onProfileUpdated()
                        } catch (e: Exception) {
                            errorMessage = "Failed to update profile: ${e.message}"
                        }
                    }
                },
                onCancel = { isEditing = false }
            )
        } else {
            ProfileSummary(
                name = name,
                profileImageUri = profileImageUri,
                onEdit = { isEditing = true },
                onLogout = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("settings") { inclusive = true }
                    }
                }
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EditProfileForm(
    name: String,
    gender: String,
    location: String,
    schoolName: String,
    interest: String,
    classLevel: String,
    profileImageUri: String?,
    onNameChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onSchoolNameChange: (String) -> Unit,
    onInterestChange: (String) -> Unit,
    onClassLevelChange: (String) -> Unit,
    onProfileImageUriChange: (String?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Make the form scrollable
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Edit Profile", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        ProfileImagePicker(initialUri = profileImageUri) { uri ->
            onProfileImageUriChange(uri.toString())
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        DropdownMenuField("Gender", listOf("Male", "Female", "Other"), gender, onGenderChange)
        Spacer(modifier = Modifier.height(16.dp))
        LocationPickerField(location, onLocationChange)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = schoolName,
            onValueChange = onSchoolNameChange,
            label = { Text("School Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = interest,
            onValueChange = onInterestChange,
            label = { Text("Interest") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        DropdownMenuField("Class Level", listOf("Class 1", "Class 2", "Class 3", "Class 4", "Class 5"), classLevel, onClassLevelChange)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onSave) {
                Text("Save")
            }
            Button(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun ProfileSummary(
    name: String,
    profileImageUri: String?,
    onEdit: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hello, $name", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        profileImageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onEdit) {
            Text("Edit Profile")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuField(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .clickable { expanded = true }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LocationPickerField(location: String, onLocationSelected: (String) -> Unit) {
    val context = LocalContext.current
    var locationState by remember { mutableStateOf(location) }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
        // Request location permissions
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            1
        )
    }

    OutlinedTextField(
        value = locationState,
        onValueChange = {},
        label = { Text("Location") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED) {
                    // Request location permissions
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        1
                    )
                    return@IconButton
                }
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    loc?.let {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        if (addresses?.isNotEmpty() == true) {
                            locationState = addresses?.get(0)?.getAddressLine(0) ?: "Location not found"
                            onLocationSelected(locationState)
                        } else {
                            Toast.makeText(context, "Location not found. Please enable location services.", Toast.LENGTH_SHORT).show()
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                    } ?: run {
                        Toast.makeText(context, "Failed to fetch location. Please enable location services.", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
            }) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Pick Location")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
