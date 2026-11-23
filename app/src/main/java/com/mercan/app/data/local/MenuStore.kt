package com.mercan.app.data.local

import com.mercan.app.domain.DayMenu
import com.mercan.app.domain.Dish
import com.mercan.app.domain.WeekMenu
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface MenuStore {
    val weeks: Flow<List<WeekMenu>>
    suspend fun all(): List<WeekMenu>
    suspend fun replace(weeks: List<WeekMenu>)
}

class RoomMenuStore @Inject constructor(private val dao: MenuDao) : MenuStore {
    override val weeks: Flow<List<WeekMenu>> = dao.observeWeeks().map { rows -> rows.map { it.toDomain() } }

    override suspend fun all(): List<WeekMenu> = dao.allWeeks().map { it.toDomain() }

    override suspend fun replace(weeks: List<WeekMenu>) = dao.replaceWeeks(
        weeks = weeks.map { WeekEntity(it.weekStart, it.weekEnd) },
        days = weeks.flatMap { week -> week.days.map { DayEntity(it.date, week.weekStart, it.calories) } },
        dishes = weeks.flatMap { it.days }.flatMap { day ->
            day.dishes.mapIndexed { index, dish ->
                DishEntity(date = day.date, position = index, name = dish.name, vegetarian = dish.vegetarian)
            }
        },
    )
}

private fun WeekWithDays.toDomain() = WeekMenu(
    weekStart = week.weekStart,
    weekEnd = week.weekEnd,
    days = days.sortedBy { it.day.date }.map { row ->
        DayMenu(
            date = row.day.date,
            dishes = row.dishes.sortedBy { it.position }.map { Dish(it.name, it.vegetarian) },
            calories = row.day.calories,
        )
    },
)
