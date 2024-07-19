package com.example.minmin_v2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.minmin_v2.ui.Navigation
import com.example.minmin_v2.ui.theme.MinMin_v2Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        setContent {
            MinMin_v2Theme {
                val navController = rememberNavController()
                Surface(color = MaterialTheme.colorScheme.background) {
                    Navigation(navController = navController)
                }
            }
        }
    }
}

//@Composable
//fun SplashScreen(navController: NavHostController) {
//    var startAnimation by remember { mutableStateOf(false) }
//    val cloudOffsetX by animateDpAsState(
//        targetValue = if (startAnimation) 0.dp else (-200).dp,
//        animationSpec = tween(durationMillis = 2000)
//    )
//    val faceAlpha by animateFloatAsState(
//        targetValue = if (startAnimation) 1f else 0f,
//        animationSpec = tween(durationMillis = 3000)
//    )
//    val textAlpha by animateFloatAsState(
//        targetValue = if (startAnimation) 1f else 0f,
//        animationSpec = tween(durationMillis = 3000)
//    )
//
//    LaunchedEffect(key1 = true) {
//        Handler(Looper.getMainLooper()).postDelayed({
//            startAnimation = true
//            Log.d("SplashScreen", "Animations started")
//        }, 1000) // 1 second delay
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            navController.navigate("login") {
//                popUpTo("splash") { inclusive = true }
//            }
//        }, 3000) // 3-second delay
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White),
//        contentAlignment = Alignment.Center
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.background_image_minmin),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )
//        Image(
//            painter = painterResource(id = R.drawable.cloud_minmin),
//            contentDescription = null,
//            modifier = Modifier
//                .offset(x = cloudOffsetX, y = (-300).dp)
//                .size(1200.dp, 900.dp)
//        )
//        Image(
//            painter = painterResource(id = R.drawable.smily_face_minmin),
//            contentDescription = null,
//            modifier = Modifier
//                .size(200.dp)
//                .alpha(faceAlpha)
//        )
//        BasicText(
//            text = "MinMin",
//            modifier = Modifier
//                .alpha(textAlpha)
//                .padding(top = 100.dp),
//            style = TextStyle(
//                fontSize = 100.sp,
//                color = Color.Green
//            )
//        )
//    }
//}
