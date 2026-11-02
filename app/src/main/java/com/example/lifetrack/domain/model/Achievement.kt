package com.example.lifetrack.domain.model

data class Achievement(
    val id: Long = 0L,
    val code: String,
    val name: String,
    val description: String,
    val icon: String,
    val conditionType: AchievementConditionType,
    val targetValue: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)
