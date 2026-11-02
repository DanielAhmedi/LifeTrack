package com.example.lifetrack.domain.model

data class HabitCompletion(
    val id: Long = 0L,
    val habitId: Long,
    val date: Long,
    val status: CompletionStatus
)
