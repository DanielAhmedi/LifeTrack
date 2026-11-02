package com.example.lifetrack.domain.model

data class Habit(
    val id: Long = 0L,
    val name: String,
    val description: String? = null,
    val categoryId: Long? = null,
    val icon: String,
    val color: Int,
    val createdAt: Long,
    val frequencyType: FrequencyType,
    val weekDaysMask: Int? = null,
    val intervalDays: Int? = null,
    val isActive: Boolean = true
)
