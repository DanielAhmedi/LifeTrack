package com.example.lifetrack.data.mapper

import com.example.lifetrack.data.local.db.entity.AchievementEntity
import com.example.lifetrack.domain.model.Achievement

fun AchievementEntity.toDomain(): Achievement = Achievement(
    id = id,
    code = code,
    name = name,
    description = description,
    icon = icon,
    conditionType = conditionType,
    targetValue = targetValue,
    isUnlocked = isUnlocked,
    unlockedAt = unlockedAt
)

fun Achievement.toEntity(): AchievementEntity = AchievementEntity(
    id = id,
    code = code,
    name = name,
    description = description,
    icon = icon,
    conditionType = conditionType,
    targetValue = targetValue,
    isUnlocked = isUnlocked,
    unlockedAt = unlockedAt
)
