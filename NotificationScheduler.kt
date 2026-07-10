package com.ecopantry.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ecopantry.app.ui.navigation.AppNavigation
import com.ecopantry.app.ui.theme.EcoPantryTheme
import com.ecopantry.app.utility.NotificationScheduler

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
