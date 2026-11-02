package com.example.lifetrack.domain.repository

import com.example.lifetrack.domain.model.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {

    fun observeAll(): Flow<List<Habit>>

    fun observeActive(): Flow<List<Habit>>

    suspend fun getAll(): List<Habit>

    suspend fun getById(id: Long): Habit?

    suspend fun insert(habit: Habit): Long

    suspend fun update(habit: Habit)

    suspend fun delete(habit: Habit)

    suspend fun deleteById(id: Long)
}
