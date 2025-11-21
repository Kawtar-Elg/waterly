package com.example.exam.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationHelper {
    
    private const val WORK_NAME = "water_reminder_work"
    
    fun scheduleWaterReminder(context: Context, intervalHours: Long = 2) {
        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            intervalHours, TimeUnit.HOURS
        ).build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    fun cancelWaterReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
