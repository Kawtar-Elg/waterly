package com.waterly.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.waterly.R
import java.util.concurrent.TimeUnit

class WaterReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val notificationManager: NotificationManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getLong("user_id", 0L)
        val reminderTime = inputData.getString("reminder_time") ?: "Rappel d'hydratation"
        val message = inputData.getString("message") ?: "Il est temps de boire de l'eau!"
        
        showNotification(reminderTime, message)
        
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Water Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Rappels pour boire de l'eau"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "water_reminder_channel"
        private const val NOTIFICATION_ID = 1001
        private const val WORK_TAG = "water_reminder"

        fun scheduleReminders(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkConstraint.NOT_REQUIRED)
                .build()

            // Schedule reminder every 2 hours during waking hours (8 AM - 10 PM)
            val reminderRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(2, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInputData(workDataOf(
                    "user_id" to 1L, // Will be updated with actual user ID
                    "reminder_time" to "💧 Rappel d'hydratation",
                    "message" to "Il est temps de boire un verre d'eau!"
                ))
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
            )
        }
    }
}