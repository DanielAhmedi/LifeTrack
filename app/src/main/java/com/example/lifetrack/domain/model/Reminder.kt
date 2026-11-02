package com.example.lifetrack.domain.model

data class Reminder(
    val id: Long = 0L,
    val habitId: Long,
    val timeMinutes: Int,
    val isActive: Boolean = true
)
