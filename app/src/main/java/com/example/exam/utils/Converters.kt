package com.waterly.utils

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): String? {
        return value?.let {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.format(Date(it))
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: String?): Long? {
        return date?.let {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.parse(it)?.time
        }
    }
}