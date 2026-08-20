package com.mishba.ecopantryapp.notification

import android.Manifest
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mishba.ecopantryapp.R
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.NotificationTable
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.model.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class EcoPantryMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "Donation update"
        val body = message.notification?.body ?: message.data["body"] ?: "There is an update on your donation."
        val relatedId = message.data["relatedId"] ?: message.data["donationId"]

        CoroutineScope(Dispatchers.IO).launch {
            val userId = AppDataStore(applicationContext).loggedInUserIdFlow().first() ?: return@launch
            val repository = Repository(AppDatabase.getInstance(applicationContext))
            repository.insertNotification(
                NotificationTable(
                    userId = userId,
                    type = NotificationType.DONATION_CLAIMED,
                    title = title,
                    message = body,
                    relatedItemId = relatedId
                )
            )
        }
        postSystemNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Store the token in Firestore (asynchronously)
        CoroutineScope(Dispatchers.IO).launch {
            val userId = AppDataStore(applicationContext).loggedInUserIdFlow().first()
            if (userId != null) {
                try {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update("fcmToken", token)
                        .await()
                } catch (e: Exception) {
                    // If document doesn't exist yet, set it
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                        .await()
                }
            }
        }
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