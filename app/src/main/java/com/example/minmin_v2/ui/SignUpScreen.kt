// File: SignUpScreen.kt
package com.example.minmin_v2.ui

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.minmin_v2.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var dateOfBirthError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val auth = Firebase.auth
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    // Date Picker
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            dateOfBirth = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        }, year, month, day
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Name") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (dateOfBirthError) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(text = if (dateOfBirth.isEmpty()) "Select Date of Birth" else dateOfBirth)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it; emailError = false },
                    label = { Text("Email") },
                    isError = emailError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it; passwordError = false },
                    label = { Text("Password") },
                    isError = passwordError,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            painterResource(id = R.drawable.ic_visibility)
                        else
                            painterResource(id = R.drawable.ic_visibilityoff)
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(painter = image, contentDescription = null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmPasswordError = false },
                    label = { Text("Confirm Password") },
                    isError = confirmPasswordError,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible)
                            painterResource(id = R.drawable.ic_visibility)
                        else
                            painterResource(id = R.drawable.ic_visibilityoff)
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(painter = image, contentDescription = null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val isNameValid = name.isNotBlank()
                        val isDateOfBirthValid = dateOfBirth.isNotBlank()
                        val isEmailValid = email.isNotBlank()
                        val isPasswordValid = password.isNotBlank()
                        val isConfirmPasswordValid = confirmPassword.isNotBlank()
                        val arePasswordsMatching = password == confirmPassword

                        nameError = !isNameValid
                        dateOfBirthError = !isDateOfBirthValid
                        emailError = !isEmailValid
                        passwordError = !isPasswordValid
                        confirmPasswordError = !isConfirmPasswordValid

                        if (!isNameValid || !isDateOfBirthValid || !isEmailValid || !isPasswordValid || !isConfirmPasswordValid) {
                            errorMessage = "All fields must be filled out."
                        } else if (!arePasswordsMatching) {
                            errorMessage = "Passwords do not match."
                            passwordError = true
                            confirmPasswordError = true
                        } else {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                                    val user = result.user
                                    if (user != null) {
                                        createUserProfile(user.uid, name, dateOfBirth, email,
                                            onSuccess = {
                                                scope.launch {
                                                    try {
                                                        user.sendEmailVerification().await()
                                                        Toast.makeText(context, "Verification email sent", Toast.LENGTH_SHORT).show()
                                                    } catch (e: Exception) {
                                                        Log.e("SignUp", "Failed to send verification email", e)
                                                        Toast.makeText(context, "Failed to send verification email", Toast.LENGTH_SHORT).show()
                                                    }
                                                    isLoading = false
                                                    navController.navigate("login") {
                                                        popUpTo("signup") { inclusive = true }
                                                    }
                                                }
                                            },
                                            onError = { error ->
                                                isLoading = false
                                                errorMessage = error
                                            }
                                        )
                                    } else {
                                        isLoading = false
                                        errorMessage = "Authentication failed."
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    Log.w("SignUp", "createUserWithEmail:failure", e)
                                    errorMessage = e.message ?: "Authentication failed."
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Up")
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Already have an account? Login")
                }
            }
        }
    }
}

suspend fun createUserProfile(
    userId: String,
    name: String,
    dateOfBirth: String,
    email: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("Users").document(userId)
    val userProfile = hashMapOf(
        "name" to name,
        "dateOfBirth" to dateOfBirth,
        "email" to email,
        "coinBalance" to 1000L,
        "profileImageUri" to null
    )
    try {
        userRef.set(userProfile).await()
        Log.d("SignUp", "User profile created successfully")

        // Initialize totalCoins if it doesn't exist
        initializeGlobalStats()
        onSuccess()
    } catch (e: Exception) {
        Log.e("SignUp", "Error creating user profile", e)
        onError(e.message ?: "Failed to create user profile.")
    }
}

suspend fun initializeGlobalStats() {
    val db = FirebaseFirestore.getInstance()
    val globalStatsRef = db.collection("GlobalStats").document("stats")
    try {
        val snapshot = globalStatsRef.get().await()
        if (!snapshot.exists()) {
            val stats = mapOf("totalCoins" to 1000L)
            globalStatsRef.set(stats).await()
            Log.d("SignUp", "Global stats initialized")
        } else {
            // Increment totalCoins by 1000
            globalStatsRef.update("totalCoins", FieldValue.increment(1000L)).await()
            Log.d("SignUp", "Total coins updated")
        }
    } catch (e: Exception) {
        Log.e("SignUp", "Error initializing global stats", e)
    }
}
