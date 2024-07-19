package com.example.minmin_v2.ui

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

@Composable
fun SignUpScreen(navController: NavController) {
    var displayName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var displayNameError by remember { mutableStateOf(false) }
    var dateOfBirthError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = Firebase.auth

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
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = displayName,
                onValueChange = { displayName = it; displayNameError = false },
                label = { Text("Display Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (displayNameError) Color.Red.copy(alpha = 0.1f) else Color.Transparent)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (dateOfBirthError) Color.Red.copy(alpha = 0.1f) else Color.Transparent)
            ) {
                Text(text = if (dateOfBirth.isEmpty()) "Select Date of Birth" else dateOfBirth)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = email,
                onValueChange = { email = it; emailError = false },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (emailError) Color.Red.copy(alpha = 0.1f) else Color.Transparent),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = password,
                onValueChange = { password = it; passwordError = false },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (passwordError) Color.Red.copy(alpha = 0.1f) else Color.Transparent),
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
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (confirmPasswordError) Color.Red.copy(alpha = 0.1f) else Color.Transparent),
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
            Button(
                onClick = {
                    val isDisplayNameValid = displayName.isNotBlank()
                    val isDateOfBirthValid = dateOfBirth.isNotBlank()
                    val isEmailValid = email.isNotBlank()
                    val isPasswordValid = password.isNotBlank()
                    val isConfirmPasswordValid = confirmPassword.isNotBlank()
                    val arePasswordsMatching = password == confirmPassword

                    displayNameError = !isDisplayNameValid
                    dateOfBirthError = !isDateOfBirthValid
                    emailError = !isEmailValid
                    passwordError = !isPasswordValid
                    confirmPasswordError = !isConfirmPasswordValid

                    if (!isDisplayNameValid || !isDateOfBirthValid || !isEmailValid || !isPasswordValid || !isConfirmPasswordValid) {
                        Toast.makeText(context, "All fields must be filled out.", Toast.LENGTH_SHORT).show()
                    } else if (!arePasswordsMatching) {
                        Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val emailFormatted = email.replace(".", "_")
                                    val userProfile = hashMapOf(
                                        "displayName" to displayName,
                                        "dateOfBirth" to dateOfBirth,
                                        "email" to email
                                    )
                                    Firebase.firestore.collection("users").document(emailFormatted)
                                        .set(userProfile)
                                        .addOnSuccessListener {
                                            Log.d("SignUp", "User profile created successfully")
                                            Toast.makeText(context, "User added successfully", Toast.LENGTH_SHORT).show()
                                            isLoading = false
                                            navController.navigate("login")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("SignUp", "Error creating user profile", e)
                                            Toast.makeText(context, "Failed to create user profile.", Toast.LENGTH_SHORT).show()
                                            isLoading = false
                                        }
                                } else {
                                    Log.w("SignUp", "createUserWithEmail:failure", task.exception)
                                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account? Login")
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
