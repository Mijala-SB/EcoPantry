package com.mishba.ecopantryapp.worker

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mishba.ecopantryapp.R
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.NotificationTable
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.model.NotificationType
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Daily background scan that finds inventory items expiring within 3 days and
 * raises both an in-app [NotificationTable] row and a system push notification
 * (FR11, US 4.1). Registered via [com.mishba.ecopantryapp.utility.NotificationScheduler].
 */
class ExpiryCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = AppDataStore(applicationContext).loggedInUserIdFlow().first() ?: return Result.success()
        val repository = Repository(AppDatabase.getInstance(applicationContext))

        val now = System.currentTimeMillis()
        val windowEnd = now + TimeUnit.DAYS.toMillis(3)
        val expiringItems = repository.getItemsExpiringSoon(now, windowEnd)

        for (item in expiringItems) {
            // Avoid re-notifying about the same item more than once every 24 hours.
            val recentAlerts = repository.countRecentExpiryAlertsFor(item.itemId, now - TimeUnit.DAYS.toMillis(1))
            if (recentAlerts > 0) continue

            val daysLeft = ((item.expiryDate ?: now) - now) / TimeUnit.DAYS.toMillis(1)
            val message = "\"${item.itemName}\" expires in ${daysLeft.coerceAtLeast(0)} day(s). Use or donate it soon."

            repository.insertNotification(
                NotificationTable(
                    userId = userId,
                    type = NotificationType.EXPIRY_ALERT,
                    title = "Food expiring soon",
                    message = message,
                    relatedItemId = item.itemId
                )
            )
            postSystemNotification(item.itemName, message)
        }
        return Result.success()
    }

    private fun postSystemNotification(itemName: String, message: String) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(
            applicationContext, applicationContext.getString(R.string.expiry_notification_channel_id)
        )
            .setSmallIcon(R.mipmap.ic_launcher_logo)
            .setContentTitle("EcoPantry - $itemName expiring soon")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(itemName.hashCode(), notification)
    }
}
