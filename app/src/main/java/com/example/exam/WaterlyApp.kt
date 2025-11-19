package com.example.exam

import android.app.Application
import androidx.room.Room
import com.example.exam.data.database.AppDatabase
import com.example.exam.workers.WaterReminderWorker
import kotlin.getValue

class WaterlyApp : Application() {
    
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "waterly_database"
        ).build()
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager for reminders
        WaterReminderWorker.scheduleReminders(this)
    }
}