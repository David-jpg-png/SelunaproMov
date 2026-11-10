package me.andresdavid.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.andresdavid.myapplication.navigation.AppNavigation
import me.andresdavid.myapplication.ui.theme.SelunaProyectoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SelunaProyectoTheme {
                AppNavigation()
            }
        }
    }
}

