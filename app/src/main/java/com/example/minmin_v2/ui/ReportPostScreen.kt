// File: ReportPostScreen.kt
package com.example.minmin_v2.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.minmin_v2.ui.components.BottomNavigationBar
import com.example.minmin_v2.utils.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportPostScreen(navController: NavController, postId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val currentUserId = currentUser?.uid

    var selectedCategory by remember { mutableStateOf("") }
    var additionalDetails by remember { mutableStateOf("") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    // Common reporting options
    val commonCategories = listOf(
        "Spam",
        "Harassment",
        "Inappropriate Content",
        "False Information",
        "Other"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select a reason for reporting:",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown for report categories
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text("Select Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        commonCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryDropdownExpanded = false
                                    if (category == "Other") {
                                        showAddCategoryDialog = true
                                    }
                                }
                            )
                        }
                    }
                }

                // Add Category Dialog
                if (showAddCategoryDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddCategoryDialog = false },
                        title = { Text("Add New Category") },
                        text = {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text("Category Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    selectedCategory = newCategoryName
                                    newCategoryName = ""
                                    showAddCategoryDialog = false
                                }
                            ) {
                                Text("Add")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddCategoryDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Additional details
                OutlinedTextField(
                    value = additionalDetails,
                    onValueChange = { additionalDetails = it },
                    label = { Text("Additional Details (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit button
                Button(
                    onClick = {
                        if (selectedCategory.isNotEmpty()) {
                            coroutineScope.launch {
                                submitReport(
                                    postId = postId,
                                    reportCategory = selectedCategory,
                                    additionalDetails = additionalDetails,
                                    onSuccess = {
                                        Toast.makeText(context, "Report submitted successfully", Toast.LENGTH_LONG).show()
                                        navController.popBackStack()
                                    }
                                )
                            }
                        } else {
                            Toast.makeText(context, "Please select a category", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Report")
                }
            }
        }
    )
}


suspend fun submitReport(
    postId: String,
    reportCategory: String,
    additionalDetails: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser ?: return
    val userId = currentUser.uid

    val reportId = UUID.randomUUID().toString()
    val report = Report(
        id = reportId,
        postId = postId,
        reportedByUserId = userId,
        reportCategory = reportCategory,
        additionalDetails = additionalDetails
    )

    val postRef = db.collection("Posts").document(postId)
    val reportsRef = db.collection("reports")

    try {
        db.runTransaction { transaction ->
            // Add report to 'reports' collection
            transaction.set(reportsRef.document(reportId), report)
            // Increment 'reportsCount' in Post document
            transaction.update(postRef, "reportsCount", FieldValue.increment(1))
        }.await()

        onSuccess()
    } catch (e: Exception) {
        Log.e("submitReport", "Error submitting report: ${e.message}")
    }
}
