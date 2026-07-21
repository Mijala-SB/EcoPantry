package com.mishba.ecopantryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mishba.ecopantryapp.ui.navigation.AppNavigation
import com.mishba.ecopantryapp.ui.theme.EcoPantryTheme
import com.mishba.ecopantryapp.utility.NotificationScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Register notification channels & schedule the daily expiry-scan worker (FR11)
        NotificationScheduler.createChannels(applicationContext)
        NotificationScheduler.scheduleExpiryWorker(applicationContext)

        setContent {
            EcoPantryTheme {
                AppNavigation()
            }
        }
    }
}
