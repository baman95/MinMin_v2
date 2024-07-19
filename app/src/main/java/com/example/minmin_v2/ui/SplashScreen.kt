package com.example.minmin_v2.ui.screens

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minmin_v2.R

@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val smallerDimension = if (screenWidth < screenHeight) screenWidth else screenHeight

    val cloudOffsetX by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else (-200).dp,
        animationSpec = tween(durationMillis = 2000)
    )
    val faceAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 3000)
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 3000)
    )

    LaunchedEffect(key1 = true) {
        Handler(Looper.getMainLooper()).postDelayed({
            startAnimation = true
            Log.d("SplashScreen", "Animations started")
        }, 1000) // 1 second delay

        Handler(Looper.getMainLooper()).postDelayed({
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }, 3000) // 3-second delay
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_image_minmin),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Image(
            painter = painterResource(id = R.drawable.cloud_minmin),
            contentDescription = null,
            modifier = Modifier
                .offset(x = cloudOffsetX, y = (-50).dp)
                .size(smallerDimension * 2f, smallerDimension * 1f), // 50% of smaller dimension for width, adjust height proportionally
            contentScale = ContentScale.FillBounds
        )
        Image(
            painter = painterResource(id = R.drawable.smily_face_minmin),
            contentDescription = null,
            modifier = Modifier
                .size(smallerDimension * 2f) // 50% of smaller dimension
                .alpha(faceAlpha),
            contentScale = ContentScale.Fit
        )
        BasicText(
            text = "MinMin",
            modifier = Modifier
                .alpha(textAlpha)
                .padding(top = smallerDimension * 0.5f), // Adjust text position based on screen size
            style = TextStyle(
                fontSize = 50.sp,
                color = Color.Red
            )
        )
    }
}
