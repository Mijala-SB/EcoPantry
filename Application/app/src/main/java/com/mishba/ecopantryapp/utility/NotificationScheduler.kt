package com.mishba.ecopantryapp.utility

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mishba.ecopantryapp.R
import com.mishba.ecopantryapp.worker.ExpiryCheckWorker
import com.mishba.ecopantryapp.worker.MealReminderWorker
import java.util.concurrent.TimeUnit

/** Sets up notification channels, the recurring background expiry scan (FR11), and
 *  one-off meal plan reminders (Use Case 6). */
object NotificationScheduler {

    private const val EXPIRY_WORK_NAME = "ecopantry_expiry_check_work"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)

        val expiryChannel = NotificationChannel(
            context.getString(R.string.expiry_notification_channel_id),
            context.getString(R.string.expiry_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Alerts for food items nearing their expiry date" }

        val donationChannel = NotificationChannel(
            context.getString(R.string.donation_notification_channel_id),
            context.getString(R.string.donation_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Updates when a donation is claimed or confirmed" }

        manager.createNotificationChannel(expiryChannel)
        manager.createNotificationChannel(donationChannel)
    }

    /** Runs once a day to scan the local inventory for items expiring within 3 days (FR11, US 4.1). */
    fun scheduleExpiryWorker(context: Context) {
        val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EXPIRY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Schedules a one-off reminder for a planned meal, firing [delayMillis] from now
     * (Use Case 6 - Plan Weekly Meals, Typical Course step 8). Uses a unique work name per
     * plan so re-confirming a week doesn't stack duplicate reminders.
     */
    fun scheduleMealReminder(
        context: Context,
        planId: String,
        userId: String,
        mealName: String,
        slotLabel: String,
        delayMillis: Long
    ) {
        val data = Data.Builder()
            .putString(MealReminderWorker.KEY_USER_ID, userId)
            .putString(MealReminderWorker.KEY_PLAN_ID, planId)
            .putString(MealReminderWorker.KEY_MEAL_NAME, mealName)
            .putString(MealReminderWorker.KEY_SLOT_LABEL, slotLabel)
            .build()

        val request = OneTimeWorkRequestBuilder<MealReminderWorker>()
            .setInitialDelay(delayMillis.coerceAtLeast(0), TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "ecopantry_meal_reminder_$planId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** Cancels a previously scheduled meal reminder, e.g. when the meal is removed from the plan. */
    fun cancelMealReminder(context: Context, planId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("ecopantry_meal_reminder_$planId")
    }
}
