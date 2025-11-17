package com.waterly.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.waterly.data.dao.UserDao
import com.waterly.data.dao.WaterConsumptionDao
import com.example.exam.data.entity.User
import com.waterly.data.entity.WaterConsumption
import com.waterly.utils.Converters

@Database(
    entities = [User::class, WaterConsumption::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun waterConsumptionDao(): WaterConsumptionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "waterly_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}