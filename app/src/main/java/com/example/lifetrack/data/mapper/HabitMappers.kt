package com.example.lifetrack.data.mapper

import com.example.lifetrack.data.local.db.entity.HabitEntity
import com.example.lifetrack.domain.model.Habit

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    name = name,
    description = description,
    categoryId = categoryId,
    icon = icon,
    color = color,
    createdAt = createdAt,
    frequencyType = frequencyType,
    weekDaysMask = weekDaysMask,
    intervalDays = intervalDays,
    isActive = isActive
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    description = description,
    categoryId = categoryId,
    icon = icon,
    color = color,
    createdAt = createdAt,
    frequencyType = frequencyType,
    weekDaysMask = weekDaysMask,
    intervalDays = intervalDays,
    isActive = isActive
)
