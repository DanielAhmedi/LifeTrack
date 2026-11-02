package com.example.lifetrack.data.mapper

import com.example.lifetrack.data.local.db.entity.ReminderEntity
import com.example.lifetrack.domain.model.Reminder

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    habitId = habitId,
    timeMinutes = timeMinutes,
    isActive = isActive
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    habitId = habitId,
    timeMinutes = timeMinutes,
    isActive = isActive
)
