package com.example.lifetrack.domain.repository

import com.example.lifetrack.domain.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

interface HabitCompletionRepository {

    suspend fun getById(id: Long): HabitCompletion?

    suspend fun getByHabitAndDate(habitId: Long, date: Long): HabitCompletion?

    suspend fun getByHabit(habitId: Long): List<HabitCompletion>

    fun observeByHabit(habitId: Long): Flow<List<HabitCompletion>>

    fun observeByHabitBetween(
        habitId: Long,
        fromDate: Long,
        toDate: Long
    ): Flow<List<HabitCompletion>>

    suspend fun insert(completion: HabitCompletion): Long

    suspend fun update(completion: HabitCompletion)

    suspend fun delete(completion: HabitCompletion)

    suspend fun deleteById(id: Long)
}
