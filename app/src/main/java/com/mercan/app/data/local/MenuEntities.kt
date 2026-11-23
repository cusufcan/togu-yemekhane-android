package com.mercan.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDate

@Entity(tableName = "weeks")
data class WeekEntity(
    @PrimaryKey val weekStart: LocalDate,
    val weekEnd: LocalDate,
)

@Entity(
    tableName = "days",
    foreignKeys = [
        ForeignKey(WeekEntity::class, ["weekStart"], ["weekStart"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("weekStart")],
)
data class DayEntity(
    @PrimaryKey val date: LocalDate,
    val weekStart: LocalDate,
    val calories: Int?,
)

@Entity(
    tableName = "dishes",
    foreignKeys = [
        ForeignKey(DayEntity::class, ["date"], ["date"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("date")],
)
data class DishEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val position: Int,
    val name: String,
    val vegetarian: Boolean,
)

data class DayWithDishes(
    @Embedded val day: DayEntity,
    @Relation(parentColumn = "date", entityColumn = "date") val dishes: List<DishEntity>,
)

data class WeekWithDays(
    @Embedded val week: WeekEntity,
    @Relation(entity = DayEntity::class, parentColumn = "weekStart", entityColumn = "weekStart")
    val days: List<DayWithDishes>,
)
