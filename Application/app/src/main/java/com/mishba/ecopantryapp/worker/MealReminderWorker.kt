// File: com/mishba/ecopantryapp/worker/MealReminderWorker.kt
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
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.NotificationTable
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.model.NotificationType

class MealReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_PLAN_ID = "plan_id"
        const val KEY_MEAL_NAME = "meal_name"
        const val KEY_SLOT_LABEL = "slot_label"
    }

    override suspend fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.success()
        val planId = inputData.getString(KEY_PLAN_ID) ?: return Result.success()
        val mealName = inputData.getString(KEY_MEAL_NAME) ?: "your planned meal"
        val slotLabel = inputData.getString(KEY_SLOT_LABEL) ?: "today"

        val repository = Repository(AppDatabase.getInstance(applicationContext))
        // Meal may have been removed from the plan since the reminder was scheduled.
        if (repository.getMealPlanById(planId) == null) return Result.success()

        val message = "It's almost time for $slotLabel: $mealName."
        repository.insertNotification(
            NotificationTable(
                userId = userId,
                type = NotificationType.MEAL_REMINDER,
                title = "Meal plan reminder",
                message = message,
                relatedItemId = planId
            )
        )
        postSystemNotification(mealName, message)
        return Result.success()
    }

    private fun postSystemNotification(mealName: String, message: String) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(
            applicationContext, applicationContext.getString(R.string.donation_notification_channel_id)
        )
            .setSmallIcon(R.mipmap.ic_launcher_logo)
            .setContentTitle("EcoPantry - Meal reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(mealName.hashCode(), notification)
    }
}