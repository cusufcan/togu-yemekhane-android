package com.mercan.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

@Database(entities = [WeekEntity::class, DayEntity::class, DishEntity::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class MenuDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
}

/** Tarihler ISO yazısı olarak durur (2026-11-23); yazı sırası tarih sırasıyla aynıdır. */
class DateConverter {
    @TypeConverter
    fun toText(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toDate(text: String): LocalDate = LocalDate.parse(text)
}
