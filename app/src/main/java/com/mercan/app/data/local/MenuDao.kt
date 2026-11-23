package com.mercan.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MenuDao {
    @Transaction
    @Query("SELECT * FROM weeks ORDER BY weekStart DESC")
    fun observeWeeks(): Flow<List<WeekWithDays>>

    @Transaction
    @Query("SELECT * FROM weeks")
    suspend fun allWeeks(): List<WeekWithDays>

    /** Verilen haftaların eski hâlini silip yenisini yazar; yarıda kalırsa hiçbiri değişmez. */
    @Transaction
    suspend fun replaceWeeks(weeks: List<WeekEntity>, days: List<DayEntity>, dishes: List<DishEntity>) {
        weeks.forEach { deleteWeek(it.weekStart) }
        insertWeeks(weeks)
        insertDays(days)
        insertDishes(dishes)
    }

    @Query("DELETE FROM weeks WHERE weekStart = :weekStart")
    suspend fun deleteWeek(weekStart: LocalDate)

    @Insert
    suspend fun insertWeeks(weeks: List<WeekEntity>)

    @Insert
    suspend fun insertDays(days: List<DayEntity>)

    @Insert
    suspend fun insertDishes(dishes: List<DishEntity>)
}
