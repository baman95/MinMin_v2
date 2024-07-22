package com.example.minmin_v2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.minmin_v2.ui.Navigation
import com.example.minmin_v2.ui.theme.MinMin_v2Theme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // Commented out to avoid default splash screen
        // installSplashScreen()
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        sharedPreferences = getSharedPreferences("minmin_prefs", Context.MODE_PRIVATE)
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Save the authentication state
        if (user != null) {
            sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
        } else {
            sharedPreferences.edit().putBoolean("is_logged_in", false).apply()
        }

        setContent {
            MinMin_v2Theme {
                val navController = rememberNavController()
                Surface(color = MaterialTheme.colorScheme.background) {
                    Navigation(navController = navController, sharedPreferences = sharedPreferences)
                }
            }
        }
    }
}


//
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
