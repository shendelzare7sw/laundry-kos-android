package com.laundry_kos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.Maroon
import com.laundry_kos.navigation.LaundryKosRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Maroon.toArgb()),
        )
        val repository = (application as LaundryKosApp).container.repository
        setContent {
            LaundryKosTheme {
                LaundryKosRoot(repository = repository)
            }
        }
    }
}
