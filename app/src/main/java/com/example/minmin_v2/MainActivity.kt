package com.example.minmin_v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.minmin_v2.ui.Navigation
import com.example.minmin_v2.ui.theme.MinMin_v2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinMin_v2Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavigationComponent()
                }
            }
        }
    }
}

@Composable
fun NavigationComponent() {
    val navController: NavHostController = rememberNavController()
    Navigation(navController = navController)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MinMin_v2Theme {
        NavigationComponent()
    }
}
