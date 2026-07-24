package com.mishba.ecopantryapp.notification

import android.Manifest
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.mishba.ecopantryapp.R
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.NotificationTable
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.model.NotificationType
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * Receives push notifications sent when a donation is claimed or a pickup is
 * confirmed (FR12, US 5.2). In production, a Cloud Function listening on
 * Firestore writes to the `donations` collection would trigger these pushes;
 * this service is the client-side receiver that surfaces them to the user.
 */
class EcoPantryMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "Donation update"
        val body = message.notification?.body ?: message.data["body"] ?: "There is an update on your donation."

        CoroutineScope(Dispatchers.IO).launch {
            val userId = AppDataStore(applicationContext).loggedInUserIdFlow().first() ?: return@launch
            val repository = Repository(AppDatabase.getInstance(applicationContext))
            repository.insertNotification(
                NotificationTable(
                    userId = userId,
                    type = NotificationType.DONATION_CLAIMED,
                    title = title,
                    message = body
                )
            )
        }
        postSystemNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // In production this token would be written to the user's Firestore document
        // so a Cloud Function can target this device for donation-claim pushes.
    }

    private fun postSystemNotification(title: String, body: String) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(
            applicationContext, getString(R.string.donation_notification_channel_id)
        )
            .setSmallIcon(R.mipmap.ic_launcher_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(title.hashCode(), notification)
    }
}
