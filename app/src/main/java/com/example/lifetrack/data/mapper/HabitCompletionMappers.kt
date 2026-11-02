package com.example.lifetrack.data.mapper

import com.example.lifetrack.data.local.db.entity.HabitCompletionEntity
import com.example.lifetrack.domain.model.HabitCompletion

fun HabitCompletionEntity.toDomain(): HabitCompletion = HabitCompletion(
    id = id,
    habitId = habitId,
    date = date,
    status = status
)

fun HabitCompletion.toEntity(): HabitCompletionEntity = HabitCompletionEntity(
    id = id,
    habitId = habitId,
    date = date,
    status = status
)
