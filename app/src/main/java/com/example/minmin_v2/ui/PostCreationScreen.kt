// File: PostCreationScreen.kt
package com.example.minmin_v2.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.minmin_v2.utils.Category
import com.example.minmin_v2.utils.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreationScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State variables
    var postText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var mediaUri by remember { mutableStateOf<Uri?>(null) }
    var mediaType by remember { mutableStateOf<String?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    // Fetch categories from Firestore
    LaunchedEffect(Unit) {
        db.collection("categories").get().addOnSuccessListener { snapshot ->
            categories = snapshot.toObjects(Category::class.java)
        }
    }

    // Media picker launchers
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        mediaUri = uri
        mediaType = "image"
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        mediaUri = uri
        mediaType = "video"
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        mediaUri = uri
        mediaType = "pdf"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create a Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            if (showPreview) {
                // Preview Screen
                PreviewScreen(
                    postText = postText,
                    selectedCategory = selectedCategory,
                    mediaUri = mediaUri,
                    mediaType = mediaType,
                    onPublish = {
                        coroutineScope.launch {
                            publishPost(
                                textContent = postText,
                                category = selectedCategory,
                                mediaUri = mediaUri,
                                mediaType = mediaType,
                                context = context,
                                onSuccess = {
                                    Toast.makeText(context, "Post published successfully", Toast.LENGTH_LONG).show()
                                    navController.navigate("community") {
                                        popUpTo("postCreation") { inclusive = true }
                                    }
                                }
                            )
                        }
                    },
                    onEdit = {
                        showPreview = false
                    }
                )
            } else {
                // Post Creation Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Post content
                    OutlinedTextField(
                        value = postText,
                        onValueChange = { postText = it },
                        label = { Text("Write your post...") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category selection
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "",
                            onValueChange = {},
                            label = { Text("Select Category") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text("${category.name} (${category.postCount})") },
                                    onClick = {
                                        selectedCategory = category
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Add New Category") },
                                onClick = {
                                    showAddCategoryDialog = true
                                    categoryDropdownExpanded = false
                                }
                            )
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
                                        coroutineScope.launch {
                                            addNewCategory(
                                                categoryName = newCategoryName,
                                                db = db,
                                                onSuccess = {
                                                    newCategoryName = ""
                                                    showAddCategoryDialog = false
                                                    // Refresh categories
                                                    db.collection("categories").get().addOnSuccessListener { snapshot ->
                                                        categories = snapshot.toObjects(Category::class.java)
                                                    }
                                                }
                                            )
                                        }
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

                    // Media upload buttons
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Text("Upload Image")
                        }
                        OutlinedButton(onClick = { videoPickerLauncher.launch("video/*") }) {
                            Text("Upload Video")
                        }
                        OutlinedButton(onClick = { pdfPickerLauncher.launch("application/pdf") }) {
                            Text("Upload PDF")
                        }
                    }

                    mediaUri?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        when (mediaType) {
                            "image" -> {
                                Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            "video" -> {
                                Text("Video selected: ${it.lastPathSegment}")
                            }
                            "pdf" -> {
                                Text("PDF selected: ${it.lastPathSegment}")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Add Remove Media Button
                        OutlinedButton(
                            onClick = {
                                mediaUri = null
                                mediaType = null
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Media")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove Media")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { showPreview = true },
                            modifier = Modifier.weight(1f),
                            enabled = postText.isNotEmpty() && selectedCategory != null
                        ) {
                            Text("Preview")
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    postText: String,
    selectedCategory: Category?,
    mediaUri: Uri?,
    mediaType: String?,
    onPublish: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview Post") }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Category: ${selectedCategory?.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = postText,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                mediaUri?.let {
                    when (mediaType) {
                        "image" -> {
                            Image(
                                painter = rememberAsyncImagePainter(it),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                        "video" -> {
                            Text("Video selected: ${it.lastPathSegment}")
                        }
                        "pdf" -> {
                            Text("PDF selected: ${it.lastPathSegment}")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                        Text("Edit")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = onPublish, modifier = Modifier.weight(1f)) {
                        Text("Publish")
                    }
                }
            }
        }
    )
}

suspend fun publishPost(
    textContent: String,
    category: Category?,
    mediaUri: Uri?,
    mediaType: String?,
    context: Context,
    onSuccess: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val user = auth.currentUser ?: return
    val userId = user.uid
    val userRef = db.collection("users").document(userId)

    try {
        val userSnapshot = userRef.get().await()
        val userName = userSnapshot.getString("name") ?: "Anonymous"
        val profileImageUri = userSnapshot.getString("profileImageUri")

        val postId = "${userId}_${System.currentTimeMillis()}"

        var mediaUrl: String? = null

        if (mediaUri != null && mediaType != null) {
            val mediaRef = storage.reference.child("posts/$postId/media")
            mediaRef.putFile(mediaUri).await()
            mediaUrl = mediaRef.downloadUrl.await().toString()
        }

        val post = Post(
            id = postId,
            userId = userId,
            userName = userName,
            profileImage = profileImageUri,
            categoryId = category?.id ?: "",
            categoryName = category?.name ?: "",
            textContent = textContent,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            timestamp = System.currentTimeMillis()
        )

        // Save post
        db.collection("Posts").document(postId).set(post).await()

        // Increment postCount in category
        if (category != null) {
            val categoryRef = db.collection("categories").document(category.id)
            categoryRef.update("postCount", FieldValue.increment(1)).await()
        }

        // Award coins to the user (e.g., 50 coins for creating a post)
        userRef.update("coinBalance", FieldValue.increment(50)).await()

        onSuccess()
    } catch (e: Exception) {
        Log.e("publishPost", "Error publishing post: ${e.message}")
        Toast.makeText(context, "Error publishing post: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

suspend fun addNewCategory(
    categoryName: String,
    db: FirebaseFirestore,
    onSuccess: () -> Unit
) {
    try {
        val categoryId = UUID.randomUUID().toString()
        val category = Category(
            id = categoryId,
            name = categoryName,
            postCount = 0
        )
        db.collection("categories").document(categoryId).set(category).await()
        onSuccess()
    } catch (e: Exception) {
        Log.e("addNewCategory", "Error adding category: ${e.message}")
        // Handle error
    }
}
