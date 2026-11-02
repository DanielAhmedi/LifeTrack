package com.example.lifetrack.data.mapper

import com.example.lifetrack.data.local.db.entity.HabitCategoryEntity
import com.example.lifetrack.domain.model.HabitCategory

fun HabitCategoryEntity.toDomain(): HabitCategory = HabitCategory(
    id = id,
    name = name,
    icon = icon,
    color = color
)

fun HabitCategory.toEntity(): HabitCategoryEntity = HabitCategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color
)
